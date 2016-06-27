
package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/** @author Nathan Sweet */
public class Settings {
	public boolean pot = true;
	public int paddingX = 4, paddingY = 4;
	public boolean edgePadding = true;
	public boolean duplicatePadding = true;
	public final boolean rotation = false;
	public int minWidth = 16, minHeight = 16;
	public int maxWidth = 1024, maxHeight = 1024;
	public boolean square = false;
	public boolean stripWhitespaceX = true, stripWhitespaceY = true;
	public int alphaThreshold = 10;
	public TextureFilter filterMin = TextureFilter.MipMapLinearLinear, filterMag = TextureFilter.MipMapLinearLinear;
	public TextureWrap wrapX = TextureWrap.ClampToEdge, wrapY = TextureWrap.ClampToEdge;
	public Format format = Format.RGBA8888;
	public boolean alias = true;
	public String outputFormat = "png";
	public float jpegQuality = 0.95f;
	public boolean ignoreBlankImages = true;
	public boolean fast;
	public boolean debug = false;
	public boolean silent = false;
	public boolean flattenPaths = false;
	public boolean premultiplyAlpha;
	public boolean useIndexes = true;
	public boolean bleed = true;
	public boolean limitMemory = true;
	public boolean grid;
	public float[] scale = {1};
	public String[] scaleSuffix = {""};
	final public static String atlasExtension = ".gdx-atlas";

	public Settings () {
	}

	public Settings (final Settings settings) {
		this.fast = settings.fast;
		// rotation = settings.rotation;
		this.pot = settings.pot;
		this.minWidth = settings.minWidth;
		this.minHeight = settings.minHeight;
		this.maxWidth = settings.maxWidth;
		this.maxHeight = settings.maxHeight;
		this.paddingX = settings.paddingX;
		this.paddingY = settings.paddingY;
		this.edgePadding = settings.edgePadding;
		this.duplicatePadding = settings.duplicatePadding;
		this.alphaThreshold = settings.alphaThreshold;
		this.ignoreBlankImages = settings.ignoreBlankImages;
		this.stripWhitespaceX = settings.stripWhitespaceX;
		this.stripWhitespaceY = settings.stripWhitespaceY;
		this.alias = settings.alias;
		this.format = settings.format;
		this.jpegQuality = settings.jpegQuality;
		this.outputFormat = settings.outputFormat;
		this.filterMin = settings.filterMin;
		this.filterMag = settings.filterMag;
		this.wrapX = settings.wrapX;
		this.wrapY = settings.wrapY;
		this.debug = settings.debug;
		// silent = settings.silent;

		this.flattenPaths = settings.flattenPaths;
		this.premultiplyAlpha = settings.premultiplyAlpha;
		this.square = settings.square;
		this.useIndexes = settings.useIndexes;
		this.bleed = settings.bleed;
		this.limitMemory = settings.limitMemory;
		this.grid = settings.grid;
		this.scale = settings.scale;
		this.scaleSuffix = settings.scaleSuffix;
		// atlasExtension = settings.atlasExtension;
	}

	public String getScaledPackFileName (String packFileName, final int scaleIndex) {
		// Use suffix if not empty string.
		if (this.scaleSuffix[scaleIndex].length() > 0) {
			packFileName += this.scaleSuffix[scaleIndex];
		} else {
			// Otherwise if scale != 1 or multiple scales, use subdirectory.
			final float scaleValue = this.scale[scaleIndex];
			if (this.scale.length != 1) {
				packFileName = (scaleValue == (int)scaleValue ? Integer.toString((int)scaleValue) : Float.toString(scaleValue)) + "/"
					+ packFileName;
			}
		}
		return packFileName;
	}
}
