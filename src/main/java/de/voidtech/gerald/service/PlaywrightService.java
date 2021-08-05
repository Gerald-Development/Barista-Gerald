package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.Map;
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
		screenshotPage.setExtraHTTPHeaders(getHttpHeaders());
		screenshotPage.navigate(url);
		if (screenshotPage.querySelector("#L2AGLb > div") != null) screenshotPage.querySelector("#L2AGLb > div").click(); 
		screenshotPage.setViewportSize(width, height);
		byte[] screenshotBytesBuffer = screenshotPage.screenshot();
		screenshotPage.close();	
		
		return screenshotBytesBuffer;
	}

	private Map<String, String> getHttpHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept-Language", "en");
		return headers;
	}
}
