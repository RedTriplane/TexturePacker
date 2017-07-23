
package com.badlogic.gdx.tools.texturepacker;

import java.util.Comparator;

import com.badlogic.gdx.tools.texturepacker.MaxRectsPacker;

class RectComparator implements Comparator<Rect> {
	/**
	 *
	 */
	private final MaxRectsPacker maxRectsPacker;

	/** @param maxRectsPacker */
	RectComparator (final MaxRectsPacker maxRectsPacker) {
		this.maxRectsPacker = maxRectsPacker;
	}

	@Override
	public int compare (final Rect o1, final Rect o2) {
		return Rect.getAtlasName(o1.name, this.maxRectsPacker.settings.flattenPaths).toString()
			.compareTo(Rect.getAtlasName(o2.name, this.maxRectsPacker.settings.flattenPaths).toString());
	}
}
