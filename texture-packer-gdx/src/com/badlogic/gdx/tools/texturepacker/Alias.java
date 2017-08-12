
package com.badlogic.gdx.tools.texturepacker;

/** @author Regnarock
 * @author Nathan Sweet */
public class Alias implements Comparable<Alias> {
	public String name;
	public int index;
	public int[] splits;
	public int[] pads;
	public int offsetX, offsetY, originalWidth, originalHeight;

	public Alias (final Rect rect) {
		this.name = rect.name;
		this.index = rect.index;
		this.splits = rect.splits;
		this.pads = rect.pads;
		this.offsetX = rect.offsetX;
		this.offsetY = rect.offsetY;
		this.originalWidth = rect.originalWidth;
		this.originalHeight = rect.originalHeight;
	}

	public void apply (final Rect rect) {
		rect.name = this.name;
		rect.index = this.index;
		rect.splits = this.splits;
		rect.pads = this.pads;
		rect.offsetX = this.offsetX;
		rect.offsetY = this.offsetY;
		rect.originalWidth = this.originalWidth;
		rect.originalHeight = this.originalHeight;
	}

	@Override
	public int compareTo (final Alias o) {
		return this.name.toString().compareTo(o.name.toString());
	}
}
