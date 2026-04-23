package com.wtf.app.app.a_runner.zTelegram;

import com.wtf.app.app.impls.WA_ContactExtractorAndMsgSender;
import com.wtf.app.app.xpaths.Telegram_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;
import org.openqa.selenium.Keys;

import java.io.IOException;

public class TG_X_BroadcastSelfSkillsToTG_ConsultantClients {


	private static final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT=
			"Hey, How are you ?"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ " *Call/Message* me directly for any Fullstack or Backend requirements in    "
			+ "*JAVA 8+,SpringBoot,Microservices,Kafka,Angular,React,AWS* and *Data Structures and Algorithm* "
			+ " for - *PROXY Call*/ Assignment/ *Job Support*/ *Coding Test*/ Training/ Task"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			//+ "*>* I am a *Professional Direct Proxy* and I do *Prompting and with Premium Otter or Google Transcript * wth Screen Control for coding."
			+ "*>* I am a *Professional Direct Java Proxy* - *Prompting and with Premium Otter* or LipSync"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*> 13+* years of *Expertise Industry Experience* as *Java Fullstack Technical Architect* and *Interview Proxy Call* expert since last 6 years."
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* *Domain Experiences*: _Banking, Capital Market/Securities Trading, Telecom, Healthcare, Aviation_"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* *Screen Sharing Apps*: VNC, *Chrome Remote Desktop*, *Anydesk*, TeamViewer, Ultraviewer, Zoom, Teams, WebEx, GetScreen and *GotoMypc*"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT);
	//">> NOTE: Be Aware of Fake People with Fake Experiences. Verify them by asking their Linkedin profile and crosscheck industry experience"


	public static void main(String[] args) throws InterruptedException, IOException {
		String fileNameToReadAllGroupNames = "known_client_consultant_names.txt";
		Boolean needToSendMessageToEveryGroup=true;
		String broadcastMessageToSend=MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT;
		XPathInterfaceWATG xPathInterfaceWatg= new Telegram_Xpaths();
		new WA_ContactExtractorAndMsgSender(xPathInterfaceWatg).updateContactsOrBroadcastMessages(fileNameToReadAllGroupNames, needToSendMessageToEveryGroup, broadcastMessageToSend);
	}
}
