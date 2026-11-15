package com.mindsmart.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/mindsmart_db}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String mysqlDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            HikariConfig mysqlConfig = new HikariConfig();
            mysqlConfig.setJdbcUrl(mysqlUrl);
            mysqlConfig.setUsername(mysqlUsername);
            mysqlConfig.setPassword(mysqlPassword);
            mysqlConfig.setDriverClassName(mysqlDriver);
            mysqlConfig.setConnectionTimeout(2500);
            mysqlConfig.setInitializationFailTimeout(2500);
            mysqlConfig.setMaximumPoolSize(10);

            HikariDataSource mysqlDs = new HikariDataSource(mysqlConfig);
            try (Connection conn = mysqlDs.getConnection()) {
                logger.info("Successfully connected to MySQL database at: {}", mysqlUrl);
                return mysqlDs;
            }
        } catch (Exception e) {
            logger.warn("===============================================================================");
            logger.warn("MySQL connection check failed: {}", e.getMessage());
            logger.warn("MindSmart is engaging fallback database resilience (Embedded H2).");
            logger.warn("All tables, REST endpoints, and demo data are fully initialized.");
            logger.warn("To use your local MySQL, set 'spring.datasource.password' in application.properties");
            logger.warn("===============================================================================");

            HikariConfig h2Config = new HikariConfig();
            h2Config.setJdbcUrl("jdbc:h2:file:./data/mindsmart_db;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER");
            h2Config.setUsername("sa");
            h2Config.setPassword("");
            h2Config.setDriverClassName("org.h2.Driver");
            h2Config.setMaximumPoolSize(10);

            return new HikariDataSource(h2Config);
        }
    }
}
