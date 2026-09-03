package org.aggregation.service.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "aggregation")
public class DataSourcesProperties {

    @NotEmpty
    private List<@Valid DataSourceConfig> dataSources;

    @Data
    public static class DataSourceConfig {

        @NotBlank
        private String name;

        @NotBlank
        private String strategy;

        @NotBlank
        private String url;

        private String driverClassName;

        @NotBlank
        private String user;

        @NotBlank
        private String password;

        @NotBlank
        private String table;

        private String changelog;

        @NotNull
        @Valid
        private Mapping mapping;
    }

    @Data
    public static class Mapping {

        @NotBlank
        private String id;

        @NotBlank
        private String username;

        @NotBlank
        private String name;

        @NotBlank
        private String surname;
    }
}
