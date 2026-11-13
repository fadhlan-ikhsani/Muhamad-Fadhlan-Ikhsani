package com.pesanyuk.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Konfigurasi inti Spring: component scan, DataSource, JdbcTemplate, dan
 * transaction manager. DAO yang diberi anotasi @Repository akan dibuat dan
 * dikelola oleh Spring.
 */
@Configuration
@ComponentScan(basePackages = "com.pesanyuk")
@EnableTransactionManagement
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        Properties config = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException("File db.properties tidak ditemukan.");
            }
            config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Gagal membaca db.properties: "
                    + ex.getMessage(), ex);
        }

        String host = config.getProperty("db.host", "localhost").trim();
        String port = config.getProperty("db.port", "3306").trim();
        String nama = config.getProperty("db.name", "pesanyuk_db").trim();
        String params = config.getProperty("db.params", "").trim();
        String user = baca("PESANYUK_DB_USER",
                config.getProperty("db.user", "root"));
        String password = baca("PESANYUK_DB_PASSWORD",
                config.getProperty("db.password", ""));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + nama
                + (params.isEmpty() ? "" : "?" + params));
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private static String baca(String env, String bawaan) {
        String nilai = System.getenv(env);
        return nilai == null || nilai.isBlank() ? bawaan : nilai;
    }
}
