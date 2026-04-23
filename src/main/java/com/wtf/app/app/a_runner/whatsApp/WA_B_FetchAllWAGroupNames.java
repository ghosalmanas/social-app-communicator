package com.wtf.app.app.a_runner.whatsApp;

import com.wtf.app.app.commons.WhatsappCommonUtils;
import com.wtf.app.app.enums.SocialType;
import com.wtf.app.app.impls.WA_B_FetchAllWAContactsWGroupNames;
import com.wtf.app.app.xpaths.WhatsApp_Xpaths;
import com.wtf.app.app.xpaths.XPathInterfaceWATG;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WA_B_FetchAllWAGroupNames extends WhatsappRunnerParent {

	public static void main(String[] args) throws InterruptedException, IOException, AWTException {

		Properties prop = new Properties();

		// try-with-resources automatically closes the InputStream
		try (InputStream input = WA_B_FetchAllWAGroupNames.class.getClassLoader().getResourceAsStream("D:\\Eclipse-workspace\\WA_TG_FB_SeleniumFreelancerApp\\src\\resources\\config.properties")) {
			if (input == null) {
				System.out.println("Sorry, unable to find config.properties");
				return ;
			}
			prop.load(input);
			prop.forEach((key, value) -> System.out.println("getConfigProperties: "+key + " : " + value));

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		XPathInterfaceWATG xPathInterface= new WhatsApp_Xpaths();
		WhatsappCommonUtils instance = new WA_B_FetchAllWAContactsWGroupNames(xPathInterface.getBASE_URL(),SocialType.WHATSAPP);
		execute(instance);
	}
}
