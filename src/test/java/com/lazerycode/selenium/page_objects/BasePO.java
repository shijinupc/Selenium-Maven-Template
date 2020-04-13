package com.lazerycode.selenium.page_objects;

import org.openqa.selenium.WebDriver;

public class BasePO {
	protected WebDriver driver;
	protected String baseUrl;
	public BasePO(WebDriver driver, String baseUrl) {
		this.driver = driver;
		this.baseUrl = baseUrl;
	}
}
