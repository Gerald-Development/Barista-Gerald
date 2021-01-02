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

	private Properties config = new Properties();

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
		return config.getProperty("defaultPrefix");
	}
}