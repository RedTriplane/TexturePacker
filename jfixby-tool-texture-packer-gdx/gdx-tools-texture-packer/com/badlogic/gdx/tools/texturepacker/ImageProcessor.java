/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.texturepacker;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.jfixby.cmns.api.desktop.ImageAWT;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.FileSystem;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.util.path.AbsolutePath;

public class ImageProcessor {
	static private final BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	static private Pattern indexPattern = Pattern.compile("(.+)_(\\d+)$");

	private final Settings settings;
	private final HashMap<String, Rect> crcs = new HashMap();
	private final Array<Rect> rects = new Array();
	private float scale = 1;

	private final AbsolutePath<FileSystem> rootPath;

	/** @param rootDir Used to strip the root directory prefix from image file names, can be null. */
	public ImageProcessor (final File rootDir, final Settings settings) {
		this.settings = settings;
		this.rootPath = rootDir.getAbsoluteFilePath();

	}

	public ImageProcessor (final Settings settings) {
		this(null, settings);
	}

	/** The image won't be kept in-memory during packing if {@link Settings#limitMemory} is true. */
	public void addImage (final File file) {

		BufferedImage image;
		try {
			image = ImageAWT.readFromFile(file);
		} catch (final IOException e) {
			e.printStackTrace();
			Err.reportError(e);
			image = null;
		}

		// Strip root dir off front of image path.
		if (this.rootPath != null) {
			if (!file.getAbsoluteFilePath().beginsWith(this.rootPath)) {
				throw new RuntimeException("Path '" + file + "' does not start with root: " + this.rootPath);
			}

		}

		// Strip extension.

		final Rect rect = this.addImage(image, file.nameWithoutExtension());
		if (rect != null && this.settings.limitMemory) {
			rect.unloadImage(file);
		}
	}

	/** The image will be kept in-memory during packing.
	 * @see #addImage(File) */
	public Rect addImage (final BufferedImage image, final String name) {
		final Rect rect = this.processImage(image, name);

		if (rect == null) {
			if (!this.settings.silent) {
				L.d("Ignoring blank input image: " + name);
			}
			return null;
		}

		if (this.settings.alias) {
			final String crc = hash(rect.getImage(this));
			final Rect existing = this.crcs.get(crc);
			if (existing != null) {
				if (!this.settings.silent) {
					L.d(rect.name + " (alias of " + existing.name + ")");
				}
				existing.aliases.add(new Alias(rect));
				return null;
			}
			this.crcs.put(crc, rect);
		}

		this.rects.add(rect);
		return rect;
	}

	public void setScale (final float scale) {
		this.scale = scale;
	}

	public Array<Rect> getImages () {
		return this.rects;
	}

	public void clear () {
		this.rects.clear();
		this.crcs.clear();
	}

	/** Returns a rect for the image describing the texture region to be packed, or null if the image should not be packed. */
	public Rect processImage (BufferedImage image, String name) {
		if (this.scale <= 0) {
			throw new IllegalArgumentException("scale cannot be <= 0: " + this.scale);
		}

		int width = image.getWidth(), height = image.getHeight();

		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
			final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			newImage.getGraphics().drawImage(image, 0, 0, null);
			image = newImage;
		}

		final boolean isPatch = name.endsWith(".9");
		final int[] splits = null, pads = null;
		Rect rect = null;
		if (isPatch) {
			Err.reportError("Sorry, not supported yet!");

// // Strip ".9" from file name, read ninepatch split pixels, and strip ninepatch split pixels.
// path = path.substring(0, path.length() - 2);
// splits = this.getSplits(image, path);
// pads = this.getPads(image, path, splits);
// // Strip split pixels.
// width -= 2;
// height -= 2;
// final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
// newImage.getGraphics().drawImage(image, 0, 0, width, height, 1, 1, width + 1, height + 1, null);
// image = newImage;
		}

