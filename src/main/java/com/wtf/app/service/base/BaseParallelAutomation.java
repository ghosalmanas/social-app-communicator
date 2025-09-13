package com.wtf.app.service.base;

import com.wtf.app.repository.WhatsappDBFileRepository;
import com.wtf.app.service.impls.starter.service.whatsApp.WhatsappBroadcastSelfSkillsToWAGroupsStarter;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Constructor;

/**
 * Base class for parallel automation services.
 */
public abstract class BaseParallelAutomation {
    protected static final Logger logger = LoggerFactory.getLogger(BaseParallelAutomation.class);

    @Autowired(required = false)
    @Lazy
    protected WhatsappDBFileRepository whatsappDBFileRepository;
    
    @Autowired
    protected ParallelWebDriverManager webDriverManager;

    protected final String profileName;
    protected final ThreadPoolTaskExecutor taskExecutor;
    protected final ApplicationContext applicationContext;
    
    /**
     * Constructor for dependency injection
     * @param taskExecutor The task executor for async operations
     * @param applicationContext The Spring application context
     */
    protected BaseParallelAutomation(ThreadPoolTaskExecutor taskExecutor, 
                                   ApplicationContext applicationContext) {
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
        this.profileName = getClass().getSimpleName().toLowerCase().replace("service", "");
    }
    
    /**
     * Constructor for backward compatibility
     * @param profileName The profile name for this automation service
     */
    protected BaseParallelAutomation(String profileName) {
        this.profileName = profileName;
        this.taskExecutor = null;
        this.applicationContext = null;
    }

    /**
     * Gets a service instance from the Spring application context, ensuring proper RequestContext handling.
     * This method is thread-safe and handles both web requests and background threads.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getServiceInstance(Class<T> serviceClass, WebDriver driver) {

        try {
            // Try to get the bean from the application context first
            if (applicationContext != null) {
                try {
                    // Special handling for services that need constructor injection
                    if (WhatsappBroadcastSelfSkillsToWAGroupsStarter.class.isAssignableFrom(serviceClass)) {
                        return (T) applicationContext.getBean("whatsappBroadcastSelfSkillsToWAGroupsStarter");
                    }
                    
                    // For other services, try to get from context
                    T service = applicationContext.getBean(serviceClass);
                    if (service instanceof WebDriverAware) {
                        ((WebDriverAware) service).setWebDriver(driver);
                    }
                    return service;
                } catch (Exception e) {
                    logger.warn("Could not get service from application context, falling back to reflection", e);
                }
            }
            
            // Fall back to reflection if application context is not available
            Constructor<?>[] constructors = serviceClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == 1 && WebDriver.class.isAssignableFrom(paramTypes[0])) {
                    return (T) constructor.newInstance(driver);
                }
            }
            
            // If no suitable constructor found, try default constructor
            return serviceClass.getDeclaredConstructor().newInstance();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create service instance: " + serviceClass.getName(), e);
        } finally {
            // Ensure the driver is closed
            driver.quit();
    }
    }
}
