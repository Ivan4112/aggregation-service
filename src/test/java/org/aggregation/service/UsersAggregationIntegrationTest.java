package org.aggregation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.aggregation.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UsersAggregationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("db1")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("db2")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void dataSources(DynamicPropertyRegistry registry) {
        // Source 0: PostgreSQL
        registry.add("aggregation.data-sources[0].name", () -> "postgres");
        registry.add("aggregation.data-sources[0].strategy", () -> "postgres");
        registry.add("aggregation.data-sources[0].url", POSTGRES::getJdbcUrl);
        registry.add("aggregation.data-sources[0].driverClassName", () -> "org.postgresql.Driver");
        registry.add("aggregation.data-sources[0].table", () -> "users");
        registry.add("aggregation.data-sources[0].user", POSTGRES::getUsername);
        registry.add("aggregation.data-sources[0].password", POSTGRES::getPassword);
        registry.add("aggregation.data-sources[0].changelog", () -> "db/changelog/postgres/postgres-changelog.xml");
        registry.add("aggregation.data-sources[0].mapping.id", () -> "user_id");
        registry.add("aggregation.data-sources[0].mapping.username", () -> "login");
        registry.add("aggregation.data-sources[0].mapping.name", () -> "first_name");
        registry.add("aggregation.data-sources[0].mapping.surname", () -> "last_name");

        // Source 1: MySQL
        registry.add("aggregation.data-sources[1].name", () -> "mysql");
        registry.add("aggregation.data-sources[1].strategy", () -> "mysql");
        registry.add("aggregation.data-sources[1].url", MYSQL::getJdbcUrl);
        registry.add("aggregation.data-sources[1].driverClassName", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("aggregation.data-sources[1].table", () -> "user_table");
        registry.add("aggregation.data-sources[1].user", MYSQL::getUsername);
        registry.add("aggregation.data-sources[1].password", MYSQL::getPassword);
        registry.add("aggregation.data-sources[1].changelog", () -> "db/changelog/mysql/mysql-changelog.xml");
        registry.add("aggregation.data-sources[1].mapping.id", () -> "ldap_login");
        registry.add("aggregation.data-sources[1].mapping.username", () -> "ldap_login");
        registry.add("aggregation.data-sources[1].mapping.name", () -> "name");
        registry.add("aggregation.data-sources[1].mapping.surname", () -> "surname");
    }

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("http://localhost:" + port);
    }

    @Test
    void aggregatesUsersFromAllDataSources() {
        User[] users = client.get().uri("/users").retrieve().body(User[].class);

        assertThat(users).isNotNull();
        assertThat(users)
                .extracting(User::username)
                .contains("user-1", "user-3", "user-2", "user-4");
        assertThat(users)
                .filteredOn(u -> "user-2".equals(u.username()))
                .singleElement()
                .satisfies(u -> {
                    assertThat(u.name()).isEqualTo("Testuser");
                    assertThat(u.surname()).isEqualTo("Testov");
                });
    }

    @Test
    void filtersByUsername() {
        User[] users = client.get().uri("/users?username=user-1").retrieve().body(User[].class);

        assertThat(users).isNotNull();
        assertThat(users).hasSize(1);
        assertThat(users[0].username()).isEqualTo("user-1");
        assertThat(users[0].name()).isEqualTo("User");
        assertThat(users[0].surname()).isEqualTo("Userenko");
    }

    @Test
    void filtersBySurname() {
        User[] users = client.get().uri("/users?surname=Petrenko").retrieve().body(User[].class);

        assertThat(users).isNotNull();
        assertThat(users).hasSize(1);
        assertThat(users[0].username()).isEqualTo("user-4");
    }

    @Test
    void returnsNotFoundWhenNoUserMatches() {
        assertThatThrownBy(() ->
                client.get().uri("/users?username=does-not-exist").retrieve().body(User[].class))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
