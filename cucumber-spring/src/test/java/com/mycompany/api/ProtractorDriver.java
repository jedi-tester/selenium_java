package com.mycompany.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.Platform;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

import com.jprotractor.NgBy;
import com.jprotractor.NgWebDriver;
import com.jprotractor.NgWebElement;

public class ProtractorDriver {
  
  public static WebDriver driver;
  public static NgWebDriver ngDriver;
  private static WebDriverWait wait;
  static int implicitWait = 10;
  static int flexibleWait = 5;
  static long pollingInterval = 500;
  private static Actions actions; 
  private static final Logger log = LogManager.getLogger(ProtractorDriver.class);
  private static int highlightInterval = 100;

  private @Value("${browser}") String browser;
  private @Value("${location}") String location;
  private @Value("${chromepath}") String chromePath;

  @Before
  public void init() throws MalformedURLException {
    log.info("Launching (protractor) in " + browser + "...");
    DesiredCapabilities capabilities = null;

    if (browser.toLowerCase().equals("firefox")) {
        capabilities = capabilitiesFirefox(capabilities);        
    } else if (browser.toLowerCase().equals("phantomjs")) {
        capabilities = capabilitiesPhantomJS(capabilities);
    } else if (browser.toLowerCase().equals("chrome")) {
        capabilities = capabilitiesChrome(capabilities);
    } else if (browser.toLowerCase().equals("iexplore")) {
        capabilities = capabilitiesExplorer(capabilities);
    } else if (browser.toLowerCase().equals("android")) {
        capabilities = capabilitiesAndroid(capabilities);
    } else if (browser.toLowerCase().equals("iphone")) {
        capabilities = capabilitiesiPhone(capabilities);
    } else if (browser.toLowerCase().equals("ipad")) {
        capabilities = capabilitiesiPad(capabilities);
    }

    if (!location.toLowerCase().contains("local")) {
        log.info("Running on Selenium Grid: " + location);
        driver = new RemoteWebDriver(new URL(location), capabilities);
    } else if (browser.toLowerCase().equals("firefox")) {
        driver = new FirefoxDriver(capabilities);
    } else if (browser.toLowerCase().equals("phantomjs")) {
        driver = new PhantomJSDriver(capabilities);
    } else if (browser.toLowerCase().equals("chrome")) {
        driver = new ChromeDriver(capabilities);
    } else if (browser.toLowerCase().equals("iexplore")) {
        driver = new InternetExplorerDriver(capabilities);
    } else if (browser.toLowerCase().equals("android")) {
        driver = new ChromeDriver(capabilities);
    } else if (browser.toLowerCase().equals("iphone")) {
        driver = new ChromeDriver(capabilities);
    } else if (browser.toLowerCase().equals("ipad")) {
        driver = new ChromeDriver(capabilities);
    }
    ngDriver = new NgWebDriver(driver);
    wait = new WebDriverWait(driver, flexibleWait );
    wait.pollingEvery(pollingInterval,TimeUnit.MILLISECONDS);
    actions = new Actions(driver);
  }
   public DesiredCapabilities capabilitiesPhantomJS(DesiredCapabilities capabilities) {
 
    capabilities = new DesiredCapabilities("phantomjs", "", Platform.ANY);			
    capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {
      "--web-security=false",
      "--ssl-protocol=any",
      "--ignore-ssl-errors=true",
      "--local-to-remote-url-access=true", // prevent local file test XMLHttpRequest Exception 101 
      "--webdriver-loglevel=INFO" // set to DEBUG for a really verbose console output
    });
    return capabilities;
  }
  
  public DesiredCapabilities capabilitiesAndroid(DesiredCapabilities capabilities) {
    capabilities = DesiredCapabilities.chrome();

    Map<String, String> mobileEmulation = new HashMap<String, String>();
    mobileEmulation.put("deviceName", "Google Nexus 5");

    Map<String, Object> chromeOptions = new HashMap<String, Object>();
    chromeOptions.put("mobileEmulation", mobileEmulation);
    capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

    return capabilities;
  }

  public DesiredCapabilities capabilitiesiPhone(DesiredCapabilities capabilities) {
    capabilities = DesiredCapabilities.chrome();

    Map<String, String> mobileEmulation = new HashMap<String, String>();
    mobileEmulation.put("deviceName", "Apple iPhone 6");

    Map<String, Object> chromeOptions = new HashMap<String, Object>();
    chromeOptions.put("mobileEmulation", mobileEmulation);
    capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

    return capabilities;
  }

  public DesiredCapabilities capabilitiesiPad(DesiredCapabilities capabilities) {
    capabilities = DesiredCapabilities.chrome();

    Map<String, String> mobileEmulation = new HashMap<String, String>();
    mobileEmulation.put("deviceName", "Apple iPad");

    Map<String, Object> chromeOptions = new HashMap<String, Object>();
    chromeOptions.put("mobileEmulation", mobileEmulation);
    capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

    return capabilities;
  }

  public DesiredCapabilities capabilitiesFirefox(DesiredCapabilities capabilities) {  
    capabilities = DesiredCapabilities.firefox();

    FirefoxProfile profile = new FirefoxProfile();
    // java.lang.IllegalArgumentException: Preference network.http.phishy-userpass-length may not be overridden: frozen value=255, requested value=255
    // profile.setPreference("network.http.phishy-userpass-length", 255);
    profile.setEnableNativeEvents(true);
    profile.setAcceptUntrustedCertificates(true);

    capabilities.setCapability(FirefoxDriver.PROFILE, profile);
    capabilities.setBrowserName(DesiredCapabilities.firefox().getBrowserName());
    return capabilities;
  }

  public DesiredCapabilities capabilitiesChrome(DesiredCapabilities capabilities) {
    capabilities = DesiredCapabilities.chrome();

    String downloadFilepath = System.getProperty("user.dir") + System.getProperty("file.separator") + "target" + System.getProperty("file.separator");

    HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
    chromePrefs.put("profile.default_content_settings.popups", 0);
    chromePrefs.put("download.default_directory", downloadFilepath);
    chromePrefs.put("enableNetwork", "true");

    ChromeOptions option = new ChromeOptions();
    option.addArguments("test-type");
    option.addArguments("--start-maximized");
    option.setExperimentalOption("prefs", chromePrefs);
    option.addArguments("--browser.download.folderList=2");
    option.addArguments(
            "--browser.helperApps.neverAsk.saveToDisk=image/jpg,text/csv,text/xml,application/xml,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/excel,application/pdf");
    option.addArguments("--browser.download.dir=" + downloadFilepath);
    option.addArguments("allow-running-insecure-content");

    System.setProperty("webdriver.chrome.driver", chromePath);

    capabilities.setBrowserName(DesiredCapabilities.chrome().getBrowserName());
    capabilities.setCapability(ChromeOptions.CAPABILITY, option);
    capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

    return capabilities;
  }

  public DesiredCapabilities capabilitiesExplorer(DesiredCapabilities capabilities) {
    capabilities = DesiredCapabilities.internetExplorer();

    capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
    capabilities.setCapability("ignoreZoomSetting", true);
    capabilities.setCapability("ignoreProtectedModeSettings", true);
    capabilities.setBrowserName(DesiredCapabilities.internetExplorer().getBrowserName());
    return capabilities;
  }

  @After
  public void tearDown(Scenario scenario) {
    if (scenario.isFailed()) {
      final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      scenario.embed(screenshot, "image/png"); // ... and embed it in the report.
    }
    if (driver != null) {
      driver.quit();
    }
  }

  public static void waitForElementVisible(By locator) {
    log.info("Waiting for element visible for locator: {}", locator);
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }
  
  public static void waitForElementVisible(By locator, long timeout) {
    log.info("Waiting for element visible for locator: {}", locator);
    WebDriverWait wait = new WebDriverWait(driver, timeout);
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }
  
  public static void waitForElementPresent(By locator) {
    log.info("Waiting for element present  for locator: {}", locator);
    wait.until(ExpectedConditions.presenceOfElementLocated(locator));
  }

  public static void waitForElementPresent(By locator, long timeout) {
    log.info("Waiting for element present for locator: {}", locator);
    WebDriverWait wait = new WebDriverWait(driver, timeout);
    wait.until(ExpectedConditions.presenceOfElementLocated(locator));
  }

  public static void waitForPageLoad() {
    log.info("Wait for page load via JS...");
    String state = "";
    int counter = 0;

    do {
      try {
          state = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");
          Thread.sleep(1000);
      } catch (Exception e) {
          e.printStackTrace();
      }
      counter++;
      log.info(("Browser state is: " + state));
    } while (!state.equalsIgnoreCase("complete") && counter < 20);

  }

  public static boolean isAttributePresent(By locator, String attribute) {
    log.info("Is Attribute Present for locator: {}, attribute: {}", locator, attribute);
    return ngDriver.findElement(locator).getAttribute(attribute) != null;
  }

  public static void selectDropdownByIndex(By locator, int index) {
    log.info("Select Dropdown for locator: {} and index: {}", locator, index);
    try {
      Select select = new Select(ngDriver.findElement(locator));
      select.selectByIndex(index);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getBaseURL() {
    log.info("Get base URL: {}", ngDriver.getCurrentUrl());
    String currentURL = ngDriver.getCurrentUrl();
    String protocol = null;
    String domain = null;

    try {
      URL url = new URL(currentURL);
      protocol = url.getProtocol();
      domain = url.getHost();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return protocol + "://" + domain;
  }

  public static void clickJS(By locator) {
    log.info("Clicking on locator via JS: {}", locator);
    wait.until(ExpectedConditions.elementToBeClickable(ngDriver.findElement(locator)));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ngDriver.findElement(locator).getWrappedElement());
  }

  public static void scrollIntoView(By locator) {
    log.info("Scrolling into view: {}", locator);
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ngDriver.findElement(locator).getWrappedElement());
  }

  public static void mouseOver(By locator) {
    log.info("Mouse over: {}", locator);
    actions.moveToElement(ngDriver.findElement(locator).getWrappedElement()).build().perform();
  }

  public static void click(By locator) {
    log.info("Clicking: {}", locator);
    ngDriver.findElement(locator).click();
  }

  public static void clear(By locator) {
    log.info("Clearing input: {}", locator);
    ngDriver.findElement(locator).clear();
  }

  public static void sendKeys(By locator, String text) {
    log.info("Typing \"{}\" into locator: {}", text, locator);
    ngDriver.findElement(locator).sendKeys(text);
  }

  public static String getText(By locator) {
    String text = ngDriver.findElement(locator).getText();
    log.info("The string at {} is: {}", locator, text);
    return text;
  }

  public static String getAttributeValue(By locator, String attribute) {
    String value = ngDriver.findElement(locator).getAttribute(attribute);
    log.info("The attribute \"{}\" value of {} is: {}", attribute, locator, value);
    return value;
  }

  public static boolean isElementVisible(By locator) {
    try {
      wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
      log.info("Element {} is visible", locator);
      return true;
    } catch (Exception e) {
      log.info("Element {} is not visible", locator);
      return false;
    }
  }

  public static boolean isElementNotVisible(By locator) {
    try {
      wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
      log.info("Element {} is visible", locator);
      return false;
    } catch (Exception e) {
      log.info("Element {} is not visible", locator);
      return true;
    }
  }
  public static String getBodyText(){
    log.info("Getting boby text");
    return ngDriver.findElement(By.tagName("body")).getText();
  }

  public static void highlight( By locator) throws InterruptedException {
    log.info("Highlighting element {}", locator);
    WebElement element = ngDriver.findElement(locator);
	  ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid yellow'", element);
    Thread.sleep(highlightInterval);
    ((JavascriptExecutor) driver).executeScript("arguments[0].style.border=''", element);
  }
  
}
