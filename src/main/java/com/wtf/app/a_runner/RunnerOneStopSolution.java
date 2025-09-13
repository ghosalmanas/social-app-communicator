/*
package com.wtf.app.a_runner;

import com.wtf.app.service.impls.starter.service.whatsApp.WhatsappUnArchieveSupportGroupsStarter;
import com.wtf.app.service.impls.starter.service.whatsApp.WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter;
import com.wtf.app.service.impls.starter.service.whatsApp.WhatsappBroadcastSelfSkillsToWAGroupsStarter;
import com.wtf.app.service.impls.starter.service.whatsApp.WhatsappArchiveSupportGroupsStarter;
import com.wtf.app.service.impls.starter.service.zFacebook.FacebookBroadcastSelfSkillsToFBGroupsStarter;
import com.wtf.app.service.impls.starter.service.zFacebook.FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter;
import com.wtf.app.service.impls.starter.service.zFacebook.FacebookBroadcastGreetOffFreelancerInFBGroupsStarter;
import com.wtf.app.service.impls.starter.service.zTelegram.TelegramFetchAllWAContactsGroupNamesJoinLinksStarter;
import com.wtf.app.service.impls.starter.service.zTelegram.TelegramBroadcastSelfSkillsToTGGroupsStarter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class RunnerOneStopSolution {
    private static final Logger logger = LogManager.getLogger(RunnerOneStopSolution.class);

	public static void main(String[] args) {
		String input = null;
		try {
			if (args.length == 0) {
				System.out.println(""
						+ "............WhatsApp....................."
						+ "\n"
						+ "11:A_UnArchieve_Support_Groups\n"
						+ "12:B_FetchAllWAGroupNames\n"
						+ "14:C_BroadcastSelfSkillsToWA_Groups\n"
						+ "15:D_Archieve_Support_Groups\n"
						+ "\n"
						+ "............Telegram....................."
						+ "\n"
						+ "21:A_FetchAllTelegramGroupNames\n"
						+ "22:B_BroadcastSelfSkillsToTG_Groups\n"
						+ "\n"
						+ "............Facebook....................."
						+ "\n"
						+ "31:A_BroadcastSelfSkillsToFB_Groups\n"
						+ "32:B_BroadcastSelfSkillsToFB_SensitiveGroups\n"
						+ "33:C_BroadcastGreetOffFreelancerInFB_Groups\n"
						+ "");
				System.out.println("Input Not Recieved as CMD Parameter. Please Enter the Integer Input : ");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				input = reader.readLine();
			}

			switch (input) {
			case "11":
				System.out.print(" Invoking - A_UnArchieve_Support_Groups");
				WhatsappUnArchieveSupportGroupsStarter.callStart();
				break;
			case "12":
				System.out.print(" Invoking - B_FetchAllWAGroupNames");
				WhatsappFetchAllWAContactsGroupNamesJoinLinksStarter.callStart();
				break;
			case "14":
				System.out.print(" Invoking - C_BroadcastSelfSkillsToWA_Groups");
				WhatsappBroadcastSelfSkillsToWAGroupsStarter.callStart();
				break;
			case "15":
				System.out.print(" Invoking - D_Archieve_Support_Groups");
				WhatsappArchiveSupportGroupsStarter.callStart();
				break;

			case "21":
				System.out.print(" Invoking - A_FetchAllTelegramGroupNames");
				TelegramFetchAllWAContactsGroupNamesJoinLinksStarter.callStart();
				break;
			case "22":
				System.out.print(" Invoking - B_BroadcastSelfSkillsToTG_Groups");
				TelegramBroadcastSelfSkillsToTGGroupsStarter.callStart();
				break;

			case "31":
				System.out.print(" Invoking - A_BroadcastSelfSkillsToFB_Groups");
				FacebookBroadcastSelfSkillsToFBGroupsStarter.callStart();
				break;
			case "32":
				System.out.print(" Invoking - C_BroadcastSelfSkillsToFB_RestrictedGroups");
				FacebookBroadcastSelfSkillsToFBSensitiveGroupsStarter.callStart();
				break;
			case "33":
				System.out.print(" Invoking - B_BroadcastGreetOffFreelancerInFB_Groups");
				FacebookBroadcastGreetOffFreelancerInFBGroupsStarter.callStart();
				break;
			default:
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
*/
