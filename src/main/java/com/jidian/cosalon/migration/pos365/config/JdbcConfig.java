package com.jidian.cosalon.migration.pos365.config;

import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.jidian.cosalon.migration.pos365.domainpos365")
public class JdbcConfig {

    @Primary
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

    @Bean(name = "db4")
    @ConfigurationProperties(prefix = "spring.datasource4")
    public DataSource dataSource4(Environment env) {
        return buildDataSource(env, "spring.datasource4");
    }

    @Bean(name = "db5")
    @ConfigurationProperties(prefix = "spring.datasource5")
    public DataSource dataSource5(Environment env) {
        return buildDataSource(env, "spring.datasource5");
    }

    @Bean(name = "omsJdbcTemplate")
    public JdbcTemplate jdbcTemplate2(@Qualifier("db2") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "upmsJdbcTemplate")
    public JdbcTemplate jdbcTemplate3(@Qualifier("db3") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "bhairJdbcTemplate")
    public JdbcTemplate jdbcTemplate4(@Qualifier("db4") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "amsJdbcTemplate")
    public JdbcTemplate jdbcTemplate5(@Qualifier("db5") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder, @Qualifier("db1") DataSource dataSource) {
        HashMap<String, Object> properties = new HashMap<>();
        properties
            .put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        properties
            .put("hibernate.hbm2ddl", "auto=update");
        return builder
            .dataSource(dataSource)
            .properties(properties)
            .packages("com.jidian.cosalon.migration.pos365.domainpos365")
            .build();
    }

    private DataSource buildDataSource(Environment env, String prop) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty(prop + ".driver-class-name"));
        dataSource.setUrl(env.getProperty(prop + ".url"));
        dataSource.setUsername(env.getProperty(prop + ".username"));
        dataSource.setPassword(env.getProperty(prop + ".password"));

        return dataSource;
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
        @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
