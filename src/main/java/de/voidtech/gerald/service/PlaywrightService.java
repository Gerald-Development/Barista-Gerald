package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

//@Service
public class PlaywrightService {
	
	@Autowired
	private ThreadManager threadManager;
	
	private static final Logger LOGGER = Logger.getLogger(PlaywrightService.class.getName());
	private BrowserContext browser = null;
	
	private NewContextOptions getContextOptions() {
		return new Browser.NewContextOptions()
				.setViewportSize(1000, 1000)
				.setLocale("en-GB");
	}
	
	//@EventListener(ApplicationReadyEvent.class)
	private void initialisePlaywright() {
		ExecutorService playwrightExecutor = threadManager.getThreadByName("Playwright");
		playwrightExecutor.execute(new Runnable() {
			@Override
			public void run() {
				LOGGER.log(Level.INFO, "Playwright is being initialised");
				Browser browserInstance = Playwright.create().firefox().launch();
				browser = browserInstance.newContext(getContextOptions());
				LOGGER.log(Level.INFO, "Playwright is ready!");
			}
		});
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
