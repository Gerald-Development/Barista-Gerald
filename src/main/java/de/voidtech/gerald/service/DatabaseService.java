package main.java.de.voidtech.gerald.service;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import main.java.de.voidtech.gerald.entities.GeraldLogger;

@Service
@EnableTransactionManagement
@org.springframework.context.annotation.Configuration
public class DatabaseService 
{
	private static final GeraldLogger LOGGER = LogService.GetLogger(DatabaseService.class.getSimpleName());
	
	@Autowired
	private GeraldConfig config;
	
	@Bean("sessionFactory")
	@Order(2)
	public SessionFactory getSessionFactory() 
	{
		SessionFactory sessionFactory = null;
			try 
			{
				exportSchema();
				Properties hibernateProperties = getHibernateProperties();
				Configuration hibernateConfig = new Configuration();
				getAllEntities().forEach(hibernateConfig::addAnnotatedClass);
				hibernateConfig.setProperties(hibernateProperties);
				
				sessionFactory = hibernateConfig.buildSessionFactory();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "An error has occurred while setting up Hibernate SessionFactory:\n" + e.getMessage());
			}

		return sessionFactory;
	}
	
	private void exportSchema() {
		Properties hbnProperties = getHibernateProperties();
		
		MetadataSources metadataSources = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(hbnProperties).build());
		
		Set<Class<?>> annotated = new Reflections("main.java.de.voidtech.gerald").getTypesAnnotatedWith(Entity.class);
		annotated.forEach(metadataSources::addAnnotatedClass);
		
		new SchemaUpdate()
			.setFormat(true)
			.execute(EnumSet.of(TargetType.DATABASE), metadataSources.buildMetadata());
	}
	
	private Properties getHibernateProperties()
	{
		Properties properties = new Properties();
		properties.put(Environment.DRIVER, config.getDriver());
		properties.put(Environment.URL, config.getConnectionURL());
		properties.put(Environment.USER, config.getDBUser());
		properties.put(Environment.PASS, config.getDBPassword());
		properties.put(Environment.DIALECT, config.getHibernateDialect());
		
		return properties;
	}
	
	private Set<Class<?>> getAllEntities()
	{
		return new Reflections("main.java.de.voidtech.gerald").getTypesAnnotatedWith(Entity.class);
	}
}
