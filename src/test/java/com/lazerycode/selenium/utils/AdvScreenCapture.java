package com.lazerycode.selenium.utils;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Provide advanced screen capture capabilities based on Java Robot class.
 * @author rophy
 *
 */
public class AdvScreenCapture {

	public enum Axis {
		HORIZONTAL,
		VERTICAL;
	
		Axis invert() {
			return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
		}
	
	}
	
	/**
	 * Interface which defines how to scroll panel.
	 * @author rophy
	 *
	 */
	public interface IScroller 
	{
		/**
		 * Implement your logic to scroll the panel by one step.
		 * For example, you may click the scroll down button in the method,
		 * or press "Page Down", or press "Down", etc.
		 * 
		 * Note: make sure each call to scroll() should scroll <b>less</b> than one full page,
		 * otherwise the contents will be missed.
		 * 
		 * @return true if scrolled, false otherwise.
		 */
		public boolean scroll();
	}
	
	
	/**
	 * Interface which defines how to scroll a dual-scrolling panel.
	 * @author rophy
	 *
	 */
	public interface IDualScroller extends IScroller {
		
		/**
		 * Implement your logic which resets vertical scroll position to top.
		 *
		 */
		void reset();
		
		/**
		 * Implement your logic which horizontally scrolls the panel here.
		 * @return true if scrolled, false otherwise.
		 */
		boolean scrollH();
	}
	
