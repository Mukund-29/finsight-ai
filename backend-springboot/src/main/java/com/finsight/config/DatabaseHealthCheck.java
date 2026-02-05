package com.finsight.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Database Health Check - Logs which database is being used
 * 
 * @author Mukund Kute
 */
@Component
@Order(1)
public class DatabaseHealthCheck implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            String driverName = metaData.getDriverName();
            String driverVersion = metaData.getDriverVersion();
            String url = metaData.getURL();
            String username = metaData.getUserName();

            logger.info("=========================================");
            logger.info("DATABASE CONNECTION INFO:");
            logger.info("Database: {}", databaseProductName);
            logger.info("Version: {}", databaseProductVersion);
            logger.info("Driver: {}", driverName);
            logger.info("Driver Version: {}", driverVersion);
            // Mask sensitive connection info in logs
            String maskedUrl = url != null ? maskUrl(url) : "N/A";
            String maskedUsername = username != null ? maskUsername(username) : "N/A";
            logger.info("URL: {}", maskedUrl);
            logger.info("Username: {}", maskedUsername);
            logger.info("=========================================");

            // Check if FLOWAI_USERS table exists
            try {
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FLOWAI_USERS", Integer.class);
                logger.info("FLOWAI_USERS table EXISTS in database");
            } catch (Exception e) {
                logger.warn("FLOWAI_USERS table DOES NOT EXIST in database");
                logger.warn("Hibernate will create it on first entity save");
            }
        } catch (Exception e) {
            logger.error("Error checking database connection: {}", e.getMessage());
        }
    }

    /**
     * Mask sensitive parts of database URL
     */
    private String maskUrl(String url) {
        if (url == null) return "N/A";
        // Mask host, port, and database name
        return url.replaceAll("@[^:]+:[0-9]+:[^:]+", "@***:***:***");
    }

    /**
     * Mask username (show only first 2 characters)
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return "***";
        return username.substring(0, 2) + "***";
    }
}
