package edu.java.scrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseCreationTest extends IntegrationEnvironment {
    @Test
    @DisplayName("Проверка создания базы данных")
    public void testDatabaseCreation() {
        try {
            Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
            );

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(
                "SELECT table_name " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'public';"
            );

            List<String> tableNames = new ArrayList<>();

            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
                tableNames.add(tableName);
            }

            Assertions.assertTrue(tableNames.contains("link"));
            Assertions.assertTrue(tableNames.contains("link_chat"));
            Assertions.assertTrue(tableNames.contains("chat"));

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
