1.Pin Oindrila
2.Check group_traverse_already_date_ curr date in groups_already_message_sent_forTheDay.txt
3.Run from Runner package
4.Use only abstraction and instance based template so that same structure is would be there

. Comment/uncomment updateExludePersonalContactListFile(groupNameExceptIndv) and use exclude_group_contact_names_always_for_the_day.txt for below as must contacts
sambeet
jony
satyajit
joydeep
satyabrata
nipurba
debmalya
soumik
santu
pintu

////button[translate(text(),'ok','OK')='OK']

// Temp://*[@aria-label='Profile picture, disappearing messages on']

		//// *[@aria-label='Profile picture, disappearing messages
		//// on']/parent::div[1]/following::span[@dir='auto' and @title and @class]
		// *[@aria-label='Profile picture, disappearing messages
		//// on']/parent::div[1]/parent::div[1]/following-sibling::div[1]/child::div[1]/child::div[1]/child::span[1]
		// span[@data-testid="default-group" and @data-icon="default-group" ]

		// *[@aria-label='Profile picture, disappearing messages
		// on']/parent::div[1]/parent::div[1]/following-sibling::div[1]/child::div[1]/child::div[1]/child::span[1]
		// | //span[@data-testid="default-group" and @data-icon="default-group" ]

		// https://www.youtube.com/watch?v=MjtJeDbSeSc

		// https://www.youtube.com/watch?v=9N7ERYWbjuw
		/*
		 * Use CTRL + F to open xpath search string window
		 *
		 * Xpath=//tagname[@attribute='value']
		 *
		 * 1. //*[@id='Manas'] 2. //input[@id='Manas'] or //input[@type='Text']
		 * 3.//input[@id='Manas'] | //input[@type='Text'] 4. //input[@id='Manas'] and
		 * //input[@type='Text'] 5. (//*[Text()='Click here to search']) [1]
		 * 6.//*[contains(@title,' JOB SUPPORTS')]
		 * 7.//div[contains(@class,'aui-column-content-last')]
		 * 8.//div[contains(@class,'atag') and contains(@class ,'btag')]
		 *
		 * Contains(),Using OR & AND,Starts-with(), Text(),
		 * Following,ancestor,child,preceding,following-sibling,parent, self,descendant
		 * Xpath=//*[@type='text']//following::input[1] Xpath=//*[text()='Enterprise
		 * Testing']//ancestor::div[1] Xpath=//*[@id='java_technologies']//child::li
		 * Xpath=//*[@type='submit']//preceding::input
		 * xpath=//*[@type='submit']//following/preceding-sibling::input
		 * Xpath=//*[@id='rt-feature']//parent::div Xpath
		 * =//*[@type='password']//self::input
		 * Xpath=//*[@id='rt-feature']//descendant::a //span[starts-with(text(),':')]
		 *
		 * https://www.guru99.com/xpath-selenium.html
		 *
		 * //span[@dir='auto' and @title]//self::text() //span[@dir='auto' and @title
		 * and @class]//self::span
		 *
		 *
		 * https://testsigma.com/blog/scrolling-in-selenium/
		 Think of it this way:

text() is like looking only at the text written directly on the surface of a box.
. is like reading all the text written on the box and everything inside it.
For checking if an element contains specific text anywhere within its content, using . is generally the more robust approach.
		 *
		 * driver.findElement(By.
		 * xpath("//div[@class='seg2_formBox']//span[@class='mbs2_formError' and not(@class='ng-hide')]//preceding::label[1]"
		 * )).sendKeys("Manoj Soundarrajan");
		 *
		 *
		 * /following::span[not(contains(text(), 'group info')) and not(contains(text(), ','))]

		 *
		 * https://www.youtube.com/watch?v=aAWvwGFkySI
		 *
		 * Examples ----- //label[text()='Email']/following-sibling::input[1]
		 * //td[text()='Maria Anders']/preceding-sibling::td/child::input
		 * //label[text()='Email']/following-sibling::input[1]/parent::div
		 * //div[@class='container']/child::input[@type='text']
		 * //div[@class='container']/descendant::button
		 * //div[@class='buttons']/ancestor-or-self::div
		 * //label[text()='Password']/following::input[1]
		 *
		 * //production[not(contains(category,'Business'))]
		 *
		 *
		 * //new WebDriverWait(driver,
		 * 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
		 * "//span[text()='index.html']")));
		 * //je.executeScript("arguments[0].scrollIntoView(true);",element);
		 *
		 */

		/* <Element attribute1="abc" attribute2="xyz">Data</Element> */
		// Element[@attribute1="abc" and @attribute2="xyz" and text()="Data"]



		Use XPath to Select the Element:
		===================================
You can use the $x() function in the console to execute XPath expressions.
Type your XPath within $x(): For example, if your XPath to the element is //button[@id='myButton'], you would type the following in the console and press Enter:
JavaScript
$x("//button[@id='myButton']")
Result: The console will return an array (or a single element if only one matches) of the DOM elements that match your XPath.

3. Access the Element in Dev Console and Trigger a Click to confirm element is clickable or interactable:
==========================================================================================================
Once you have the element(s) selected, you can access the first matching element (usually the one you're interested in) using array indexing ([0]) and then call its click() method.
For a single matching element:

JavaScript
$x("//button[@id='myButton']")[0].click()
If you have multiple matches and want to try clicking a specific one (e.g., the second one):

JavaScript
$x("//your/xpath")[1].click()
eg: $x("//*[@title='New chat']")[0].click()




<div id="container">
  <p class="message">Hello!</p>
  <span>Some text</span>
</div>
And you use the following Selenium XPath to locate the <p> element:
//p[@class='message']
If you then want to select the parent div element of this <p> element using XPath, you can append /.. to your existing XPath:
//p[@class='message']/..