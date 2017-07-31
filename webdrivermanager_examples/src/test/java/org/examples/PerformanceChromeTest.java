package org.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class PerformanceChromeTest {

	private static final int NUMBER_OF_BROWSERS = 5;
	private List<WebDriver> driverList = new ArrayList<>(NUMBER_OF_BROWSERS);

	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	@BeforeClass
	public static void setupClass() {
		ChromeDriverManager.getInstance().setup();
	}

	@Before
	public void setupTest() {
		for (int i = 0; i < NUMBER_OF_BROWSERS; i++) {
			driverList.add(new ChromeDriver());
		}
	}

	@After
	public void teardown() {
		for (int i = 0; i < NUMBER_OF_BROWSERS; i++) {
			driverList.get(i).close();
		}
	}

	@Test
	public void test() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_BROWSERS);
		final CountDownLatch latch = new CountDownLatch(NUMBER_OF_BROWSERS);

		for (final WebDriver driver : driverList) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						singleTestExcution(driver);
					} catch (Throwable e) {
						errorCollector.addError(e);
					} finally {
						latch.countDown();
					}
				}
			});
		}

		latch.await();
		executor.shutdown();
	}

	private void singleTestExcution(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, 30); // 30 seconds of timeout
		driver.get("https://en.wikipedia.org/wiki/Main_Page"); // navigate to
																														// Wikipedia

		By searchInput = By.id("searchInput"); // search for "Software"
		wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
		driver.findElement(searchInput).sendKeys("Software");
		By searchButton = By.id("searchButton");
		wait.until(ExpectedConditions.elementToBeClickable(searchButton));
		driver.findElement(searchButton).click();

		wait.until(ExpectedConditions.textToBePresentInElementLocated(
				By.tagName("body"), "Computer software")); // assert that the resulting
																										// page contains a text
	}

}
