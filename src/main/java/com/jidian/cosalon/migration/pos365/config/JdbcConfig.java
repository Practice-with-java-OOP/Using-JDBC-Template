package com.jidian.cosalon.migration.pos365.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class JdbcConfig {

    @Bean(name = "db1")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource1(Environment env) {
        return buildDataSource(env, "spring.datasource");
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate1(@Qualifier("db1") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "db2")
    @ConfigurationProperties(prefix = "spring.datasource2")
    public DataSource dataSource2(Environment env) {
        return buildDataSource(env, "spring.datasource2");
    }

    @Bean(name = "db3")
    @ConfigurationProperties(prefix = "spring.datasource3")
    public DataSource dataSource3(Environment env) {
        return buildDataSource(env, "spring.datasource3");
    }

    @Bean(name = "omsJdbcTemplate")
    public JdbcTemplate jdbcTemplate2(@Qualifier("db2") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "upmsJdbcTemplate")
    public JdbcTemplate jdbcTemplate3(@Qualifier("db3") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    private DataSource buildDataSource(Environment env, String prop) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty(prop + ".driver-class-name"));
        dataSource.setUrl(env.getProperty(prop + ".url"));
        dataSource.setUsername(env.getProperty(prop + ".username"));
        dataSource.setPassword(env.getProperty(prop + ".password"));

        return dataSource;
    }
}
