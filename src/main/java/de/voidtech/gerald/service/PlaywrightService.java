package main.java.de.voidtech.gerald.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class PlaywrightService {
	
	private static final Logger LOGGER = Logger.getLogger(PlaywrightService.class.getName());
	private BrowserContext browser = null;
	
	private NewContextOptions getContextOptions() {
		return new Browser.NewContextOptions()
				.setLocale("en-GB")
				.setViewportSize(1000, 1000);
	}
	
	public PlaywrightService() {
		LOGGER.log(Level.INFO, "Playwright is being initialised");
		Browser browserInstance = Playwright.create().firefox().launch();
		this.browser = browserInstance.newContext(getContextOptions());
		LOGGER.log(Level.INFO, "Playwright is ready!");
	}
	
	public BrowserContext getBrowser() {
		return this.browser;
	}
	
	public byte[] screenshotPage(String url, int width, int height) {
		Page screenshotPage = getBrowser().newPage();
		screenshotPage.navigate(url);
		screenshotPage.setViewportSize(width, height);
		byte[] screenshotBytesBuffer = screenshotPage.screenshot();
		screenshotPage.close();	
		
		return screenshotBytesBuffer;
	}
}
