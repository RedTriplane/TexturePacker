package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.utils.Array;

public interface Packer {
	public Array<Page> pack (Array<Rect> inputRects);
}