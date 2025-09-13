package com.wtf.app.a_runner;

import com.wtf.app.service.impls.starter.service.whatsApp.SocialMediaParentRunner;
import com.wtf.app.model.dto.SocialModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Base class for all social media automation starters.
 * Provides common dependencies and functionality for social media automation tasks.
 */
public abstract class SocialMediaAppStarter {
    protected static final Logger logger = LogManager.getLogger(SocialMediaAppStarter.class);
    
    protected final SocialMediaParentRunner socialMediaParentRunner;
    protected final SocialModel whatsappDTO;
    protected final SocialModel telegramDTO;
    protected final SocialModel facebookDTO;

    /**
     * Constructs a new SocialMediaAppStarter with required dependencies.
     * 
     * @param socialMediaParentRunner The runner for executing social media tasks
     * @param whatsappDTO DTO for WhatsApp configuration
     * @param telegramDTO DTO for Telegram configuration
     * @param facebookDTO DTO for Facebook configuration
     * @throws IllegalArgumentException if any of the required dependencies are null
     */
    @Autowired
    protected SocialMediaAppStarter(
            SocialMediaParentRunner socialMediaParentRunner,
            SocialModel whatsappDTO,
            SocialModel telegramDTO,
            SocialModel facebookDTO) {
        
        Assert.notNull(socialMediaParentRunner, "SocialMediaParentRunner must not be null");
        Assert.notNull(whatsappDTO, "WhatsApp DTO must not be null");
        Assert.notNull(telegramDTO, "Telegram DTO must not be null");
        Assert.notNull(facebookDTO, "Facebook DTO must not be null");
        
        this.socialMediaParentRunner = socialMediaParentRunner;
        this.whatsappDTO = whatsappDTO;
        this.telegramDTO = telegramDTO;
        this.facebookDTO = facebookDTO;
        
        logger.debug("SocialMediaAppStarter initialized with all required dependencies");
    }
    
    /**
     * Validates that all required dependencies are properly initialized.
     * 
     * @throws IllegalStateException if any required dependency is not initialized
     */
    protected void validateDependencies() {
        if (socialMediaParentRunner == null) {
            throw new IllegalStateException("socialMediaParentRunner is not initialized");
        }
        if (whatsappDTO == null) {
            throw new IllegalStateException("whatsappDTO is not initialized");
        }
        if (telegramDTO == null) {
            throw new IllegalStateException("telegramDTO is not initialized");
        }
        if (facebookDTO == null) {
            throw new IllegalStateException("facebookDTO is not initialized");
        }
    }
}
