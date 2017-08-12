package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.math.MathUtils;

class BinarySearch {
	int min, max, fuzziness, low, high, current;
	boolean pot;

	public BinarySearch (int min, int max, int fuzziness, boolean pot) {
		this.pot = pot;
		this.fuzziness = pot ? 0 : fuzziness;
		this.min = pot ? (int)(Math.log(MathUtils.nextPowerOfTwo(min)) / Math.log(2)) : min;
		this.max = pot ? (int)(Math.log(MathUtils.nextPowerOfTwo(max)) / Math.log(2)) : max;
	}

	public int reset () {
		low = min;
		high = max;
		current = (low + high) >>> 1;
		return pot ? (int)Math.pow(2, current) : current;
	}

	public int next (boolean result) {
		if (low >= high) return -1;
		if (result)
			low = current + 1;
		else
			high = current - 1;
		current = (low + high) >>> 1;
		if (Math.abs(low - high) < fuzziness) return -1;
		return pot ? (int)Math.pow(2, current) : current;
	}
}