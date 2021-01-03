package test.java.de.voidtech.gerald.service;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.Test;

import junit.framework.TestCase;
import main.java.de.voidtech.gerald.service.ConfigService;

public class ConfigServiceTest extends TestCase
{
	@Test
	public void testDefault() throws Exception
	{
		ConfigService configService = ConfigService.getInstance();
		
		Properties config = new Properties();
		config.setProperty("token", "test");
		config.setProperty("defaultPrefix", "%!!");
		
		Field field = configService.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(configService, config);
		
		assertEquals(configService.getToken(), "test");
		assertEquals(configService.getDefaultPrefix(), "%!!");
	}
	
	@Test
	public void testNoConfigFile() throws Exception {
		ConfigService configService = ConfigService.getInstance();
		
		Properties config = new Properties();
		
		Field field = configService.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(configService, config);
		
		assertEquals(configService.getDefaultPrefix(), "$");
		assertEquals(configService.getToken(), null);
	}
}