	private static AdvScreenCapture defaultInstance = null;
	public static AdvScreenCapture getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new AdvScreenCapture(new RobotCapturer());
		}
		return defaultInstance;
	}
	
	
	private Capturer capturer;
	public AdvScreenCapture(Capturer capturer) {
		this.capturer = capturer;
	}

	protected Capturer getCapturer() {
		return this.capturer;
	}
	
	private boolean debug = false;
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Take screen capture for the whole screen
	 * @param file
	 */
	public void doCapture(String imageFile) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width;
		int height = screenSize.height;
		doCapture(new Rectangle(0,0,width,height), imageFile);
	}

	
	/**
	 * Take screen capture for the specified region on screen
	 * @param area A region on screen
	 * @param file Output image file. Example: "panel.png"
	 */
	public void doCapture(Rectangle area, String imageFile) {
		int[] pixels = getCapturer().capture(area);
		RenderedImage rImage = createRenderedImage(pixels, area.width, area.height);
		saveRenderedImage(rImage, imageFile);
	}
	
	/**
	 * Perform a scrolling screen capture. 
	 * @param area A region on screen, excluding the scroll bar.
	 * @param imageFile Output image file. Example: "panel.png"
	 * @param axis Scroll vertiically or horizontally?
	 * @param scroller Implement your logic to perform the actual scrolling.
	 */
	public void doScrollCapture(Rectangle area, String imageFile, Axis axis, IScroller scroller) {
		doScrollCapture(area, imageFile, axis, scroller, true);
	}


	/**
	 * Perform a scrolling screen capture. 
	 * @param area A region on screen, excluding the scroll bar.
	 * @param imageFile Output image file. Example: "panel.png"
	 * @param axis Scroll vertiically or horizontally?
	 * @param scroller Implement your logic to perform the actual scrolling.
	 * @param autoStopScroll if true (default), will stop when the scrolling doesn't change the result image anymore.
	 */
	public void doScrollCapture(Rectangle area, String imageFile, Axis axis, IScroller scroller, boolean autoStopScroll) {
		RenderedImage rImage = doScrollCapture(area, axis, scroller, autoStopScroll);
		saveRenderedImage(rImage, imageFile);
	}


	public RenderedImage doScrollCapture(Rectangle r, Axis axis, IScroller scroller, boolean autoStopScroll) {
		ImageData imageData = createImageData(r.width, r.height, axis);
		imageData.doScrollCaptureImageData(axis, r, scroller, autoStopScroll, true);
		imageData.createMinimalCombinedCapture();
		return imageData.createRenderedImage();
	}


	public void doDualScrollCapture(Rectangle area, String imageFile, Axis axis, IDualScroller scroller, boolean autoStopScroll) {
		RenderedImage rImage = doDualScrollCapture(area, axis, scroller, autoStopScroll);
		saveRenderedImage(rImage, imageFile);
	}

	/**
	 * Perform dual scrolling capture.
	 */
	public RenderedImage doDualScrollCapture(Rectangle r, Axis axis, IDualScroller scroller, boolean autoStopScroll) {
	
		ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
		
		ImageData firstImageData = createImageData(r.width, r.height, axis);
		firstImageData.doScrollCaptureImageData(axis, r, scroller, autoStopScroll, false);
		firstImageData.createMinimalCombinedCapture();
		imageDatas.add(firstImageData);
		
		int width, height;
		
		width = firstImageData.combinedWidth;
		height = firstImageData.combinedHeight;
	
		while( true ) {
			
			scroller.reset();
	
			boolean scrolled = scroller.scrollH();
			
			int offset = 0;
			
			if ( autoStopScroll || scrolled ) {
				
				ImageData secondImageData = createImageData(r.width, r.height, axis);
				secondImageData.doScrollCaptureImageData(axis, r, scroller, autoStopScroll,false);
				secondImageData.createMinimalCombinedCapture();
				imageDatas.add(secondImageData);
				
				if ( autoStopScroll ) {
					int[] firstPixels = firstImageData.combinedCapture;
					int[] secondPixels = secondImageData.combinedCapture;
					offset = getOverlappingOffsets(axis, firstPixels, secondPixels, width, height, 0, true)[0];					
				}
				
				firstImageData = secondImageData;
	
			}
				
			if ( (autoStopScroll && offset==0) || (!autoStopScroll && !scrolled) ) { 
	
				ImageData combinedImageData = createCombinedImageData(imageDatas, axis);
				return combinedImageData.createRenderedImage();
	
			}
			
		}
	}
	
	public void setTolerance(int alpha, int red, int green, int blue) {
		tolerance_alpha = alpha;
		tolerance_red = red;
		tolerance_green = green;
		tolerance_blue = blue;
	}
	
	public int getTolerance() {
		return tolerance_alpha<<24 | tolerance_red<<16 | tolerance_green<<8 | tolerance_blue;
	}


	
	private static int tolerance_alpha = 0;
	private static int tolerance_red = 0;
	private static int tolerance_blue = 0;
	private static int tolerance_green = 0;
	

	protected static void saveRenderedImage(RenderedImage rImage, String imageFile) {
		String fileExt = imageFile.substring(imageFile.lastIndexOf('.')+1);
		try {
			ImageIO.write(rImage, fileExt.toLowerCase(), new File(imageFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}


	private static void combineTargetToBase(Axis axis, int[] base, int baseWidth, int baseHeight, int baseOffset, int[] target, int targetWidth, int targetHeight, int targetOffset) {
		if ( axis == Axis.HORIZONTAL ) {
//			assert(baseHeight==targetHeight);
			for (int y=0; y<targetHeight; y++) {
				int offset = targetWidth-targetOffset;
				for (int x=offset; x<targetWidth; x++) {
					base[baseOffset+x-offset+y*baseWidth] = target[x+y*targetWidth];
				}
			}
		} else {
//			assert(baseWidth==targetWidth);
			int offset = (targetHeight-targetOffset)*targetWidth;
			for (int n=offset; n<target.length; n++) {
				base[baseOffset*baseWidth+n-offset] = target[n];
			}
		}
	}
	private ImageData createCombinedImageData(List<ImageData> imageDatas, Axis axis) {
		
		int size = imageDatas.size();
	
//		assert( size>0 );
	
		if ( size == 1 ) {
			return imageDatas.get(0);
		}
		
		for (ImageData imageData : imageDatas ) {
			imageData.createMinimalCombinedCapture();
		}
		
		int width;
		int which = 0;
	
		if ( axis == Axis.VERTICAL ) {
			width = imageDatas.get(0).combinedHeight;
			for (int i=1; i<size; i++) {
				int currWidth = imageDatas.get(i).combinedHeight;
				if ( currWidth > width ) {
					width = currWidth;
					which = i;
				}
			}
		} else {
			width = imageDatas.get(0).combinedWidth;
			for (int i=1; i<size; i++) {
				int currWidth = imageDatas.get(i).combinedWidth;
				if ( currWidth > width ) {
					width = currWidth;
					which = i;
				}
			}
		}
		
		
		List<List<int[]>> possibleCombinations = new ArrayList<List<int[]>>();
		for (int i=0; i<size; i++) {
			if ( i != which ) {
				List<int[]> result = imageDatas.get(i).calculatePossibleCombinedCaptures(width);
				if ( result.size() == 0 ) {
					// no possible result, something must be wrong
					return null;
				}
				
				if ( result.size() == 1 ) {
					imageDatas.get(i).createCombinedCapture( result.get(0) );
				}
				possibleCombinations.add(result);
			} else {
				List<int[]> combo = new ArrayList<int[]>();
				combo.add(new int[size-1]);
				possibleCombinations.add(combo);
			}
		}
		
		// at here, all imageDatas should have the same width
		
		
		int[] offsets = new int[size-1];
		for (int i=0; i<size-1; i++) {
			ImageData firstCapture = imageDatas.get(i);
			ImageData secondCapture = imageDatas.get(i+1);
			List<int[]> firstCombo = possibleCombinations.get(i);
			List<int[]> secondCombo = possibleCombinations.get(i+1);
			
			// try to get the smallest offset
			int minOffset = -1;
			int[] firstBestCombinedCapture = null;
			int firstBestCombinedWidth = 0;
			int firstBestCombinedHeight = 0;
			int[] secondBestCombinedCapture = null;
			int secondBestCombinedWidth = 0;
			int secondBestCombinedHeight = 0;
			for( int j=0; j<firstCombo.size(); j++) {
				int[] firstOffset = firstCombo.get(j);
				for( int k=0; k<secondCombo.size(); k++) {
					int[] secondOffset = secondCombo.get(k);
					if ( firstCombo.size() > 1 ) {
						firstCapture.createCombinedCapture(firstOffset);
					}
					if ( secondCombo.size() > 1 ) {
						secondCapture.createCombinedCapture(secondOffset);
					}
					int offset = getOverlappingOffsets(axis.invert(), firstCapture.combinedCapture, secondCapture.combinedCapture, firstCapture.combinedWidth, firstCapture.combinedHeight, 0, true)[0];
					if ( minOffset == -1 || offset < minOffset ) {
						minOffset = offset;
						firstBestCombinedCapture = firstCapture.combinedCapture;
						firstBestCombinedWidth = firstCapture.combinedWidth;
						firstBestCombinedHeight = firstCapture.combinedHeight;
						secondBestCombinedCapture = secondCapture.combinedCapture;
						secondBestCombinedWidth = secondCapture.combinedWidth;
						secondBestCombinedHeight = secondCapture.combinedHeight;
					}
				}
			}
			offsets[i] = minOffset;
			firstCapture.setCombinedCapture(firstBestCombinedWidth, firstBestCombinedHeight, firstBestCombinedCapture);
			secondCapture.setCombinedCapture(secondBestCombinedWidth, secondBestCombinedHeight, secondBestCombinedCapture);
			
		}
		
		int combinedWidth = imageDatas.get(0).combinedWidth;
		int combinedHeight = imageDatas.get(0).combinedHeight;
		
		for (int i=0; i<offsets.length; i++) {
			if ( axis == Axis.VERTICAL ) {
				combinedWidth += offsets[i];
			} else {
				combinedHeight += offsets[i];
			}
		}
		
		int[] combinedPixels = new int[combinedWidth*combinedHeight];
		ImageData imageData = imageDatas.get(0);
		int length = axis == Axis.VERTICAL ? imageData.combinedWidth : imageData.combinedHeight;
		combineTargetToBase(axis.invert(), combinedPixels, combinedWidth, combinedHeight, 0, imageData.combinedCapture, imageData.combinedWidth, imageData.combinedHeight, length);
		int baseOffset = length;
		
		
		for( int i=1; i<size; i++) {
			imageData = imageDatas.get(i);
//			assert(imageData.combinedWidth == width);
			int offset = offsets[i-1];
			combineTargetToBase(axis.invert(), combinedPixels, combinedWidth, combinedHeight, baseOffset, imageData.combinedCapture, imageData.combinedWidth, imageData.combinedHeight, offset);
			baseOffset +=  offset;
		}
		return  createImageData(combinedPixels, combinedWidth, combinedHeight);
	
	}
	private static RenderedImage createRenderedImage(int[] pixels, int width, int height) {
		ColorModel colorModel = ColorModel.getRGBdefault();
		MemoryImageSource mis = new MemoryImageSource(width, height, colorModel, pixels, 0, width);
		Image ret = Toolkit.getDefaultToolkit().createImage(mis);
		BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics g = bimage.createGraphics();
	    g.drawImage(ret, 0, 0, null);
	    g.dispose(); 
		return bimage;
	}

	private static int[] getOverlapOffsetH(int[] origPixels, int[] compPixels, int width, int height, int offset, boolean stopOnFirstMatch) {
		
		int k,x,y,n;
		
		int[] offsets = new int[width];
		n = 0;
		
		// TODO: performance can be improved by using a binary search based approach
		// i.e. log2 complexity in the main loop.
		k: for (k=0; k<width; k++) {
			for (x=0; x<width-k; x++) {
				for (y=0; y<height; y++) {
					if ( isDifferent(origPixels[x+k+offset+y*width], compPixels[x+y*width]) ) { 
						continue k;
					}
				}
			}
			offsets[n++] = width-x;
			if ( stopOnFirstMatch ) {
				break;
			}
		}
		
		offsets[n++] = width;
		int[] result = new int[n];
		for (int i=0; i<n; i++) {
			result[i] = offsets[i];
		}
		return result;
	}
	
	protected static boolean isDifferent(int pixel1, int pixel2) {
		if ( Math.abs( ((pixel1>>24)&0xff) - ((pixel2>>24)&0xff) ) > tolerance_alpha ) {
			return true;
		}
		if ( Math.abs( ((pixel1>>16)&0xff) - ((pixel2>>16)&0xff) ) > tolerance_red ) {
			return true;
		}
		if ( Math.abs( ((pixel1>>8)&0xff) - ((pixel2>>8)&0xff) ) > tolerance_green ) {
			return true;
		}
		if ( Math.abs( ((pixel1)&0xff) - ((pixel2)&0xff) ) > tolerance_blue) {
			return true;
		}
		return false;
	}
	
	protected static int[] getOverlapOffsetV(int[] origPixels, int[] compPixels, int width, int height, int offset, boolean stopOnFirstMatch) {
		
		int k,x,y,n;
		
		int[] offsets = new int[height];
		n = 0;
		
		// TODO: performance can be improved by using a binary search based approach
		// i.e. log2 complexity in the main loop.
		k: for (k=0; k<height; k++) {
			for (y=0; y<height-k; y++) {
				for (x=0; x<width; x++) {
					if ( isDifferent(origPixels[x+(y+k+offset)*width], compPixels[x+y*width]) ) { 
						continue k;
					}
				}
			}
			offsets[n++] = height-y;
			if ( stopOnFirstMatch ) {
				break;
			}
		}
		
		offsets[n++] = height;
		int[] result = new int[n];
		for (int i=0; i<n; i++) {
			result[i] = offsets[i];
		}
		return result;
	}
	
	
	/**
	 * return the possible overlapping offsets for y-axis. 
	 * offset~height of origPixels overlaps with 0~(height-offset) of compPixels
	 * a return value of 0 means compPixels fully overlaps origPixels.
	 * @param origPixels the base image
	 * @param compPixels the comparing image
	 * @param width width of comparing image and base image
	 * @param height height of comparing image (height of base image expected to be offset+height)
	 * @param offset y-offset of base image to compare with comparing image
	 * @return overlappingOffset of y-axis
	 */
	protected static int[] getOverlappingOffsets(Axis axis, int[] origPixels, int[] compPixels, int width, int height, int offset, boolean stopOnFirstMatch) {
		if ( axis == Axis.VERTICAL ) {
			return getOverlapOffsetV(origPixels, compPixels, width, height, offset, stopOnFirstMatch);
		} else {
			return getOverlapOffsetH(origPixels, compPixels, width, height, offset, stopOnFirstMatch);
		}
	}
	
	public interface Capturer {
		int[] capture(Rectangle r);
	}


	private static class RobotCapturer implements Capturer {
		Robot robot;
		RobotCapturer() {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
		public int[] capture(Rectangle r) {
			BufferedImage image = robot.createScreenCapture(r);
			int[] pixels = new int[r.width * r.height];
			try {
				new PixelGrabber(image, 0, 0, r.width, r.height, pixels, 0, r.width).grabPixels();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return pixels;
			
		}		
	}
	
	
	/**
	 * Class for implementation of scrolling capture 
	 * @author rophy
	 *
	 */
	private class ImageData 
	{
		
		
		private Axis axis;
	
		// Each capture represents 1 screenCapture created by Robot, in sequential.
		private List<int[]> captures;
	
		
		// Width and height of each capture.
		private int captureWidth, captureHeight;
	
		// Combined final image pixels.
		private int[] combinedCapture;
		
		private int combinedWidth, combinedHeight;
		
		
		// overlappingOffsets[0] = possible offsets between captures[0] and captures[1].
		private List<int[]> overlappingOffsets;
		
		private ImageData(int width, int height, Axis axis) {
			this.captureWidth = width;
			this.captureHeight = height;
			this.captures = new ArrayList<int[]>();
			this.overlappingOffsets = new ArrayList<int[]>();
			this.axis = axis;
			
		}
		
		private ImageData(int[] pixels, int totalWidth, int totalHeight) {
			this.combinedCapture = pixels;
			this.combinedWidth = totalWidth;
			this.combinedHeight = totalHeight;
		}
		
		
		private void addCapturedData(int[] capture, boolean minimal) {
			if ( getCapturedCount() > 0 ) {
				int firstOne = this.captures.size()-1;
				int[] offsets = getOverlappingOffsets(this.axis, this.captures.get(firstOne), capture, this.captureWidth, this.captureHeight, 0, minimal);
				overlappingOffsets.add(offsets);
				// if capture fully overlaps with previous capture...
				if ( offsets[0] == 0 ) {
					// simply reference to previous one to save memory.
					this.captures.add( this.captures.get(firstOne) );
					return;
				}
			}
			this.captures.add(capture);
		}
		
		
		/**
		 * 
		 * @param offsets if method returns true, offsets will contain the offset to create target height, if method returns false, offset's value will be undefined.
		 * @param which
		 * @param currHeight
		 * @param targetHeight
		 * @return
		 */
		private boolean calculateOffsetsRecur(List<int[]> possibleList, int[] offsets, int which, int currHeight, int targetHeight) {
			
			int captureLength = axis == Axis.VERTICAL ? captureHeight : captureWidth; 
			
			
			// an attempt to stop recursing in the middle
			if ( captureLength + currHeight > targetHeight ) {
				return false;
			}
			
			int beforeSize = possibleList.size();
			
			// stop recur'ing if we're at the last capture.
			if ( which == offsets.length ) {
				if (captureLength + currHeight == targetHeight) {
					int[] resultOffsets = new int[offsets.length];
					System.arraycopy(offsets, 0, resultOffsets, 0, offsets.length);
					possibleList.add(resultOffsets);
					return true;
				} else {
					return false;
				}
				
			} else {
				int[] overlappingOffsets = this.overlappingOffsets.get(which);
				for( int i=0; i<overlappingOffsets.length; i++) {
					int thisHeight = overlappingOffsets[i];
					offsets[which] = i;
					calculateOffsetsRecur(possibleList, offsets, which+1, currHeight+thisHeight, targetHeight);
				}
			}
			
			return possibleList.size() > beforeSize;
		}
		
		/**
		 * Given a target height, calculate the possible combined captures (in form of offsetIdx arrays)
		 * captures.size() must be >= 2 (otherwise it doesn't make sense to have offset array).
		 * @param targetHeight the target height
		 * @return a list of possible offests
		 */
		private List<int[]> calculatePossibleCombinedCaptures(int targetHeight) {
			
			int size = this.captures.size();
			ArrayList<int[]> possibleOffsetIdx = new ArrayList<int[]>();
	
			if ( size < 2 ) {
				possibleOffsetIdx.add( new int[size-1]);
			} else {
				int[] offsets = new int[size-1];
				for (int i=0; i<offsets.length; i++) {
					offsets[i] = 0;
				}
				calculateOffsetsRecur(possibleOffsetIdx, offsets, 0, 0, targetHeight);
			}
			return possibleOffsetIdx;
		}
		
		// *** Private non- methods below ***
		
		private void createCombinedCapture(int[] offsetIdx) {
			if ( this.axis == Axis.VERTICAL ) {
				createCombinedCaptureV(offsetIdx);
			} else{
				createCombinedCaptureH(offsetIdx);
			}
			
		}
		private void createCombinedCaptureH(int[] offsetIdx) {
			int size = this.captures.size();
			
//			assert(size>0);
	
			if ( size == 1 ) {
				this.combinedCapture = captures.get(0);
				this.combinedWidth = this.captureWidth;
				this.combinedHeight = this.captureHeight;
				return;
			}
			
			
			int totalWidth = this.captureWidth;
			int totalHeight = this.captureHeight;
			for( int i=0; i<size-1; i++) {
				totalWidth += getPossibleOverlappingOffses(i)[offsetIdx[i]];
			}
			int[] combinedPixels = new int[totalWidth * totalHeight];
			
			int[] capture = this.captures.get(0);
			int width = this.captureWidth;
			int height = this.captureHeight;
			combineTargetToBase(axis, combinedPixels, totalWidth, totalHeight, 0, capture, width, height, width);
			
			int xOffset = width;
			
			for( int i=1; i<size; i++) {
				capture = this.captures.get(i);
				int offset = getPossibleOverlappingOffses(i-1)[offsetIdx[i-1]];
				combineTargetToBase(axis, combinedPixels, totalWidth, totalHeight, xOffset, capture, width, height, offset);
				xOffset += offset;
			}
			this.combinedWidth = totalWidth;
			this.combinedHeight = totalHeight;
			this.combinedCapture = combinedPixels;
		}
		
		private void createCombinedCaptureV(int[] offsetIdx) {
			int size = this.captures.size();
			
//			assert(size>0);
	
			if ( size == 1 ) {
				this.combinedCapture = captures.get(0);
				this.combinedWidth = this.captureWidth;
				this.combinedHeight = this.captureHeight;
				return;
			}
			
			
			int totalWidth = this.captureWidth;
			int totalHeight = this.captureHeight;
			for( int i=0; i<size-1; i++) {
				totalHeight += getPossibleOverlappingOffses(i)[offsetIdx[i]];
			}
			int combinedPixelSize = totalWidth * totalHeight;
			int[] combinedPixels = new int[combinedPixelSize];
			
			int width = this.captureWidth;
			int height = this.captureHeight;
	
			int[] capture = this.captures.get(0);
			combineTargetToBase(axis, combinedPixels, totalWidth, totalHeight, 0, capture, width, height, height);
			int baseOffset = height;
			
			for( int i=1; i<size; i++) {
				capture = this.captures.get(i);
				int offset = getPossibleOverlappingOffses(i-1)[offsetIdx[i-1]];
				combineTargetToBase(axis, combinedPixels, totalWidth, totalHeight, baseOffset, capture, width, height, offset);
				baseOffset += offset;
			}
			setCombinedCapture(totalWidth, totalHeight, combinedPixels);
		}
		private void createMinimalCombinedCapture() {
			createCombinedCapture(new int[this.captures.size()]);
		}
	
		private RenderedImage createRenderedImage() {
			return AdvScreenCapture.createRenderedImage(this.combinedCapture, this.combinedWidth, this.combinedHeight);
		}
		/**
		 * The core algorithm for scrolling capture. 
		 */
		private  void doScrollCaptureImageData(Axis axis, Rectangle r, IScroller scroller, boolean autoStopScroll, boolean minimal) {
	
			int[] firstPixels = getCapturer().capture(r);
			
			if (debug) {
				saveRenderedImage(AdvScreenCapture.createRenderedImage(firstPixels, r.width, r.height), createTempFile());
			}
	
			addCapturedData(firstPixels, minimal);
	
	
			while (true) {	
				
				
				boolean scrolled = scroller.scroll();
				
				if ( autoStopScroll || scrolled ) {
	
					int[] secondPixels = getCapturer().capture(r);
					
					if (debug) {
						saveRenderedImage(AdvScreenCapture.createRenderedImage(secondPixels, r.width, r.height), createTempFile());
					}
					
					addCapturedData(secondPixels,minimal);
					
				} 
				
				if ( (autoStopScroll && isLastCaptureFullyOverlapped()) || (!autoStopScroll && !scrolled) ) { 
					return;
				}
			}
	
		}
		
		private int getCapturedCount() {
			return this.captures.size();
		}
		
		private int[] getPossibleOverlappingOffses(int firstOne) {
//			assert(firstOne < getCapturedCount()-2);
			return this.overlappingOffsets.get(firstOne);
		}
		
		private boolean isLastCaptureFullyOverlapped() {
			int size = getCapturedCount(); 
			if (size<2) {
				return false;
			}
			return overlappingOffsets.get(size-2)[0] == 0;
		}
		
		
		public void setCombinedCapture(int combinedWidth, int combinedHeight, int[] combinedCapture) {
			this.combinedWidth = combinedWidth;
			this.combinedHeight = combinedHeight;
			this.combinedCapture = combinedCapture;
		}
	
	
	
	}

	private ImageData createImageData(int width, int height, Axis axis) {
		return new ImageData(width, height, axis);
	}
	private ImageData createImageData(int[] pixels, int totalWidth, int totalHeight) {
		return new ImageData(pixels, totalWidth, totalHeight);
	}
	
	
	// for debugging
	private int idx = 0;
	private String createTempFile() {
		try {
			String prefix = String.format("AdvScreenCapture-%d-", ++idx);
			String name = File.createTempFile(prefix,".png").getAbsolutePath();
			System.err.println(name);
			return name;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	

}

