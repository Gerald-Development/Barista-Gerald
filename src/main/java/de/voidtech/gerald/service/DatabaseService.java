package main.java.de.voidtech.gerald.service;

import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Service
@EnableTransactionManagement
@org.springframework.context.annotation.Configuration
@EnableSpringConfigured
public class DatabaseService 
{
	private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
	
	@Autowired
	private GeraldConfig configService;
	
	@Bean
	public SessionFactory getSessionFactory() 
	{
		SessionFactory sessionFactory = null;
			try 
			{
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
	
	private Properties getHibernateProperties()
	{
		Properties properties = new Properties();
		properties.put(Environment.DRIVER, configService.getDriver());
		properties.put(Environment.URL, configService.getConnectionURL());
		properties.put(Environment.USER, configService.getDBUser());
		properties.put(Environment.PASS, configService.getDBPassword());
		properties.put(Environment.DIALECT, configService.getHibernateDialect());
		
		return properties;
	}
	
	private Set<Class<?>> getAllEntities()
	{
		return new Reflections("main.java.de.voidtech.gerald").getTypesAnnotatedWith(Entity.class);
	}
}
