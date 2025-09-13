package com.wtf.app.commons;

import com.wtf.app.parent.WATGParent;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.inject.Inject;
import jakarta.inject.Named;

//@Component
public abstract class CommonImpl extends WATGParent {

    protected static final Logger logger = LogManager.getLogger(CommonImpl.class);

    @Autowired
    protected CommonImpl(
            InitialSetup initialSetup,
            ParallelWebDriverManager parallelWebDriverManager) {
        super(initialSetup, parallelWebDriverManager);

        logger.info("CommonImpl initialized with  InitialSetup, and WebDriver for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
    }


    @Override
    public boolean searchAndClickOnGroup(String oldGroupString) {

        try {
            febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
            WebElement webElement = febx(getxPathInterface().getXPATH_SEARCH());

            // 1.Search groupName in SearchBox
            Thread.sleep(getSLEEP_TIME_MS()/2);
            logger.info("Old Group typing in Search box: " + oldGroupString);
            webElement.sendKeys(oldGroupString + "\n");
            Thread.sleep(getSLEEP_TIME_MS()/2);
            // driver.manage().timeouts().implicitlyWait(SLEEP_TIME, TimeUnit.MILLISECONDS);
            WebElement webElementGroupNameCheck = getDriver()
                    .findElement(By.xpath("//span[contains(@title,'" + oldGroupString + "')]"));
            webElement.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
            Thread.sleep(getSLEEP_TIME_MS());
            febx("//span[@data-icon='x-alt']").click();
            Thread.sleep(getSLEEP_TIME_MS()/2);
            logger.info("Old Group restored from Search box: " + oldGroupString);
            return true;

        } catch (Exception exception) {
            logger.info("searchAndClickOnGroup : " + exception.getLocalizedMessage());
        }
        return true;
    }


    @Override
    public void removeArchievedOrExitedClosurePopups() {
        try {
            getDriver().findElements(By.xpath(getxPathInterface().getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT())).stream()
                    .forEach(crossArchievedPopup -> {
                        if (crossArchievedPopup != null) {
                            crossArchievedPopup.click();
                        }
                    });
            Thread.sleep(200);
            logger.info("ClosurePopups executed.....  for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeSingleArchievedOrExitedClosurePopup() {
        try {
            WebElement webElement = febx(getxPathInterface().getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT());
            if (webElement != null && !webElement.getText().isEmpty()) {
                webElement.click();
            }
            logger.info("ClosurePopups executed.....  for SOCIAL_TYPE: {}", getxPathInterface().getSocialType().name());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
