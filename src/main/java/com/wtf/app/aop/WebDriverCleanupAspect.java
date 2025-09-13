package com.wtf.app.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wtf.app.web.driver.WebDriverConfig;

/**
 * Aspect for automatically cleaning up WebDriver instances after controller methods complete.
 */
@Aspect
@Component
public class WebDriverCleanupAspect {
    
    private static final Logger log = LoggerFactory.getLogger(WebDriverCleanupAspect.class);
    
    //private final WebDriverConfig webDriverConfig;
    
   /* @Autowired
    public WebDriverCleanupAspect(WebDriverConfig webDriverConfig) {
        this.webDriverConfig = webDriverConfig;
    }
    */
    /**
     * Around advice that wraps controller methods to ensure WebDriver cleanup.
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController) && " +
            "execution(public * com.wtf.app.controller..*.*(..))")
    public Object aroundControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Proceed with the method execution
            return joinPoint.proceed();
        } finally {
            // After the method completes, ensure the WebDriver is cleaned up
            Object target = joinPoint.getTarget();
            try {
                // Use reflection to get the WebDriver field if it exists
                java.lang.reflect.Field[] fields = target.getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    if (WebDriver.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        WebDriver driver = (WebDriver) field.get(target);
                        if (driver != null) {
                            log.debug("Cleaning up WebDriver instance from {}.{}", 
                                    target.getClass().getSimpleName(), 
                                    joinPoint.getSignature().getName());
                            driver.quit();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error during WebDriver cleanup", e);
            }
        }
    }

    @Around("execution(* com.wtf.app.service..*.*(..))")
    public Object monitorService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Method {} executed in {} ms", methodName, duration);
            return result;
        } catch (Exception e) {
            log.error("Error in method {}: {}", methodName, e.getMessage(), e);
            throw e;
        }
    }

}
