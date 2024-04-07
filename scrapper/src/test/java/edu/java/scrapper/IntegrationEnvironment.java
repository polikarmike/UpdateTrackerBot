package edu.java.scrapper;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;

@Testcontainers
public abstract class IntegrationEnvironment {
    public static PostgreSQLContainer<?> POSTGRES;
    public static KafkaContainer KAFKA;


    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");
        POSTGRES.setCommand("postgres", "-c", "max_connections=300");

        POSTGRES.start();

        runMigrations(POSTGRES);

        KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092");

        KAFKA.start();
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

        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        System.out.println(KAFKA.getBootstrapServers());
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
