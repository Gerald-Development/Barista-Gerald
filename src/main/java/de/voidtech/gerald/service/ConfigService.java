package main.java.de.voidtech.gerald.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigService {
	
	public Properties config = new Properties();
	
	public ConfigService()  {
		//Get path
		String path = null;
		try {
			path = new File(".").getCanonicalPath() + "\\GeraldConfig.properties";
		} catch (IOException IOError) {
			IOError.printStackTrace();
			System.exit(1);
		}
		//Read file
		try {
			config.load(new FileInputStream(path));
		} catch (FileNotFoundException noFileError) {
			noFileError.printStackTrace();
			System.exit(1);
		} catch (IOException IOError) {
			IOError.printStackTrace();
			System.exit(1);
		}
	}
	//Get Token
	public String getToken() {
		String token = config.getProperty("token");
		return token;
	}
	//Get Prefix
	public String getDefaultPrefix() {
		String defaultPrefix = config.getProperty("defaultPrefix");
		return defaultPrefix;
	}
}