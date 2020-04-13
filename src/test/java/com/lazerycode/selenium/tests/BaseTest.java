package com.lazerycode.selenium.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.lazerycode.selenium.DriverBase;

public class BaseTest extends DriverBase {
	protected WebDriver driver;
	protected String baseDir = "./tvt";
	
	
	protected void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	
	protected String getImageDir() {
		File imageDirFile = new File(baseDir, System.getProperty("language"));
		String imageDir = imageDirFile.getPath();
		imageDirFile.mkdirs();
		return imageDir;
	}

    protected void captureScreen(WebDriver driver, String fileName) {
        try {
            FileOutputStream screenshotStream = new FileOutputStream(new File(getImageDir(), fileName));
            screenshotStream.write(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            screenshotStream.close();
        } catch (IOException unableToWriteScreenshot) {
            System.err.println("Unable to write " + new File(getImageDir(), fileName).getAbsolutePath());
            unableToWriteScreenshot.printStackTrace();
        }
    }
	
    protected void captureElement(WebElement element, String fileName) {
        try {
            FileOutputStream screenshotStream = new FileOutputStream(new File(getImageDir(), fileName));
            screenshotStream.write(element.getScreenshotAs(OutputType.BYTES));
            screenshotStream.close();
        } catch (IOException unableToWriteScreenshot) {
            System.err.println("Unable to write " + new File(getImageDir(), fileName).getAbsolutePath());
            unableToWriteScreenshot.printStackTrace();
        }
    }
    
    @Parameters({ "locale" })
	@BeforeClass
	public void initiate(@Optional("en") String locale) {
		System.setProperty("headless", "false");
		String firefoxDriverPath = "C:\\shijin\\workspace\\Selenium-Maven-Template\\src\\test\\resources\\selenium_standalone_binaries\\windows\\marionette\\64bit\\geckodriver.exe";
		System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
		String chromeDriverPath = "C:\\shijin\\workspace\\Selenium-Maven-Template\\src\\test\\resources\\selenium_standalone_binaries\\windows\\googlechrome\\64bit\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		System.setProperty("headless", "false");
//		System.setProperty("proxyEnabled", "true");
//		System.setProperty("proxyHost", "proxy.emea.ibm.com");
//		System.setProperty("proxyPort", "8080");
		
		System.setProperty("language", locale);
	}
	
	@Parameters({"browser"})
	@BeforeMethod
	public void setupDriver(@Optional("firefox") String browser) throws Exception {
		System.setProperty("browser", browser);
		driver = getDriver();
		driver.manage().window().maximize();
//		driver.manage().window().setPosition(new Point(0, 0));
//		driver.manage().window().setSize(new Dimension(1024, 768));
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	}
	@AfterMethod
	public void teardown() {
//		driver.close();//父类里要清cookie
	}
}
