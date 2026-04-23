package com.wtf.app;

import com.wtf.app.app.a_runner.whatsApp.WA_A_UnArchieve_Support_Groups;
import com.wtf.app.app.a_runner.whatsApp.WA_B_FetchAllWAGroupNames;
import com.wtf.app.app.a_runner.whatsApp.WA_C_BroadcastSelfSkillsToWA_Groups;
import com.wtf.app.app.a_runner.whatsApp.WA_D_Archieve_Support_Groups;
import com.wtf.app.app.a_runner.zFacebook.FB_A_BroadcastSelfSkillsToFB_Groups;
import com.wtf.app.app.a_runner.zFacebook.FB_B_BroadcastSelfSkillsToFB_SensitiveGroups;
import com.wtf.app.app.a_runner.zFacebook.FB_C_BroadcastGreetOffFreelancerInFB_Groups;
import com.wtf.app.app.a_runner.zTelegram.TG_A_FetchAllTelegramGroupNames;
import com.wtf.app.app.a_runner.zTelegram.TG_B_BroadcastSelfSkillsToTG_Groups;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunnerOneStopSolution {

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
				WA_A_UnArchieve_Support_Groups.main(null);
				break;
			case "12":
				System.out.print(" Invoking - B_FetchAllWAGroupNames");
				WA_B_FetchAllWAGroupNames.main(null);
				break;
			case "14":
				System.out.print(" Invoking - C_BroadcastSelfSkillsToWA_Groups");
				WA_C_BroadcastSelfSkillsToWA_Groups.main(null);
				break;
			case "15":
				System.out.print(" Invoking - D_Archieve_Support_Groups");
				WA_D_Archieve_Support_Groups.main(null);
				break;

			case "21":
				System.out.print(" Invoking - A_FetchAllTelegramGroupNames");
				TG_A_FetchAllTelegramGroupNames.main(null);
				break;
			case "22":
				System.out.print(" Invoking - B_BroadcastSelfSkillsToTG_Groups");
				TG_B_BroadcastSelfSkillsToTG_Groups.main(null);
				break;

			case "31":
				System.out.print(" Invoking - A_BroadcastSelfSkillsToFB_Groups");
				FB_A_BroadcastSelfSkillsToFB_Groups.main(null);
				break;
			case "32":
				System.out.print(" Invoking - C_BroadcastSelfSkillsToFB_RestrictedGroups");
				FB_B_BroadcastSelfSkillsToFB_SensitiveGroups.main(null);
				break;
			case "33":
				System.out.print(" Invoking - B_BroadcastGreetOffFreelancerInFB_Groups");
				FB_C_BroadcastGreetOffFreelancerInFB_Groups.main(null);
				break;
			default:
				break;
			}
		} catch (InterruptedException | IOException | AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
