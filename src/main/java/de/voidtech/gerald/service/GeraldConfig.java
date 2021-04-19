package main.java.de.voidtech.gerald.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
public class GeraldConfig {
	private static final Logger LOGGER = Logger.getLogger(GeraldConfig.class.getName());

	private final Properties config = new Properties();

	//PRIVATE FOR SINGLETON
	public GeraldConfig() {
		try (FileInputStream fis = new FileInputStream(new File("GeraldConfig.properties"))){
			config.load(fis);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "an error has occurred while reading the config\n" + e.getMessage());
		}
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
		return dbURL != null ? dbURL : "jdbc:postgresql://localhost:5432/baristadb";
	}
	
	public List<String> getMasters()
	{
		if(config.getProperty("masters") == null) return Arrays.asList("275355515003863040", "497341083949465600", "217567653210882049");
		
		List<String> masters = Arrays.asList(StringUtils.split(config.getProperty("masters"), ','));
		return masters;
	}

	public String getAIMLFolderDirectory() {
		String dir = config.getProperty("AIMLDirectory"); 
		return dir != null ? dir : "AIML";
	}
}