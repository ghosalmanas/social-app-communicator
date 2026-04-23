package com.wtf.WA_TG_FB_SeleniumFreelancerAppV2;

import com.wtf.app.WaTgFbSeleniumFreelancerAppV2Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = WaTgFbSeleniumFreelancerAppV2Application.class)
class WaTgFbSeleniumFreelancerAppV2ApplicationTests {

	@Test
	void contextLoads() {
	}
}
