package org.aggregation.service.repository;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aggregation.service.config.DataSourceFactory;
import org.aggregation.service.config.DataSourcesProperties;
import org.aggregation.service.model.User;
import org.aggregation.service.model.UserFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class UserAggregationRepository {

    private final DataSourceFactory dataSourceFactory;

    public List<User> findAll(List<DataSourcesProperties.DataSourceConfig> sourceConfigs, UserFilter filter) {
        List<User> result = new ArrayList<>();
        for (DataSourcesProperties.DataSourceConfig sourceConfig : sourceConfigs) {
            result.addAll(readFromSource(sourceConfig, filter));
        }
        return result;
    }

    private List<User> readFromSource(DataSourcesProperties.DataSourceConfig sourceConfig, UserFilter filter) {
        List<Object> params = new ArrayList<>();
        String sql = buildSql(sourceConfig, filter, params);
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceFactory.get(sourceConfig));
            return jdbcTemplate.query(sql,
                    (rs, rowNum) -> new User(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("surname")),
                    params.toArray());
        } catch (RuntimeException ex) {
            log.warn("Skipping data source '{}': {}", sourceConfig.getName(), ex.getMessage());
            return List.of();
        }
    }

    private String buildSql(DataSourcesProperties.DataSourceConfig sourceConfig, UserFilter filter, List<Object> params) {
        DataSourcesProperties.Mapping mapping = sourceConfig.getMapping();
        StringBuilder sql = new StringBuilder("select ")
                .append(mapping.getId()).append(" as id, ")
                .append(mapping.getUsername()).append(" as username, ")
                .append(mapping.getName()).append(" as name, ")
                .append(mapping.getSurname()).append(" as surname ")
                .append("from ").append(sourceConfig.getTable());

        List<String> conditions = new ArrayList<>();
        addCondition(conditions, params, mapping.getId(), filter.id());
        addCondition(conditions, params, mapping.getUsername(), filter.username());
        addCondition(conditions, params, mapping.getName(), filter.name());
        addCondition(conditions, params, mapping.getSurname(), filter.surname());
        if (!conditions.isEmpty()) {
            sql.append(" where ").append(String.join(" and ", conditions));
        }
        return sql.toString();
    }

    private void addCondition(List<String> conditions, List<Object> params, String column, String value) {
        if (value != null && !value.isBlank()) {
            conditions.add(column + " = ?");
            params.add(value);
        }
    }
}
