package com.badlogic.gdx.tools.texturepacker;

class ARGBColor {
	int argb = 0xff000000;

	public int red () {
		return (argb >> 16) & 0xFF;
	}

	public int green () {
		return (argb >> 8) & 0xFF;
	}

	public int blue () {
		return (argb >> 0) & 0xFF;
	}

	public int alpha () {
		return (argb >> 24) & 0xff;
	}

	public void setARGBA (int a, int r, int g, int b) {
		if (a < 0 || a > 255 || r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
			throw new IllegalArgumentException("Invalid RGBA: " + r + ", " + g + "," + b + "," + a);
		argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
	}
}