package com.wtf.app.app.commons;

import com.wtf.app.SimpleMessageFormatter;
import com.wtf.app.app.enums.SocialType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.logging.*;

public class InitialSetup {

	private static InitialSetup initialSetup;
	private static LogManager lgmngr = LogManager.getLogManager();
	private static Logger logger = lgmngr.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static WebDriver driver = null;

	private InitialSetup(String baseUrlFromXpath, SocialType socialType) {
		try {
			executeInitialSetUp(baseUrlFromXpath, socialType);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static InitialSetup getInstance(String baseUrlFromXpath, SocialType socialType) {
		if (initialSetup == null) {
			initialSetup = new InitialSetup(baseUrlFromXpath, socialType);
		}
		return initialSetup;
	}

	public void executeInitialSetUp(String baseUrlFromXpath, SocialType socialType) throws InterruptedException {
		initializeLogger();
		driver = setupDriver(baseUrlFromXpath, socialType);
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(200));
		System.out.println("Initial Setup is completed. Waiting for Pinned WebElement");

	}

	public static void initializeLogger() {
		FileHandler fileHandler;
		try {
			logger.setLevel(Level.FINE);

			 /* Custom logger removing the default logger with date time pkg class*/

			// This block configure the logger with handler and formatter
			fileHandler = new FileHandler("D:/Eclipse-workspace/WA_TG_FB_SeleniumFreelancerApp/logs/WhatsApp.log",true);
            SimpleMessageFormatter formatter = new SimpleMessageFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
			// logger.setUseParentHandlers(false);

            // Remove default console handler (optional)
            Handler[] handlers = logger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                	logger.removeHandler(handler);
                }
            }

			// the following statement is used to log any messages
			logger.fine("Starting Logging....");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected WebDriver setupDriver(String baseUrlFromXpath, SocialType socialType) throws InterruptedException {

		driver = new ChromeDriver(getHumanLikeOptions());
		String baseUrl = baseUrlFromXpath;
		driver.get(baseUrl);
		if(socialType == SocialType.FACEBOOK) {
			Thread.sleep(30000);
		}else if(socialType == SocialType.TELEGRAM) {
		   Thread.sleep(20000);
		}else {
			Thread.sleep(180000);//Make highest for WA: pass additional enum for app to decide the timeout
		}
		logger.fine("SocialType :: "+socialType);

		return driver;

	}

	public static ChromeOptions getHumanLikeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Common arguments for a more "human" appearance
        options.addArguments("--start-maximized");
        options.addArguments("--window-size=1920,1080"); // Or other common resolutions
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--incognito");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"); // Example user agent
        options.addArguments("--lang=en-US");

        // Potentially helpful, but consider the implications
		//options.addArguments("--headless=new");//remove if any issues
        // options.addArguments("--disable-gpu");
        // options.addArguments("--no-sandbox"); // Use with caution

        // Experimental options
        options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // To use a custom user profile (replace with your profile path)
        // options.addArguments("user-data-dir=/path/to/your/custom/profile");

        /*Own Features */
		Path path = Paths.get("src/resources/extensions/webextensions-selenium-example.crx");//
		File extensionFilePath = new File(path.toUri());
		options.addExtensions(extensionFilePath);

		/*
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);

		options.merge(capabilities);
		*/

		return options;
    }

	public static Logger getLogger() {
		return logger;
	}

	public WebDriver getDriver() {
		return driver;
	}

}
