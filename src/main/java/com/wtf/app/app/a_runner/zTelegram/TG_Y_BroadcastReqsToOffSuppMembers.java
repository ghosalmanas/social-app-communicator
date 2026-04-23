package com.wtf.app.app.a_runner.zTelegram;

import com.wtf.app.app.impls.WA_ContactExtractorAndMsgSender;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;
import org.openqa.selenium.Keys;

import java.io.IOException;

public class TG_Y_BroadcastReqsToOffSuppMembers {


	private static final String MESSAGE_TO_SUPPORT_CANDIDATE_LIST_JD_REQUIREMENT="Hi, We have Support Freelancing Requirement for following Tech Stack"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "1. "
			+ "Java Development for 2-3 years of consultant in India,"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "2. "
			+ "Reactor Kafka with Java for 7 years Experience consultant in US"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "Please let me know if you are comfortable for daily Max 2 hrs(Mon-Fri) Freelancing"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "Please Save The Contact for Continuous Requirements in Pipeline";


		public static void main(String[] args) throws InterruptedException, IOException {
			String fileNameToReadAllGroupNames = "candidate_support_names_java.txt";
			Boolean needToSendMessageToEveryGroup=true;
			String broadcastMessageToSend=MESSAGE_TO_SUPPORT_CANDIDATE_LIST_JD_REQUIREMENT;
			XPathInterfaceWATG xPathInterfaceWatg= new Telegram_Xpaths();
			new WA_ContactExtractorAndMsgSender(xPathInterfaceWatg).updateContactsOrBroadcastMessages(fileNameToReadAllGroupNames, needToSendMessageToEveryGroup, broadcastMessageToSend);
	}
}
