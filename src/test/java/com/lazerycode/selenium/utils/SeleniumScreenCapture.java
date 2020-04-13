package com.lazerycode.selenium.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.lazerycode.selenium.utils.AdvScreenCapture.Axis;

public class SeleniumScreenCapture {
	
	protected final static Logger logger = LogManager.getLogger(SeleniumScreenCapture.class);
	
	public static void captureScreen(String imagePath) {
		
	}
	
	/**
	 * Capture the entire browser window.
	 * @param imagePath the full path of the screen capture
	 */
	public static void captureWindow(WebDriver driver ,String imagePath) {
		File screenFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(screenFile, new File(imagePath));
			logger.info("Captured " + imagePath);
		} catch (IOException ex) {
			logger.warn(ex);
			throw new RuntimeException(ex);
		}
	}
	
	public static void capturePageTitle(WebDriver driver, String imagePath) {
		File screenFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		BufferedImage screenImg = null;
		try {
			screenImg = ImageIO.read(screenFile);
			Graphics2D mergedCanvas = null;

			int fontSize = 12;
			String text = "The page title needed to be verified is: " + driver.getTitle();
			Font font = new Font("sans-serif", Font.PLAIN, fontSize);
			int pageTitle_h = fontSize + 7;
			//calculate the real width of the title message
			BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D tempCanvas = tempImg.createGraphics();
			tempCanvas.setFont(font);
			int pageTitle_w = tempCanvas.getFontMetrics().stringWidth(text) + 7;
			BufferedImage pageTitleImg = new BufferedImage(pageTitle_w, pageTitle_h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D pageTitleCanvas = pageTitleImg.createGraphics();
			pageTitleCanvas.setFont(font);
			pageTitleCanvas.setPaint(Color.decode("#F5FCDE"));
			pageTitleCanvas.fillRect(0, 0, pageTitle_w, pageTitle_h);
			pageTitleCanvas.setPaint(Color.black);
			pageTitleCanvas.draw3DRect(0, 0, pageTitle_w-1, pageTitle_h-1, true);
			pageTitleCanvas.drawString(text,3,pageTitle_h-5);
			pageTitleCanvas.dispose();
			
			mergedCanvas = screenImg.createGraphics();
			mergedCanvas.drawImage(pageTitleImg, (screenImg.getWidth()-pageTitleImg.getWidth())/2, (screenImg.getHeight()-pageTitleImg.getHeight())/2, null);
			mergedCanvas.dispose();
			
			ImageIO.write(screenImg, "png", new File(imagePath));
			logger.info("Captured " + imagePath);
		} catch (IOException ex) {
			logger.warn(ex);
			throw new RuntimeException(ex);
		};
	}
	
	public static void captureTitle(WebDriver driver, String imagePath, WebElement elementWithTitle) {
		File screenFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		BufferedImage screenImg = null;
		BufferedImage mergedImg = null;
		try {
			screenImg = ImageIO.read(screenFile);
			mergedImg = mergeTitle2Img(elementWithTitle, screenImg);
			ImageIO.write(mergedImg, "png", new File(imagePath));
			logger.info("Captured " + imagePath);
		} catch (IOException ex) {
			logger.warn(ex);
			throw new RuntimeException(ex);
		};

	}
	
	/**
	 * Capture a specific area by using Selenium function
	 * The specific area can be identified by locating a layout web element, such as a div.
	 * @param screenCaptureFullPath the full path of the screen capture
	 * @param element	The Web element need to be captured
	 */
	public static void captureElement(WebDriver driver, File imageFile, WebElement element) {
		RenderedImage elementImage = captureElement(driver, element);
		try {
			imageFile.createNewFile();
			ImageIO.write(elementImage, TVTUtil.extention.substring(1), imageFile);
			logger.info("Captured " + imageFile.getPath());
		} catch (IOException ex) {
			logger.warn(ex);
		}
	}
	
	public static BufferedImage captureElement(WebDriver driver, WebElement element) {
		Point p = element.getLocation();
		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();
		byte[] imageBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		try {
			BufferedImage screenImage = ImageIO.read( new ByteArrayInputStream(imageBytes) );
			BufferedImage elementImage = screenImage.getSubimage(p.getX(), p.getY(), width, height);
			return elementImage;
		} catch (IOException ex) {
			logger.warn(ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Capture an area by AWT function and resizing the width and height of the element
	 * @param screenCaptureFullPath the full path of the screen capture
	 * @param element with the scroll bar 
	 */
	public static void captureScrollElementWithResizing(final WebDriver driver, final File imageFile, final WebElement scrollElement) {
		final JavascriptExecutor js = (JavascriptExecutor) driver;
		long old_height = (long)js.executeScript("var old_height = arguments[0].offsetHeight;"+
		"arguments[0].style.height = arguments[0].scrollHeight + \"px\";"+
		"arguments[0].style.maxHeight = arguments[0].scrollHeight + \"px\";"+
		"return old_height;",scrollElement);
		
		captureElement(driver, imageFile, scrollElement);
		
		js.executeScript("arguments[0].style.height = arguments[1] + \"px\";"+
				"arguments[0].style.maxHeight = arguments[1] + \"px\";",scrollElement, old_height);
		
	}
	
	/**
	 * Capture an area by AWT function
	 * @param screenCaptureFullPath the full path of the screen capture
	 * @param element with the scroll bar 
	 */
	public static void captureScrollElement(final WebDriver driver, final File imageFile, final WebElement scrollElement) {
		final JavascriptExecutor js = (JavascriptExecutor) driver;
		// check if it's vertical or horizontal scroll
		long ret = (long)js.executeScript("element = arguments[0];"+
				"if((element.scrollWidth > element.clientWidth ) && (parseInt(element.scrollWidth)> 0) && (parseInt(element.scrollWidth) < 99000)){"+
				"if ((element.scrollHeight > element.clientHeight ) && (parseInt(element.scrollHeight)> 0) && (parseInt(element.scrollHeight) < 99000)){return 2;}"+
				"    else{return 0;}} "+
				"else if ((element.scrollHeight > element.clientHeight ) && (parseInt(element.scrollHeight)> 0) && (parseInt(element.scrollHeight) < 99000)){return 1;}" +
				"else {return 3;}", scrollElement);
		if (ret == 3) {
			logger.warn("Element has no scroll bar");
			return;}		
		logger.debug("ret is " + ret);
		
		// get the tool bar height
		long outH = (long)js.executeScript("return screen.availHeight");
		long inH = (long)js.executeScript("return window.innerHeight");
		
		// get whole browser scroll height/width
		long browserScrollH = (long)js.executeScript("return document.documentElement.scrollTop;");
		long browserScrollW = (long)js.executeScript("return document.documentElement.scrollLeft;");		
		// get the scroll bar width					
		long scrollBarWidth =  (long)js.executeScript("var outer = document.createElement(\"div\");"+
			"outer.style.visibility = \"hidden\";"+
			"outer.style.width = \"100px\";"+
			"outer.style.msOverflowStyle = \"scrollbar\";"+
			"document.body.appendChild(outer);"+
			"var widthNoScroll = outer.offsetWidth;"+
			"outer.style.overflow = \"scroll\";"+
			"var inner = document.createElement(\"div\");"+
			"inner.style.width = \"100%\";"+
			"outer.appendChild(inner);"+
			"var widthWithScroll = inner.offsetWidth;"+
			"outer.parentNode.removeChild(outer);"+
			"return widthNoScroll - widthWithScroll;");
		
		// remove the original css and store it
		String cssBackup = (String)js.executeScript("var cssBackup=arguments[0].style.cssText;" +
				"arguments[0].style.border=\"0px\";return cssBackup;"	, scrollElement);
		
		
		// reset all global variables
		final int scrollWidth = ret == 0 ? scrollElement.getSize().width : (int) (scrollElement.getSize().width - scrollBarWidth);
		final int scrollHeight = ((ret == 0)||(ret==2)) ? (int) (scrollElement.getSize().height - scrollBarWidth) : (int) (scrollElement.getSize().height); 
		final Axis scrollDir = ret == 0 ? AdvScreenCapture.Axis.HORIZONTAL : AdvScreenCapture.Axis.VERTICAL; 
		
		
		if (ret == 0) { // horizontally scroll			
			logger.debug("horizontal scrolling");
		} else if (ret == 1){ // vertical scroll			
			logger.debug("vertical scrolling");
		}
		else {
			logger.debug("Dual scrolling");
		}
		
		AdvScreenCapture sc = new AdvScreenCapture( new AdvScreenCapture.Capturer() {
			@Override
			public int[] capture(Rectangle r) {
				System.out.println(r);
				BufferedImage image = captureElement(driver, scrollElement);
				int[] pixels = new int[r.width * r.height];
				try {
					new PixelGrabber(image, 0, 0, r.width, r.height, pixels, 0, r.width).grabPixels();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return pixels;
			}
		});
		
		sc.setDebug(true);
		
		if (ret == 2){ // dual scroll
		// add tolerance to improve the matching possibility 
		sc.setTolerance(140, 140, 140, 140);

		sc.doDualScrollCapture(new Rectangle((int) (scrollElement.getLocation().x-browserScrollW),(int)(scrollElement.getLocation().y+(outH-inH)-browserScrollH),scrollWidth,scrollHeight), 
				imageFile.getPath(), scrollDir, new AdvScreenCapture.IDualScroller() {
					boolean lastScrollV = false;
					boolean lastScrollH = false;
					long topV = 0;
					long topH = 0;
					int countV = 1;
					int countH = 1;
					@Override
					public boolean scroll() {
						logger.debug("scrollV");						
						if (lastScrollV){lastScrollV = false; return false;}
						
						long t = 0;						
						// vertical scrolling				
						 t = (long)js.executeScript( // minus 5 is to add overlapping chance
						"arguments[0].scrollTop= ((arguments[0].clientHeight)*arguments[1])-5;"+
						"if(arguments[0].scrollTop>=arguments[0].scrollHeight){arguments[0].scrollTop=arguments[0].scrollHeight}"+
								"return arguments[0].scrollTop", scrollElement, countV++);						
						 logger.debug(t);		
						 
						if (t == topV){lastScrollV  = true;}					
						topV = t;
						return true;
					}
					
					@Override
					public boolean scrollH() {
						logger.debug("scrollH");
						if (lastScrollH){ return false;}
						
						long t = 0;						
						t = (long)js.executeScript( // minus 5 is to add overlapping chance
								"arguments[0].scrollLeft= ((arguments[0].clientWidth)*arguments[1])-5;"+
								"if(arguments[0].scrollLeft>=arguments[0].scrollWidth){arguments[0].scrollLeft=arguments[0].scrollWidth}"+
										"return arguments[0].scrollLeft", scrollElement, countH++);
						logger.debug(t);
						
						if (t == topH){lastScrollH  = true;return false;}					
						topH = t;
						return true;
					}
					
					@Override
					public void reset() {
						js.executeScript("arguments[0].scrollTop= 0", scrollElement);
						logger.debug("reset");
					}
				}, false); 
		}
		else{ // vertical or horizontal		
		
		sc.doScrollCapture(new Rectangle((int) (scrollElement.getLocation().x-browserScrollW),(int)(scrollElement.getLocation().y+(outH-inH)-browserScrollH),scrollWidth,scrollHeight), 
				imageFile.getPath(), scrollDir, new AdvScreenCapture.IScroller() {
			
			boolean lastScroll = false;
			long top = 0;
			int count = 1;
					
			@Override
			public boolean scroll() {
				// TODO Auto-generated method stub				
				if (lastScroll){return false;}
				
				long t = 0;
				if (scrollDir == AdvScreenCapture.Axis.VERTICAL){
				// vertical scrolling				
				 t = (long)js.executeScript( // minus 5 is to add overlapping chance
				"arguments[0].scrollTop= ((arguments[0].clientHeight)*arguments[1])-5;"+
				"if(arguments[0].scrollTop>=arguments[0].scrollHeight){arguments[0].scrollTop=arguments[0].scrollHeight}"+
						"return arguments[0].scrollTop", scrollElement, count++);
				
				}
				else {// horizontal scrolling
				 t = (long)js.executeScript( // minus 5 is to add overlapping chance
						"arguments[0].scrollLeft= ((arguments[0].clientWidth)*arguments[1])-5;"+
						"if(arguments[0].scrollLeft>=arguments[0].scrollWidth){arguments[0].scrollLeft=arguments[0].scrollWidth}"+
								"return arguments[0].scrollLeft", scrollElement, count++);
				}				
				if (t == top){lastScroll  = true;}					
				top = t;
				return true;
			}
		}, false);		
		}
		// put back the original css
		js.executeScript("arguments[0].style.cssText = arguments[1];", scrollElement, cssBackup);
	}



	
	
	/**
	 * 
	 * @param element The Web element which has a title attribute
	 * @param screenImg The BufferedImage of represent the whole screen page
	 * @return a merged Buffered which add the titleImg to the screenImg
	 */
	private static BufferedImage mergeTitle2Img(WebElement element, BufferedImage screenImg) {
		BufferedImage mergedImg = null;
		Graphics2D mergedCanvas = null;
		int screenImg_w = screenImg.getWidth();
		int screenImg_h = screenImg.getHeight();
		
		int tooltip_x = element.getLocation().getX() + element.getSize().getWidth()/2;
		int tooltip_y = element.getLocation().getY() + element.getSize().getHeight()/2;
		BufferedImage titleImg = createTitleImg(element);
		int tooltip_w = titleImg.getWidth();
		int tooltip_h = titleImg.getHeight();
		
		int mergeImg_w = (screenImg_w >= (tooltip_x + tooltip_w))?screenImg_w:(tooltip_x + tooltip_w);
		int mergeImg_h = (screenImg_h >= (tooltip_y + tooltip_h))?screenImg_h:(tooltip_y + tooltip_h);
		mergedImg = new BufferedImage(mergeImg_w, mergeImg_h, BufferedImage.TYPE_INT_ARGB);
		mergedCanvas = mergedImg.createGraphics();
		mergedCanvas.drawImage(screenImg, 0, 0, null);
		mergedCanvas.drawImage(titleImg, tooltip_x, tooltip_y, null);
		mergedCanvas.dispose();
		return mergedImg;
	}
	
	/**
	 * 
	 * @param element The Web element which has a title attribute
	 * @return	a simulated BufferedImage shows the hovered title 
	 */
	private static BufferedImage createTitleImg(WebElement element) {
		int fontSize = 12;
		String text = element.getAttribute("title");
		Font font = new Font("sans-serif", Font.PLAIN, fontSize);
		int tooltip_h = fontSize + 7;
		//calculate the real width of the title message
		BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tempCanvas = tempImg.createGraphics();
		tempCanvas.setFont(font);
		int tooltip_w = tempCanvas.getFontMetrics().stringWidth(text) + 7;
		
		BufferedImage titleImg = new BufferedImage(tooltip_w, tooltip_h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D titleCanvas = titleImg.createGraphics();
		titleCanvas.setFont(font);
		titleCanvas.setPaint(Color.decode("#F5FCDE"));
		titleCanvas.fillRect(0, 0, tooltip_w, tooltip_h);
		titleCanvas.setPaint(Color.black);
		titleCanvas.draw3DRect(0, 0, tooltip_w-1, tooltip_h-1, true);
		titleCanvas.drawString(text,3,tooltip_h-5);
		titleCanvas.dispose();
		return titleImg;
	}


}
