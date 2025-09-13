package com.wtf.app.config;

import com.wtf.app.commons.InitialSetup;
import com.wtf.app.model.dto.FacebookDTO;
import com.wtf.app.model.dto.TelegramDTO;
import com.wtf.app.model.dto.WhatsappDTO;
import com.wtf.app.model.enums.SocialType;
import com.wtf.app.model.xpaths.XPathInterfaceWATG;
import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.service.impls.WATGBroadcastMessageToAllGroupsService;
import com.wtf.app.service.impls.WATGFetchAllWAContactsWGroupNamesService;
import com.wtf.app.util.AutomationContextTaskDecorator;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import com.wtf.app.web.driver.WebDriverHealthService;
import jakarta.inject.Provider;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@Configuration
@Import({
        WebConfig.class,
})
@EnableAsync
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final AutomationContextTaskDecorator taskDecorator;

    @Autowired
    private ParallelWebDriverManager webDriverManager;

    public AppConfig(AutomationContextTaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }

    /* @Bean
     @Lazy
     public ParallelWebDriverManager webDriverManager() {
         return new ParallelWebDriverManager();
     }
    */

    @Bean
    public ChromeOptions chromeOptions1() {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return options;
    }

    @Bean
    @Qualifier("baseURLTG")
    public String telegramBaseUrl() {
        return "https://web.telegram.org/";
    }

    @Bean
    public InitialSetup initialSetup(
            @Qualifier("webDriver") Provider<WebDriver> webDriverProvider,
            Provider<JavascriptExecutor> javascriptExecutorProvider,
            WhatsappDTO whatsappDTO,
            TelegramDTO telegramDTO,
            FacebookDTO facebookDTO) {

        return new InitialSetup(
                webDriverProvider,
                javascriptExecutorProvider,
                whatsappDTO,
                telegramDTO,
                facebookDTO
        );
    }

    @Bean
    public WhatsappDTO whatsappDTO() {
        return new WhatsappDTO();
    }

    @Bean(name = "telegramDTOConfig")
    public TelegramDTO telegramDTOConfig(
            @Value("${telegram.social.type:TELEGRAM}") String socialType,
            @Value("${telegram.profile:telegram-profile}") String profile) {

        // Initialize with actual implementations from TelegramConfig
        XPathInterfaceWATG xpathWatg = new com.wtf.app.model.xpaths.Telegram_Xpaths();

        return new TelegramDTO(
                xpathWatg,
                xpathWatg,  // Telegram_Xpaths implements both interfaces
                SocialType.valueOf(socialType),
                profile
        );
    }

    @Bean
    public FacebookDTO facebookDTO() {
        return new FacebookDTO();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Qualifier("whatsAppFetchService")
    public WATGFetchAllWAContactsWGroupNamesService whatsAppFetchService(InitialSetup initialSetup,
                                                                         ParallelWebDriverManager webDriverManager) {
        return new WATGFetchAllWAContactsWGroupNamesService(initialSetup, webDriverManager);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Qualifier("telegramFetchService")
    public WATGFetchAllWAContactsWGroupNamesService telegramFetchService(InitialSetup initialSetup,
                                                                         ParallelWebDriverManager webDriverManager) {
        return new WATGFetchAllWAContactsWGroupNamesService(initialSetup, webDriverManager);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Qualifier("whatsAppBroadcastService")
    public WATGBroadcastMessageToAllGroupsService whatsAppBroadcastService(InitialSetup initialSetup,
                                                                           ParallelWebDriverManager webDriverManager,
                                                                           WebDriverHealthService webDriverHealthService,
                                                                           WhatsappDBFileRepository whatsappDBFileRepository) {
        return new WATGBroadcastMessageToAllGroupsService(initialSetup, webDriverManager, webDriverHealthService, whatsappDBFileRepository);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Qualifier("telegramBroadcastService")
    public WATGBroadcastMessageToAllGroupsService telegramBroadcastService(InitialSetup initialSetup,
                                                                           ParallelWebDriverManager webDriverManager,
                                                                           WebDriverHealthService webDriverHealthService,
                                                                           WhatsappDBFileRepository whatsappDBFileRepository) {
        return new WATGBroadcastMessageToAllGroupsService(initialSetup, webDriverManager, webDriverHealthService, whatsappDBFileRepository);
    }

    @Bean(name = "automationTaskExecutor")
    @Primary
    public ThreadPoolTaskExecutor automationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-exec-");
        executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }

}
