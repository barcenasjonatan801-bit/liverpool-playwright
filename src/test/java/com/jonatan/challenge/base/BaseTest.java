package com.jonatan.challenge.base;

import com.jonatan.challenge.reporting.ScreenshotOnFailureExtension;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseTest {

    protected static final String BASE_URL =
            System.getProperty(
                    "baseUrl",
                    "https://www.liverpool.com.mx/"
            );

    private Playwright playwright;
    private Browser browser;

    protected BrowserContext context;
    protected Page page;

    public Page getPage() {
        return page;
    }

    @BeforeAll
    void launchBrowser() {
        boolean headless = Boolean.parseBoolean(
                System.getProperty("headless", "true")
        );

        String browserName = System.getProperty(
                "browser",
                "firefox"
        ).trim().toLowerCase();

        String browserChannel = System.getProperty(
                "browserChannel",
                ""
        ).trim();

        playwright = Playwright.create();

        BrowserType browserType = switch (browserName) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            case "chromium" -> playwright.chromium();
            default -> throw new IllegalArgumentException(
                    "Navegador no soportado: " + browserName
            );
        };

        BrowserType.LaunchOptions launchOptions =
                new BrowserType.LaunchOptions()
                        .setHeadless(headless);

        if (!browserChannel.isBlank()) {
            if (!browserName.equals("chromium")) {
                throw new IllegalArgumentException(
                        "browserChannel solamente puede utilizarse con Chromium"
                );
            }

            launchOptions.setChannel(browserChannel);
        }

        browser = browserType.launch(launchOptions);

        System.out.println("Navegador: " + browserName);
        System.out.println("Modo headless: " + headless);
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1440, 900)
                        .setLocale("es-MX")
        );

        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    void closeBrowser() {
        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }
}