		// Scale image.
		if (this.scale != 1) {
			final int originalWidth = width, originalHeight = height;
			width = Math.round(width * this.scale);
			height = Math.round(height * this.scale);
			final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			if (this.scale < 1) {
				newImage.getGraphics().drawImage(image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING), 0, 0, null);
			} else {
				final Graphics2D g = (Graphics2D)newImage.getGraphics();
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.drawImage(image, 0, 0, width, height, null);
			}
			image = newImage;
		}

		if (isPatch) {
			// Ninepatches aren't rotated or whitespace stripped.
			rect = new Rect(image, 0, 0, width, height, true);
			rect.splits = splits;
			rect.pads = pads;
			rect.canRotate = false;
		} else {
			rect = this.stripWhitespace(image);
			if (rect == null) {
				return null;
			}
		}

		// Strip digits off end of name and use as index.
		int index = -1;
		if (this.settings.useIndexes) {
// Err.reportError("Sorry, not supported yet!");
			final Matcher matcher = indexPattern.matcher(name);
			if (matcher.matches()) {
				name = matcher.group(1);
				index = Integer.parseInt(matcher.group(2));
			}
		}

		rect.name = name;
		rect.index = index;
		return rect;
	}

	/** Strips whitespace and returns the rect, or null if the image should be ignored. */
	private Rect stripWhitespace (final BufferedImage source) {
		final WritableRaster alphaRaster = source.getAlphaRaster();
		if (alphaRaster == null || (!this.settings.stripWhitespaceX && !this.settings.stripWhitespaceY)) {
			return new Rect(source, 0, 0, source.getWidth(), source.getHeight(), false);
		}
		final byte[] a = new byte[1];
		int top = 0;
		int bottom = source.getHeight();
		if (this.settings.stripWhitespaceX) {
			outer:
			for (int y = 0; y < source.getHeight(); y++) {
				for (int x = 0; x < source.getWidth(); x++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) {
						alpha += 256;
					}
					if (alpha > this.settings.alphaThreshold) {
						break outer;
					}
				}
				top++;
			}
			outer:
			for (int y = source.getHeight(); --y >= top;) {
				for (int x = 0; x < source.getWidth(); x++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) {
						alpha += 256;
					}
					if (alpha > this.settings.alphaThreshold) {
						break outer;
					}
				}
				bottom--;
			}
		}
		int left = 0;
		int right = source.getWidth();
		if (this.settings.stripWhitespaceY) {
			outer:
			for (int x = 0; x < source.getWidth(); x++) {
				for (int y = top; y < bottom; y++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) {
						alpha += 256;
					}
					if (alpha > this.settings.alphaThreshold) {
						break outer;
					}
				}
				left++;
			}
			outer:
			for (int x = source.getWidth(); --x >= left;) {
				for (int y = top; y < bottom; y++) {
					alphaRaster.getDataElements(x, y, a);
					int alpha = a[0];
					if (alpha < 0) {
						alpha += 256;
					}
					if (alpha > this.settings.alphaThreshold) {
						break outer;
					}
				}
				right--;
			}
		}
		final int newWidth = right - left;
		final int newHeight = bottom - top;
		if (newWidth <= 0 || newHeight <= 0) {
			if (this.settings.ignoreBlankImages) {
				return null;
			} else {
				return new Rect(emptyImage, 0, 0, 1, 1, false);
			}
		}
		return new Rect(source, left, top, newWidth, newHeight, false);
	}

	static private String splitError (final int x, final int y, final int[] rgba, final String name) {
		throw new RuntimeException("Invalid " + name + " ninepatch split pixel at " + x + ", " + y + ", rgba: " + rgba[0] + ", "
			+ rgba[1] + ", " + rgba[2] + ", " + rgba[3]);
	}

	/** Returns the splits, or null if the image had no splits or the splits were only a single region. Splits are an int[4] that
	 * has left, right, top, bottom. */
	private int[] getSplits (final BufferedImage image, final String name) {
		final WritableRaster raster = image.getRaster();

		int startX = getSplitPoint(raster, name, 1, 0, true, true);
		int endX = getSplitPoint(raster, name, startX, 0, false, true);
		int startY = getSplitPoint(raster, name, 0, 1, true, false);
		int endY = getSplitPoint(raster, name, 0, startY, false, false);

		// Ensure pixels after the end are not invalid.
		getSplitPoint(raster, name, endX + 1, 0, true, true);
		getSplitPoint(raster, name, 0, endY + 1, true, false);

		// No splits, or all splits.
		if (startX == 0 && endX == 0 && startY == 0 && endY == 0) {
			return null;
		}

		// Subtraction here is because the coordinates were computed before the 1px border was stripped.
		if (startX != 0) {
			startX--;
			endX = raster.getWidth() - 2 - (endX - 1);
		} else {
			// If no start point was ever found, we assume full stretch.
			endX = raster.getWidth() - 2;
		}
		if (startY != 0) {
			startY--;
			endY = raster.getHeight() - 2 - (endY - 1);
		} else {
			// If no start point was ever found, we assume full stretch.
			endY = raster.getHeight() - 2;
		}

		if (this.scale != 1) {
			startX = Math.round(startX * this.scale);
			endX = Math.round(endX * this.scale);
			startY = Math.round(startY * this.scale);
			endY = Math.round(endY * this.scale);
		}

		return new int[] {startX, endX, startY, endY};
	}

	/** Returns the pads, or null if the image had no pads or the pads match the splits. Pads are an int[4] that has left, right,
	 * top, bottom. */
	private int[] getPads (final BufferedImage image, final String name, final int[] splits) {
		final WritableRaster raster = image.getRaster();

		final int bottom = raster.getHeight() - 1;
		final int right = raster.getWidth() - 1;

		int startX = getSplitPoint(raster, name, 1, bottom, true, true);
		int startY = getSplitPoint(raster, name, right, 1, true, false);

		// No need to hunt for the end if a start was never found.
		int endX = 0;
		int endY = 0;
		if (startX != 0) {
			endX = getSplitPoint(raster, name, startX + 1, bottom, false, true);
		}
		if (startY != 0) {
			endY = getSplitPoint(raster, name, right, startY + 1, false, false);
		}

		// Ensure pixels after the end are not invalid.
		getSplitPoint(raster, name, endX + 1, bottom, true, true);
		getSplitPoint(raster, name, right, endY + 1, true, false);

		// No pads.
		if (startX == 0 && endX == 0 && startY == 0 && endY == 0) {
			return null;
		}

		// -2 here is because the coordinates were computed before the 1px border was stripped.
		if (startX == 0 && endX == 0) {
			startX = -1;
			endX = -1;
		} else {
			if (startX > 0) {
				startX--;
				endX = raster.getWidth() - 2 - (endX - 1);
			} else {
				// If no start point was ever found, we assume full stretch.
				endX = raster.getWidth() - 2;
			}
		}
		if (startY == 0 && endY == 0) {
			startY = -1;
			endY = -1;
		} else {
			if (startY > 0) {
				startY--;
				endY = raster.getHeight() - 2 - (endY - 1);
			} else {
				// If no start point was ever found, we assume full stretch.
				endY = raster.getHeight() - 2;
			}
		}

		if (this.scale != 1) {
			startX = Math.round(startX * this.scale);
			endX = Math.round(endX * this.scale);
			startY = Math.round(startY * this.scale);
			endY = Math.round(endY * this.scale);
		}

		final int[] pads = new int[] {startX, endX, startY, endY};

		if (splits != null && Arrays.equals(pads, splits)) {
			return null;
		}

		return pads;
	}

	/** Hunts for the start or end of a sequence of split pixels. Begins searching at (startX, startY) then follows along the x or
	 * y axis (depending on value of xAxis) for the first non-transparent pixel if startPoint is true, or the first transparent
	 * pixel if startPoint is false. Returns 0 if none found, as 0 is considered an invalid split point being in the outer border
	 * which will be stripped. */
	static private int getSplitPoint (final WritableRaster raster, final String name, final int startX, final int startY,
		final boolean startPoint, final boolean xAxis) {
		final int[] rgba = new int[4];

		int next = xAxis ? startX : startY;
		final int end = xAxis ? raster.getWidth() : raster.getHeight();
		final int breakA = startPoint ? 255 : 0;

		int x = startX;
		int y = startY;
		while (next != end) {
			if (xAxis) {
				x = next;
			} else {
				y = next;
			}

			raster.getPixel(x, y, rgba);
			if (rgba[3] == breakA) {
				return next;
			}

			if (!startPoint && (rgba[0] != 0 || rgba[1] != 0 || rgba[2] != 0 || rgba[3] != 255)) {
				splitError(x, y, rgba, name);
			}

			next++;
		}

		return 0;
	}

	static private String hash (BufferedImage image) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA1");

			// Ensure image is the correct format.
			final int width = image.getWidth();
			final int height = image.getHeight();
			if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
				final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				newImage.getGraphics().drawImage(image, 0, 0, null);
				image = newImage;
			}

			final WritableRaster raster = image.getRaster();
			final int[] pixels = new int[width];
			for (int y = 0; y < height; y++) {
				raster.getDataElements(0, y, width, 1, pixels);
				for (int x = 0; x < width; x++) {
					hash(digest, pixels[x]);
				}
			}

			hash(digest, width);
			hash(digest, height);

			return new BigInteger(1, digest.digest()).toString(16);
		} catch (final NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	static private void hash (final MessageDigest digest, final int value) {
		digest.update((byte)(value >> 24));
		digest.update((byte)(value >> 16));
		digest.update((byte)(value >> 8));
		digest.update((byte)value);
	}
}
