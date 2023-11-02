package main.java.de.voidtech.gerald.persistence;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Map;
import java.util.Set;

public class CustomPhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private static final long serialVersionUID = 1L;

    public static final CustomPhysicalNamingStrategy INSTANCE = new CustomPhysicalNamingStrategy();

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return new Identifier(name.getText().toLowerCase(), name.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        String lowerName = name.getText().toLowerCase();
        return new Identifier(lowerName.replace("_", ""), name.isQuoted());
    }
}