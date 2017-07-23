package com.badlogic.gdx.tools.texturepacker;

import java.util.NoSuchElementException;

import com.badlogic.gdx.tools.texturepacker.ColorBleedEffect;

class Mask {
	int[] data, pending, changing;
	int pendingSize, changingSize;

	Mask (int[] rgb) {
		data = new int[rgb.length];
		pending = new int[rgb.length];
		changing = new int[rgb.length];
		ARGBColor color = new ARGBColor();
		for (int i = 0; i < rgb.length; i++) {
			color.argb = rgb[i];
			if (color.alpha() == 0) {
				data[i] = ColorBleedEffect.TO_PROCESS;
				pending[pendingSize] = i;
				pendingSize++;
			} else
				data[i] = ColorBleedEffect.REALDATA;
		}
	}

	int getMask (int index) {
		return data[index];
	}

	int removeIndex (int index) {
		if (index >= pendingSize) throw new IndexOutOfBoundsException(String.valueOf(index));
		int value = pending[index];
		pendingSize--;
		pending[index] = pending[pendingSize];
		return value;
	}

	class MaskIterator {
		private int index;

		boolean hasNext () {
			return index < pendingSize;
		}

		int next () {
			if (index >= pendingSize) throw new NoSuchElementException(String.valueOf(index));
			return pending[index++];
		}

		void markAsInProgress () {
			index--;
			changing[changingSize] = removeIndex(index);
			changingSize++;
		}

		void reset () {
			index = 0;
			for (int i = 0; i < changingSize; i++) {
				int index = changing[i];
				data[index] = ColorBleedEffect.REALDATA;
			}
			changingSize = 0;
		}
	}
}