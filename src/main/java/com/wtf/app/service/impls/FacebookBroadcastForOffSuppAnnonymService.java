package com.wtf.app.service.impls;

import com.wtf.app.commons.InitialSetup;

import com.wtf.app.parent.FacebookBroadcastParent;
import com.wtf.app.parent.FacebookCommonUtils;
import com.wtf.app.model.enums.FlowType;
import com.wtf.app.service.interfaces.IFacebookPluggable;
import com.wtf.app.model.xpaths.Facebook_Xpaths;
import com.wtf.app.web.driver.ParallelWebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.wtf.app.model.xpaths.XPathInterfaceFB;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.wtf.app.repository.WhatsappDBFileRepository;

@Service("facebookBroadcastForOffSuppAnnonymService")
public class FacebookBroadcastForOffSuppAnnonymService extends FacebookBroadcastParent
        implements IFacebookPluggable {

    private static final Logger logger = LogManager.getLogger(FacebookBroadcastForOffSuppAnnonymService.class);
    
    @Autowired
    public FacebookBroadcastForOffSuppAnnonymService(XPathInterfaceFB xPathInterfaceFB,
                                                   InitialSetup initialSetup,
                                                   ParallelWebDriverManager parallelWebDriverManager) {
        super(xPathInterfaceFB,  initialSetup, parallelWebDriverManager);
        logger.info("FacebookBroadcastForOffSuppAnnonymService initialized with InitialSetup and ParallelWebDriverManager");
    }


	@Override
	public void sendFacebookBroadcastToEachGroup(FacebookCommonUtils instance, List<String> groupIdList, String message,
			int groupListSize, String groupId, boolean annonymous) throws InterruptedException {

		try {
			driver.get(instance.getxPathInterface().getGroupsLink().concat(groupId));
			Thread.sleep(10000);

			driver.findElement(By.xpath("//span[text()='Write something...']")).click();

			Thread.sleep(2000);
			driver.findElement(By.xpath("//input[@role='switch' and @aria-label='Anonymous post toggle']")).click();// Annonymously
			Thread.sleep(2000);
			try {
				driver.findElement(By.xpath("//div[@aria-label='Got it']")).click();
			} catch (Exception ex) {
				logger.info("Annonymous Got it not there or Not required");
			}

			Thread.sleep(4000);
			WebElement postBox = driver.findElement(By.xpath("//div[contains(@aria-placeholder,'Submit an anonymous post...')]"));

			Thread.sleep(4000);
			postBox.sendKeys(message);
			logger.info( "Message is ready to be posted in the group id :" + groupId);

			Thread.sleep(3000);
			driver.findElement(By.xpath("//span[text()='Submit']//parent::span//parent::div//parent::div//parent::div//parent::div")).click();

			logger.info( "************** Message is posted successfully for groupId : " + groupId + " index:"
					+ groupIdList.indexOf(groupId) + " out of " + groupListSize+" *****************");
			Thread.sleep(8000);

			WhatsappDBFileRepository.exportAndMergeToExistingSingleFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB, Set.of(groupId));

		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
			if(retryCounter==0) {
			retryCounter=retryCounter+1;
			//Retry only once
			logger.info( "Retrying sendFacebookBroadcastToEachGroup once for groupId : " + groupId);
			sendFacebookBroadcastToEachGroup( instance,groupIdList,  message, groupListSize, groupId, annonymous);
			}
		}catch(Exception e) {
			e.printStackTrace();
			retryCounter=0;
		}
	}



	@Override
	public FlowType getEnumFlowType() {
		return FlowType.BROADCAST_WELCOME_AD_TO_FOR_COLLECTING_OFFSORE_SUPPORT_ANNONYM;
	}

	@Override
	public Collection<String> getBroadcastGroupIdNameFBSet_() {
		return broadcastGroupIdNameFBSet;
	}


	@Override
	public String getMessageToBroadcast() {
		return new Facebook_Xpaths()
				.getMESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_WELCOMING_OFFSHORE_FREELANCER_IN_ALL_TECHS();
	}

	@Override
	protected Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate() {
		return clearUpdateCurrDateAndReloadFile(GROUPS_ALREADY_MESSAGE_SENT_FOR_THE_DAY_TXT_FB,
				GROUP_STRING_TRAVERSE_ALREADY_DATE_FB, groupsAlreadyMessageSentForTheDaySetFB, true);
	}

	public boolean isAnnonymous() { return true;}
	
    /**
     * Traverses through Facebook groups and performs the required actions.
     * This method is called from the starter class to initiate the group traversal process.
     * 
     * @throws InterruptedException if the thread is interrupted
     * @throws AWTException if there's an AWT exception
     * @throws IOException if there's an I/O error
     */
    public void traverseGroups() throws InterruptedException, AWTException, IOException {
        logger.info("Starting to traverse Facebook groups for off-support freelancer greeting");
        
        try {
            // Call the parent class's traverseGroupsTemplate method with this instance
            // This will trigger the Facebook group traversal and posting process
            traverseGroupsTemplate(this);
            
            logger.info("Successfully completed Facebook groups traversal for off-support freelancer greeting");
        } catch (Exception e) {
            String errorMsg = "Error during Facebook groups traversal for off-support freelancer greeting: " + e.getMessage();
            logger.error(errorMsg, e);
            throw e;
        }
    }

	@Override
	public Set<String> getBroadcastGroupIdNameFBSet() {
		return broadcastGroupIdNameFBSet;
	}

}
