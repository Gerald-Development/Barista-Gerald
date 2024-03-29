package main.java.de.voidtech.gerald.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Geolocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PlaywrightService {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
    private static final Logger LOGGER = Logger.getLogger(PlaywrightService.class.getSimpleName());
    private static BrowserContext browser = null;
    @Autowired
    private MultithreadingService multithreadingService;

    private NewContextOptions getContextOptions() {
        return new Browser.NewContextOptions()
                .setViewportSize(1000, 1000)
                .setAcceptDownloads(false)
                .setGeolocation(new Geolocation(51.5072, 0.1276))
                .setUserAgent(USER_AGENT)
                .setLocale("en-GB");
    }

    @EventListener(ApplicationReadyEvent.class)
    private void initialisePlaywright() {
        ExecutorService playwrightExecutor = multithreadingService.getThreadByName("Playwright");
        playwrightExecutor.execute(() -> {
            LOGGER.log(Level.INFO, "Playwright is being initialised");
            Browser browserInstance = Playwright.create().firefox().launch();
            browser = browserInstance.newContext(getContextOptions());
            LOGGER.log(Level.INFO, "Playwright is ready!");
        });
    }

    public BrowserContext getBrowser() {
        return browser;
    }

    public byte[] screenshotPage(String url, int width, int height) {
        Page screenshotPage = getBrowser().newPage();
        screenshotPage.setExtraHTTPHeaders(getHttpHeaders());
        screenshotPage.navigate(url);
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