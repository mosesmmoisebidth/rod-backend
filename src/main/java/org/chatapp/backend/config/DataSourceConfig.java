package org.chatapp.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@Configuration
public class DataSourceConfig {

    private final Environment env;

    public DataSourceConfig(Environment env) {
        this.env = env;
    }

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        // Priority 1: Explicit JDBC env variables (e.g., Heroku-like)
        String jdbcUrl = env.getProperty("JDBC_DATABASE_URL");
        String jdbcUser = env.getProperty("JDBC_DATABASE_USERNAME");
        String jdbcPass = env.getProperty("JDBC_DATABASE_PASSWORD");

        if (StringUtils.hasText(jdbcUrl)) {
            return buildHikari(jdbcUrl, jdbcUser, jdbcPass);
        }

        // Priority 2: Railway/12-factor style DATABASE_URL parsing
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (StringUtils.hasText(databaseUrl)) {
            ParsedDbUrl parsed = parseDatabaseUrl(databaseUrl);
            if (parsed != null && StringUtils.hasText(parsed.jdbcUrl)) {
                return buildHikari(parsed.jdbcUrl, parsed.username, parsed.password);
            }
        }

        // Priority 3: Standard Spring properties (env variables or application.properties)
        String springUrl = env.getProperty("spring.datasource.url");
        String springUser = env.getProperty("spring.datasource.username");
        String springPass = env.getProperty("spring.datasource.password");
        if (StringUtils.hasText(springUrl)) {
            return buildHikari(springUrl, springUser, springPass);
        }

        // Last resort: let Boot try its auto-configuration
        // (This should rarely happen since we provide defaults in application.properties)
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        return new HikariDataSource(config);
    }

    private HikariDataSource buildHikari(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        if (StringUtils.hasText(username)) config.setUsername(username);
        if (StringUtils.hasText(password)) config.setPassword(password);
        config.setDriverClassName(Objects.requireNonNullElse(driverClassName, "org.postgresql.Driver"));
        // Reasonable defaults
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setPoolName("chatapp-hikari");
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

    private ParsedDbUrl parseDatabaseUrl(String url) {
        try {
            // Accept forms like: postgres://user:pass@host:port/db or postgresql://user:pass@host:port/db
            String normalized = url.replaceFirst("^postgres://", "postgresql://");
            URI uri = new URI(normalized);
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                username = parts.length > 0 ? parts[0] : null;
                password = parts.length > 1 ? parts[1] : null;
            }
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath(); // starts with "/db"
            String database = (path != null && path.length() > 1) ? path.substring(1) : null;

            if (!StringUtils.hasText(host) || port == -1 || !StringUtils.hasText(database)) {
                return null;
            }
            String jdbc = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            ParsedDbUrl parsed = new ParsedDbUrl();
            parsed.jdbcUrl = jdbc;
            parsed.username = username;
            parsed.password = password;
            return parsed;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static class ParsedDbUrl {
        String jdbcUrl;
        String username;
        String password;
    }
}
