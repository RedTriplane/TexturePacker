package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
public class Page {
	public String imageName;
	public Array<Rect> outputRects, remainingRects;
	public float occupancy;
	public int x, y, width, height, imageWidth, imageHeight;
}