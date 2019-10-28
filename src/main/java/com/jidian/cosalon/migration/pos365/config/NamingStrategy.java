package com.jidian.cosalon.migration.pos365.config;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier,
        JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier,
        JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(identifier);
    }

    private Identifier convertToSnakeCase(Identifier id) {
        if (id == null || StringUtils.isBlank(id.getText())) {
            return id;
        }
        String snakeCase = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, id.getText());
        return Identifier.toIdentifier(snakeCase);
    }
}
