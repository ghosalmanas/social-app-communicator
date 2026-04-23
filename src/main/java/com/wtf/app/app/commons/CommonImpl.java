package com.wtf.app.app.commons;

import com.wtf.app.app.com.parent.WhatsappParent;
import com.wtf.app.app.enums.SocialType;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public abstract class CommonImpl extends WhatsappParent {

	CommonImpl(String baseURL, SocialType socialType) {
		super(baseURL,socialType);
	}


	@Override
	public boolean searchAndClickOnGroup(String oldGroupString) {

		try {
			febx(getxPathInterface().getLABEL_MAGNIFYING_GLASS_SEARCH_BUTTON_XPATH()).click();
			WebElement webElement = febx(getxPathInterface().getXPATH_SEARCH());

			// 1.Search groupName in SearchBox
			Thread.sleep(1000);
			logger.fine( "Old Group typing in Search box: " + oldGroupString);
			webElement.sendKeys(oldGroupString + "\n");
			Thread.sleep(1000);
			// driver.manage().timeouts().implicitlyWait(SLEEP_TIME, TimeUnit.MILLISECONDS);
			WebElement webElementGroupNameCheck = driver
					.findElement(By.xpath("//span[contains(@title,'" + oldGroupString + "')]"));
			webElement.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
			Thread.sleep(2000);
			febx("//span[@data-icon='x-alt']").click();
			Thread.sleep(1000);
			logger.fine( "Old Group restored from Search box: " + oldGroupString);
			return true;

		} catch (Exception exception) {
			logger.fine( "searchAndClickOnGroup : " + exception.getLocalizedMessage());
		}
		return true;
	}


	@Override
	public void removeArchievedOrExitedClosurePopups() {
		try {
			driver.findElements(By.xpath(getxPathInterface().getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT())).stream()
			.forEach(crossArchievedPopup -> {
				if(crossArchievedPopup!=null) {
				crossArchievedPopup.click();
				}});
			Thread.sleep(200);
			logger.fine( "ClosurePopups executed..... ");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void removeSingleArchievedOrExitedClosurePopup() {
		try {
			WebElement webElement =febx(getxPathInterface().getSPAN_CLOSE_POPUP_ARCHIVE_UNARCHIVE_X_ALT());
			if(webElement!=null && !webElement.getText().isEmpty()) {
				webElement.click();
			}
			logger.fine( "ClosurePopups executed..... ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
