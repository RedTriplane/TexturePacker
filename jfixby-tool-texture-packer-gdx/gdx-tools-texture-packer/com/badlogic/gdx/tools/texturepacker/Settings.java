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
	public TextureFilter filterMin = TextureFilter.MipMapLinearLinear,
			filterMag = TextureFilter.MipMapLinearLinear;
	public TextureWrap wrapX = TextureWrap.ClampToEdge,
			wrapY = TextureWrap.ClampToEdge;
	public Format format = Format.RGBA8888;
	public boolean alias = true;
	public String outputFormat = "png";
	public float jpegQuality = 0.95f;
	public boolean ignoreBlankImages = true;
	public boolean fast;
	public boolean debug = false;
	public boolean silent = false;
	public boolean flattenPaths = true;
	public boolean premultiplyAlpha;
	public boolean useIndexes = true;
	public boolean bleed = true;
	public boolean limitMemory = true;
	public boolean grid;
	public float[] scale = { 1 };
	public String[] scaleSuffix = { "" };
	final public static String atlasExtension = ".gdx-atlas";

	public Settings() {
	}

	public Settings(Settings settings) {
		fast = settings.fast;
		// rotation = settings.rotation;
		pot = settings.pot;
		minWidth = settings.minWidth;
		minHeight = settings.minHeight;
		maxWidth = settings.maxWidth;
		maxHeight = settings.maxHeight;
		paddingX = settings.paddingX;
		paddingY = settings.paddingY;
		edgePadding = settings.edgePadding;
		duplicatePadding = settings.duplicatePadding;
		alphaThreshold = settings.alphaThreshold;
		ignoreBlankImages = settings.ignoreBlankImages;
		stripWhitespaceX = settings.stripWhitespaceX;
		stripWhitespaceY = settings.stripWhitespaceY;
		alias = settings.alias;
		format = settings.format;
		jpegQuality = settings.jpegQuality;
		outputFormat = settings.outputFormat;
		filterMin = settings.filterMin;
		filterMag = settings.filterMag;
		wrapX = settings.wrapX;
		wrapY = settings.wrapY;
		debug = settings.debug;
		// silent = settings.silent;

		flattenPaths = settings.flattenPaths;
		premultiplyAlpha = settings.premultiplyAlpha;
		square = settings.square;
		useIndexes = settings.useIndexes;
		bleed = settings.bleed;
		limitMemory = settings.limitMemory;
		grid = settings.grid;
		scale = settings.scale;
		scaleSuffix = settings.scaleSuffix;
		// atlasExtension = settings.atlasExtension;
	}

	public String getScaledPackFileName(String packFileName, int scaleIndex) {
		// Use suffix if not empty string.
		if (scaleSuffix[scaleIndex].length() > 0)
			packFileName += scaleSuffix[scaleIndex];
		else {
			// Otherwise if scale != 1 or multiple scales, use subdirectory.
			float scaleValue = scale[scaleIndex];
			if (scale.length != 1) {
				packFileName = (scaleValue == (int) scaleValue ? Integer
						.toString((int) scaleValue) : Float
						.toString(scaleValue))
						+ "/" + packFileName;
			}
		}
		return packFileName;
	}
}