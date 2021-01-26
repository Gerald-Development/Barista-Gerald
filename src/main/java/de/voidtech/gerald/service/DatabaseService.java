package main.java.de.voidtech.gerald.service;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;

public final class DatabaseService 
{
	private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
	private static DatabaseService instance;
	private ConfigService configService;
	private SessionFactory rootSessionFactory;
	
	private DatabaseService()
	{
		this.configService = ConfigService.getInstance();
	}
	
	public static DatabaseService getInstance() {
        if (DatabaseService.instance == null) {
        	DatabaseService.instance = new DatabaseService();
        }
        return DatabaseService.instance;
    }
	
	public SessionFactory getSessionFactory() {
		if (this.rootSessionFactory == null) {

			try 
			{
				Properties hibernateProperties = getHibernateProperties();
				Configuration hibernateConfig = new Configuration();
				getAllEntities().forEach(hibernateConfig::addAnnotatedClass);
				hibernateConfig.setProperties(hibernateProperties);
				
				this.rootSessionFactory = hibernateConfig.buildSessionFactory();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "An error has occurred while setting up Hibernate SessionFactory:\n" + e.getMessage());
			}
		}

		return this.rootSessionFactory;
	}
	
	/**
	 * Exports the Schema directly to the Database.
	 * This drops all the data first and creates the DB from the new Schema.
	 * All existing data will be lost that way.
	 **/
	public void exportSchema()
	{
		Properties hbnProperties = getHibernateProperties();
		
		MetadataSources metadataSources = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(hbnProperties).build());
		
		Set<Class<?>> annotated = getAllEntities();
		annotated.forEach(metadataSources::addAnnotatedClass);
		
		//TODO: This is highly not good. Better export to a migration file and migrate the DB after it
		new SchemaUpdate()
			.setFormat(true)
			.execute(EnumSet.of(TargetType.DATABASE), metadataSources.buildMetadata());
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
