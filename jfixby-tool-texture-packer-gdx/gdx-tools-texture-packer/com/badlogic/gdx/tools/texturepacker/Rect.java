
package com.badlogic.gdx.tools.texturepacker;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.tools.texturepacker.ImageProcessor;
import com.jfixby.scarabei.api.desktop.ImageAWT;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;

public class Rect implements Comparable<Rect> {
	public String name;
	public int offsetX, offsetY, regionWidth, regionHeight, originalWidth, originalHeight;
	public int x, y;
	public int width, height; // Portion of page taken by this region, including padding.
	public int index;
	public boolean rotated;
	public Set<Alias> aliases = new HashSet<Alias>();
	public int[] splits;
	public int[] pads;
	public boolean canRotate = true;

	private boolean isPatch;
	private BufferedImage image;
	private File file;
	int score1, score2;

	Rect (final BufferedImage source, final int left, final int top, final int newWidth, final int newHeight,
		final boolean isPatch) {
		this.image = new BufferedImage(source.getColorModel(),
			source.getRaster().createWritableChild(left, top, newWidth, newHeight, 0, 0, null),
			source.getColorModel().isAlphaPremultiplied(), null);
		this.offsetX = left;
		this.offsetY = top;
		this.regionWidth = newWidth;
		this.regionHeight = newHeight;
		this.originalWidth = source.getWidth();
		this.originalHeight = source.getHeight();
		this.width = newWidth;
		this.height = newHeight;
		this.isPatch = isPatch;
	}

	/** Clears the image for this rect, which will be loaded from the specified file by {@link #getImage(ImageProcessor)}. */
	public void unloadImage (final File file) {
		this.file = file;
		this.image = null;
	}

	public BufferedImage getImage (final ImageProcessor imageProcessor) {
		if (this.image != null) {
			return this.image;
		}

		BufferedImage image;
		try {
			image = ImageAWT.readFromFile(this.file);
		} catch (final IOException ex) {
			throw new RuntimeException("Error reading image: " + this.file, ex);
		}
		if (image == null) {
			throw new RuntimeException("Unable to read image: " + this.file);
		}
		final String name = this.name;
		if (this.isPatch) {
// name += ".9";
			Err.reportError("Sorry, not supported yet!");
		}
		return imageProcessor.processImage(image, name).getImage(null);
	}

	public Rect () {
	}

	public Rect (final Rect rect) {
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;
	}

	public void set (final Rect rect) {
		this.name = rect.name;
		this.image = rect.image;
		this.offsetX = rect.offsetX;
		this.offsetY = rect.offsetY;
		this.regionWidth = rect.regionWidth;
		this.regionHeight = rect.regionHeight;
		this.originalWidth = rect.originalWidth;
		this.originalHeight = rect.originalHeight;
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;
		this.index = rect.index;
		this.rotated = rect.rotated;
		this.aliases = rect.aliases;
		this.splits = rect.splits;
		this.pads = rect.pads;
		this.canRotate = rect.canRotate;
		this.score1 = rect.score1;
		this.score2 = rect.score2;
		this.file = rect.file;
		this.isPatch = rect.isPatch;
	}

	@Override
	public int compareTo (final Rect o) {
		return this.name.toString().compareTo(o.name.toString());
	}

	@Override
	public boolean equals (final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Rect other = (Rect)obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString () {
		return this.name + "[" + this.x + "," + this.y + " " + this.width + "x" + this.height + "]";
	}

	static public String getAtlasName (final String name, final boolean flattenPaths) {
		if (flattenPaths) {
// return new FileHandle(name).name();
			Err.reportError("no flattenPaths");
			return null;
		} else {
			return name;
		}
	}
}
