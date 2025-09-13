package com.wtf.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestContextListener;
import com.wtf.app.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

@SpringBootApplication(
    scanBasePackages = {
        "com.wtf.app",
        "com.wtf.app.service.impls.starter.service.whatsApp",
        "com.wtf.app.service.impls.starter.service.zTelegram",
        "com.wtf.app.commons",
        "com.wtf.app.util",
        "com.wtf.app.model.config"
    }
)
@EnableAsync(proxyTargetClass = true)
@Import(AppConfig.class)
public class WaTgFbSeleniumFreelancerAppV2Application {
    private static final Logger logger = LogManager.getLogger(WaTgFbSeleniumFreelancerAppV2Application.class);

    private static final SimpleDateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    public static void main(String[] args) {
        // Set a unique session ID for logging
        String sessionId = "session_" + System.currentTimeMillis();
        System.setProperty("log.session.id", sessionId);
        
        // Create logs directory if it doesn't exist
        new File("logs").mkdirs();
        
        // Now start the application with the session-based logging
        SpringApplication.run(WaTgFbSeleniumFreelancerAppV2Application.class, args);
    }
}
