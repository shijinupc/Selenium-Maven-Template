package com.lazerycode.selenium.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TVTUtil {

	public final static String screenshotDir = "C:\\screenshotsTVT\\";
	public final static String extention = ".png";

	public final static int IMPLICITWAIT_TIMEINTERVAL = 20;

	public final static int NAVIGATE_REFRESH = 0;
	public final static int NAVIGATE_FOWARD = 1;
	public final static int NAVIGATE_BACK = 2;

	public final static String LOCALE_ARABIC = "ar";
	public final static String LOCALE_CHINESE = "zh-cn";
	public final static String LOCALE_TAIWAN = "zh-tw";
	public final static String LOCALE_GERMAN = "de";
	public final static String LOCALE_TURKISH = "tr";
	public final static String LOCALE_RUSSIAN = "ru";
	public final static String LOCALE_DUTCH = "nl";
	public final static String LOCALE_KOREAN = "ko";
	public final static String LOCALE_ITALIAN = "it";
	public final static String LOCALE_JAPANESE = "ja";
	public final static String LOCALE_FRENCH = "fr";

	public final static int LOCATERTYPE_ID = 0;
	public final static int LOCATERTYPE_CLASSNAME = 1;
	public final static int LOCATERTYPE_TAGNAME = 2;
	public final static int LOCATERTYPE_NAME = 3;
	public final static int LOCATERTYPE_LINKTEXT = 4;
	public final static int LOCATERTYPE_PARTIALLINKTEXT = 5;
	public final static int LOCATERTYPE_CSSPATH = 6;
	public final static int LOCATERTYPE_XPATH = 7;
	public final static int LOCATERTYPE_DOM = 8;

	public final static int PANLETYPE_ALERT = 0;
	public final static int PANLETYPE_FRAME = 1;
	public final static int PANLETYPE_WINDOW = 2;

	public static Logger getLogger(Class<?> clazz) {
		Logger log = LogManager.getLogger(clazz);

		return log;
	}

	/**
	 * 
	 * @return The method name of the test case which invoke this function
	 */
	public static String getTCNameAsPrefix() {
		StackTraceElement[] stacks = new Throwable().getStackTrace();
		return stacks[1].getMethodName() + "_";
	}

	public static void waitSeconds(int seconds) {
		try {
			Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void waitUntilPresence(WebDriver driver, By cssSelector) {
		waitUntilPresence(driver, cssSelector, 100);
	}

	public static void waitUntilPresence(WebDriver driver, By cssSelector, long seconds) {
		(new WebDriverWait(driver, seconds))
				.until(ExpectedConditions.presenceOfElementLocated(cssSelector));
	}
}
