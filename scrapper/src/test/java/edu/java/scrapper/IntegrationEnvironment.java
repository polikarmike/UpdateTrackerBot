package edu.java.scrapper;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers
public abstract class IntegrationEnvironment {
    public static PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");
        POSTGRES.setCommand("postgres", "-c", "max_connections=300");

        POSTGRES.start();

        runMigrations(POSTGRES);
    }

    public static void runMigrations(JdbcDatabaseContainer<?> container) {
        try (Connection connection = createConnection(container)) {
            Liquibase liquibase = createLiquibase(connection);
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static Connection createConnection(JdbcDatabaseContainer<?> container) throws SQLException {
        return DriverManager.getConnection(
            container.getJdbcUrl(),
            container.getUsername(),
            container.getPassword()
        );
    }

    private static Liquibase createLiquibase(Connection connection) throws FileNotFoundException {
        Database database = new PostgresDatabase();
        database.setConnection(new JdbcConnection(connection));

        Path changelogPath =  Paths.get("").toAbsolutePath().getParent().resolve("migrations");

        return new Liquibase(
            "master.xml",
            new DirectoryResourceAccessor(changelogPath),
            database
        );
    }
}
