package org.aggregation.service.service;

import java.util.List;

import lombok.AllArgsConstructor;
import org.aggregation.service.config.DataSourcesProperties;
import org.aggregation.service.exception.UsersNotFoundException;
import org.aggregation.service.model.User;
import org.aggregation.service.model.UserFilter;
import org.aggregation.service.repository.UserAggregationRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAggregationService {

    private final DataSourcesProperties dataSourcesProperties;
    private final UserAggregationRepository userAggregationRepository;

    public List<User> getUsers(UserFilter filter) {
        List<User> users = userAggregationRepository.findAll(dataSourcesProperties.getDataSources(), filter);
        if (users.isEmpty()) {
            throw new UsersNotFoundException("No users found matching the requested criteria");
        }
        return users;
    }
}
