package org.aggregation.service.migration;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.aggregation.service.config.DataSourceFactory;
import org.aggregation.service.config.DataSourcesProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataSourceMigrationRunner implements ApplicationRunner {

    private final DataSourcesProperties dataSourcesProperties;
    private final DataSourceFactory dataSourceFactory;
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    public DataSourceMigrationRunner(DataSourcesProperties dataSourcesProperties,
                                     DataSourceFactory dataSourceFactory) {
        this.dataSourcesProperties = dataSourcesProperties;
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (DataSourcesProperties.DataSourceConfig source : dataSourcesProperties.getDataSources()) {
            migrate(source);
        }
    }

    private void migrate(DataSourcesProperties.DataSourceConfig source) {
        String changelog = source.getChangelog();
        if (changelog == null || changelog.isBlank()) {
            log.info("No changelog configured for data source '{}', skipping migration", source.getName());
            return;
        }
        log.info("Running Liquibase migration for data source '{}' using changelog '{}'",
                source.getName(), changelog);
        try {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setResourceLoader(resourceLoader);
            liquibase.setDataSource(dataSourceFactory.get(source));
            liquibase.setChangeLog(normalize(changelog));
            liquibase.setShouldRun(true);
            liquibase.afterPropertiesSet();
            log.info("Migration completed for data source '{}'", source.getName());
        } catch (Exception ex) {
            log.error("Migration failed for data source '{}': {}", source.getName(), ex.getMessage(), ex);
        }
    }

    private String normalize(String changelog) {
        if (changelog.contains(":")) {
            return changelog;
        }
        return "classpath:" + changelog;
    }
}
