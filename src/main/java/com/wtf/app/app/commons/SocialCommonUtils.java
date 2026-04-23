package com.wtf.app.app.commons;

import com.wtf.app.app.com.parent.Constants;
import com.wtf.app.app.enums.SocialType;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Random;

public abstract class SocialCommonUtils extends Constants {

	protected SocialCommonUtils(String baseURL, SocialType socialType) {
		super(baseURL, socialType);
	}

	protected void scrollAndSleepRandomly(int delay1, int delay2) throws InterruptedException {
		Thread.sleep((long) (Math.random() * 3000 + delay1));
		scrollUpAndDown();
		randomDelay(3000, delay2);
	}

	protected String getRandomGreeting() {
		List<String> originalList = List.of("Hi", "Hey", "Hello", "Greetings", "Good day", "Hey there", "Hi there");
		// 1. Get a single random string
		int size = originalList.size();
		return originalList.get(getRandomNumber(0, size - 1));
	}

	protected int getRandomNumber(int lowerBoundInt, int upperBoundInt) {
		return lowerBoundInt + (int) (Math.random() * (upperBoundInt - lowerBoundInt + 1));
	}

	protected static void randomDelay(long minMillis, long maxMillis) {
		Random random = new Random();
		try {
			long bound = maxMillis - minMillis + 1;
			bound = bound< 0? -bound :bound;// bound always must be positive
			Thread.sleep(random.nextInt((int) bound) + minMillis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	protected static void gradualTypeMessage(WebElement element, String message) {
		for (char c : message.toCharArray()) {
			element.sendKeys(String.valueOf(c));
			randomDelay(40, 100); // Small delay per character
		}
	}

	protected static void gradualTypeMessageCustom(WebElement element, String message) {
		for (String word : message.split(" ")) {
			if(word.equals("_NL_")) {//replicate custom new line char if already not working
				Actions actions = new Actions(driver);
				//actions.sendKeys(Keys.SHIFT, Keys.ENTER).perform();//add new line for fb, wa, tg
				actions.keyDown(Keys.SHIFT).sendKeys(/*element,*/ Keys.ENTER).keyUp(Keys.SHIFT).build().perform();
				actions.release();
				randomDelay(20,200);
				continue;
			}

			for (char c : word.toCharArray()) {
				element.sendKeys(String.valueOf(c));
				randomDelay(40, 100); // Small delay per character
			}
			element.sendKeys(" ");
			randomDelay(150, 200); // pause per word
		}
	}

	protected static void gradualTypeMessageOnlyLastLine(WebElement element, String message) {
		String[] sentenceByNewLine = message.split("_NL_");
		int sentenceCount = sentenceByNewLine.length;
		int counter=0;

		for (String sentence : sentenceByNewLine) {
			if(counter < sentenceCount-1) {
				element.sendKeys(sentence);
				Actions actions = new Actions(driver);
				//actions.sendKeys(Keys.SHIFT, Keys.ENTER).perform();//add new line for fb, wa, tg
				actions.keyDown(Keys.SHIFT).sendKeys(/*element,*/ Keys.ENTER).keyUp(Keys.SHIFT).build().perform();
				actions.release();
				randomDelay(20, 200);
				counter++;
				continue;
			}

			logger.severe("Executing last sentence char by char : " + sentence);
			for (char c : sentence.toCharArray()) {
				element.sendKeys(String.valueOf(c));
				randomDelay(50, 300); // Small delay per character
			}
			Actions actions = new Actions(driver);
			actions.sendKeys(""+((int) (Math.random() * 10))).build().perform();
			randomDelay(50, 300);
			actions.sendKeys(Keys.BACK_SPACE).build().perform();//Mimic human typing to add a 0-9 and delete it
			randomDelay(50, 300);
			actions.release();
		}
	}


	protected static void gradualTypeMessageByWords(WebElement element, String message) {
		for (String s : message.split(" ")) {
			element.sendKeys(s+" ");//adding the space back
			randomDelay(20, 50); // Small delay per character
		}
	}

	private void scrollUpAndDown() {
		Random random = new Random();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0, " + random.nextInt(190) + ")");
		randomDelay(500, 1500);
		js.executeScript("window.scrollBy(0, -" + random.nextInt(170) + ")");
		randomDelay(300, 1000);
	}
}