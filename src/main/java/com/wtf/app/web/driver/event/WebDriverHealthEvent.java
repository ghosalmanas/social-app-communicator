package com.wtf.app.web.driver.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a WebDriver session health status changes.
 */
public class WebDriverHealthEvent extends ApplicationEvent {
    private final String profileName;
    private final boolean isHealthy;
    private final String message;

    public WebDriverHealthEvent(Object source, String profileName, boolean isHealthy, String message) {
        super(source);
        this.profileName = profileName;
        this.isHealthy = isHealthy;
        this.message = message;
    }

    public String getProfileName() {
        return profileName;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "WebDriverHealthEvent{" +
                "profileName='" + profileName + '\'' +
                ", isHealthy=" + isHealthy +
                ", message='" + message + '\'' +
                ", source=" + source +
                '}';
    }
}
