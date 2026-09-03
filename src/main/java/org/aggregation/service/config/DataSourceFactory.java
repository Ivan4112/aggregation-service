package org.aggregation.service.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Provides a single, long-lived Hikari connection pool per configured data
 * source. Pools are created lazily on first use and cached by source name, so
 * repeated requests (and the startup migration) reuse the same pool instead of
 * opening a new one every time. All pools are closed on application shutdown.
 */
@Slf4j
@Component
public class DataSourceFactory {

    private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();

    public HikariDataSource get(DataSourcesProperties.DataSourceConfig sourceConfig) {
        return pools.computeIfAbsent(sourceConfig.getName(), name -> create(sourceConfig));
    }

    private HikariDataSource create(DataSourcesProperties.DataSourceConfig sourceConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(sourceConfig.getUrl());
        config.setUsername(sourceConfig.getUser());
        config.setPassword(sourceConfig.getPassword());
        if (sourceConfig.getDriverClassName() != null && !sourceConfig.getDriverClassName().isBlank()) {
            config.setDriverClassName(sourceConfig.getDriverClassName());
        }
        config.setPoolName("aggregation-" + sourceConfig.getName());
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(10_000L);
        config.setInitializationFailTimeout(-1L);
        return new HikariDataSource(config);
    }

    @PreDestroy
    public void closeAll() {
        pools.forEach((name, pool) -> {
            log.info("Closing connection pool for data source '{}'", name);
            pool.close();
        });
        pools.clear();
    }
}
