package com.wtf.app.a_runner;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.model.dto.SocialModel;
import com.wtf.app.parent.SocialParentCommonUtils;
import com.wtf.app.service.impls.starter.service.whatsApp.SocialMediaParentRunner;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for all social media service starters.
 * Provides common functionality and enforces required constructor and method implementations.
 */
@Component
public abstract class SocialMediaServiceStarter extends SocialParentCommonUtils{
    
    protected final SocialMediaParentRunner socialMediaParentRunner;
    protected final SocialModel whatsappDTO;
    protected final SocialModel telegramDTO;
    protected final SocialModel facebookDTO;

    @Autowired
    public SocialMediaServiceStarter(
            
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager,
            SocialMediaParentRunner socialMediaParentRunner,
            SocialModel whatsappDTO,
            SocialModel telegramDTO,
            SocialModel facebookDTO) {
        super( initialSetup, parallelWebDriverManager);
        this.socialMediaParentRunner = socialMediaParentRunner;
        this.whatsappDTO = whatsappDTO;
        this.telegramDTO = telegramDTO;
        this.facebookDTO = facebookDTO;
    }

    /**
     * Entry point for the service. This method is called asynchronously.
     */
    /**
     * Entry point for the service. This method is called asynchronously.
     * @return CompletableFuture that completes when the operation is done
     */
    @Async
    public abstract CompletableFuture<Boolean> start(com.wtf.app.model.dto.AutomationContext context);

    /**
     * Template method that defines the group traversal logic.
     * Must be implemented by concrete service starters.
     *
     * @throws InterruptedException if the thread is interrupted
     * @throws AWTException if there's an AWT error
     * @throws IOException if there's an I/O error
     */
    @Override
    /**
     * Template method that defines the group traversal logic.
     * @param instance The instance to use for traversal
     * @throws InterruptedException if the thread is interrupted
     * @throws AWTException if there's an AWT error
     * @throws IOException if there's an I/O error
     */
    public abstract void traverseGroupsTemplate(SocialParentCommonUtils instance) 
            throws InterruptedException, AWTException, IOException;

    /**
     * Creates a new instance of the service starter via Spring context.
     * This is a convenience method for manual testing and should not be used in production code.
     *
     * @return a new instance of the service starter
     * @throws UnsupportedOperationException always, as this method should be called via Spring context
     */
    public static CompletableFuture<Void> callStart() {
        throw new UnsupportedOperationException(
                "This method should be called via Spring context. Use @Autowired to inject the service.");
    }

}
