package com.wtf.WA_TG_FB_SeleniumFreelancerAppV2;

import com.wtf.app.WaTgFbSeleniumFreelancerAppV2Application;
import org.springframework.boot.SpringApplication;

public class TestWaTgFbSeleniumFreelancerAppV2Application {

	public static void main(String[] args) {
		SpringApplication.from(WaTgFbSeleniumFreelancerAppV2Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
