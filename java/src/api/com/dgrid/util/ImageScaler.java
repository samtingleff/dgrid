package com.dgrid.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageScaler {

	/**
	 * 
	 * @param source
	 * @param dest
	 * @param encoding:
	 *            either png, jpg or gif
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public static void scaleImage(File source, File dest, String encoding,
			int width, int height) throws IOException {
		BufferedImage srcImage = ImageIO.read(source);
		BufferedImage destImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = destImage.createGraphics();
		AffineTransform at = AffineTransform.getScaleInstance((double) width
				/ srcImage.getWidth(), (double) height / srcImage.getHeight());
		g.drawRenderedImage(srcImage, at);
		ImageIO.write(destImage, encoding, dest);
	}
}
