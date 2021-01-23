package main.java.de.voidtech.gerald.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigService {
	private static volatile ConfigService instance;
	private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());

	private final Properties config = new Properties();

	//PRIVATE FOR SINGLETON
	private ConfigService() {
		try (FileInputStream fis = new FileInputStream(new File("GeraldConfig.properties"))){
			config.load(fis);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "an error has occurred while reading the config\n" + e.getMessage());
		}
	}

	public static ConfigService getInstance() {
		if (ConfigService.instance == null) {
			ConfigService.instance = new ConfigService();
		}
		return ConfigService.instance;
	}

	public String getToken() {
		return config.getProperty("token");
	}

	public String getDefaultPrefix() {
		String prefix = config.getProperty("defaultPrefix");
		return prefix != null ? prefix : "$";
	}
	
	public String getHibernateDialect()
	{
		String dialect = config.getProperty("hibernate.Dialect");
		return dialect != null ? dialect : "org.hibernate.dialect.PostgreSQLDialect";
	}
	
	public String getDriver()
	{
		String driver = config.getProperty("hibernate.Driver");
		return driver != null ? driver : "org.postgresql.Driver";
	}
	
	public String getDBUser()
	{
		String user = config.getProperty("hibernate.User");
		return user != null ? user : "postgres";
	}
	
	public String getDBPassword()
	{
		String pass = config.getProperty("hibernate.Password");
		return pass != null ? pass : "root";
	}
	
	public String getConnectionURL()
	{
		String dbURL = config.getProperty("hibernate.ConnectionURL");
		return dbURL != null ? dbURL : "jdbc:postgresql://localhost:5432/BaristaDB";
	}
}