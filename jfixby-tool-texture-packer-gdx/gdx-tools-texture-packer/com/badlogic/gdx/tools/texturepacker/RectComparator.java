package com.badlogic.gdx.tools.texturepacker;

import java.util.Comparator;

class RectComparator implements Comparator<Rect> {
	/**
	 * 
	 */
	private final MaxRectsPacker maxRectsPacker;

	/**
	 * @param maxRectsPacker
	 */
	RectComparator(MaxRectsPacker maxRectsPacker) {
		this.maxRectsPacker = maxRectsPacker;
	}

	public int compare (Rect o1, Rect o2) {
		return Rect.getAtlasName(o1.name, this.maxRectsPacker.settings.flattenPaths).compareTo(Rect.getAtlasName(o2.name, this.maxRectsPacker.settings.flattenPaths));
	}
}