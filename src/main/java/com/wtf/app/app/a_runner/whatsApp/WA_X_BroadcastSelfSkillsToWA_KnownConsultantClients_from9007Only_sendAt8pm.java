package com.wtf.app.app.a_runner.whatsApp;

import com.wtf.app.app.impls.WA_MsgSenderByPhoneNumber;
import com.wtf.app.app.xpaths.WhatsApp_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;
import org.openqa.selenium.Keys;

import java.io.IOException;

public class WA_X_BroadcastSelfSkillsToWA_KnownConsultantClients_from9007Only_sendAt8pm {


	private static final String MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT=
			"Hey, How are you ?"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+"*>* Hi, as you know, I am a Java *Fullstack Proxy and Support* Person with 14+ Years of *Expertise Industry Experience* and 7+ Years Experience in Interview Support and Job Support."
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* Call/Message me directly (*Direct Contact*) for any *Fullstack or Backend requirements* on - "
			+ "*INTERVIEW SUPPORT (PROXY Call)*/ Assignment/ *Job Support/ Coding Test*/ Training/ Task"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* Whatsapp:- https://wa.me/+919836509607"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* I am a *Java Fullstack Technical Lead Developer* expertised in"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)+
	        "*>* JAVA 8, JAVA 17, JAVA 21, Spring Boot, Microservices, Kafka, Angular, React, AWS, Docker, Kubernetes, Redis*, JPA, Data Structures and Algorithms "
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* A *Professional Java Fullstack Proxy* use - *Prompting & Transcript with Premium Otter* wth Screen Control for live coding."
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT)
			+ "*>* *Domain Experiences*: _Banking, Capital Market/Securities Trading, Telecom, Healthcare, Aviation_"
			+(Keys.SHIFT)+(Keys.ENTER)+(Keys.SHIFT);
		
	
	public static void main(String[] args) throws InterruptedException, IOException {
		String fileNameToReadAllGroupNames = "known_client_consultant_names_USA.txt";
		Boolean needToSendMessageToEveryGroup=true;
		String broadcastMessageToSend=MESSAGE_TO_EACH_GROUP_TO_ADVERTISE_FOR_SELF_SKILLSET_FOR_PROXY_SUPPORT;
		broadcastMessageToSend = broadcastMessageToSend.replace("+919836509607", "+919007765487");
		
		XPathInterfaceWATG xPathInterfaceWatg= new WhatsApp_Xpaths();

		new WA_MsgSenderByPhoneNumber(xPathInterfaceWatg).updateContactsOrBroadcastMessages(fileNameToReadAllGroupNames, needToSendMessageToEveryGroup, broadcastMessageToSend);
	}
}
