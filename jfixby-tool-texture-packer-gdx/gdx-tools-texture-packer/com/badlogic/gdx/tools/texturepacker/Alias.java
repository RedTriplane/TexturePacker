package com.badlogic.gdx.tools.texturepacker;


/** @author Regnarock
 * @author Nathan Sweet */
public class Alias implements Comparable<Alias> {
	public String name;
	public int index;
	public int[] splits;
	public int[] pads;
	public int offsetX, offsetY, originalWidth, originalHeight;

	public Alias (Rect rect) {
		name = rect.name;
		index = rect.index;
		splits = rect.splits;
		pads = rect.pads;
		offsetX = rect.offsetX;
		offsetY = rect.offsetY;
		originalWidth = rect.originalWidth;
		originalHeight = rect.originalHeight;
	}

	public void apply (Rect rect) {
		rect.name = name;
		rect.index = index;
		rect.splits = splits;
		rect.pads = pads;
		rect.offsetX = offsetX;
		rect.offsetY = offsetY;
		rect.originalWidth = originalWidth;
		rect.originalHeight = originalHeight;
	}

	public int compareTo (Alias o) {
		return name.compareTo(o.name);
	}
}