/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.texturepacker;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Sort;
import com.jfixby.cmns.api.log.L;

/** Packs pages of images using the maximal rectangles bin packing algorithm by Jukka Jylänki. A brute force binary search is used
 * to pack into the smallest bin possible.
 * @author Nathan Sweet */
public class MaxRectsPacker implements Packer {
	private RectComparator rectComparator = new RectComparator(this);
	private FreeRectChoiceHeuristic[] methods = FreeRectChoiceHeuristic.values();
	private MaxRects maxRects = new MaxRects(this);
	Settings settings;
	private Sort sort = new Sort();

	public MaxRectsPacker (Settings settings) {
		this.settings = settings;
		if (settings.minWidth > settings.maxWidth) throw new RuntimeException("Page min width cannot be higher than max width.");
		if (settings.minHeight > settings.maxHeight)
			throw new RuntimeException("Page min height cannot be higher than max height.");
	}

	public Array<Page> pack (Array<Rect> inputRects) {
		for (int i = 0, nn = inputRects.size; i < nn; i++) {
			Rect rect = inputRects.get(i);
			rect.width += settings.paddingX;
			rect.height += settings.paddingY;
		}

		if (settings.fast) {
			if (settings.rotation) {
				// Sort by longest side if rotation is enabled.
				sort.sort(inputRects, new Comparator<Rect>() {
					public int compare (Rect o1, Rect o2) {
						int n1 = o1.width > o1.height ? o1.width : o1.height;
						int n2 = o2.width > o2.height ? o2.width : o2.height;
						return n2 - n1;
					}
				});
			} else {
				// Sort only by width (largest to smallest) if rotation is disabled.
				sort.sort(inputRects, new Comparator<Rect>() {
					public int compare (Rect o1, Rect o2) {
						return o2.width - o1.width;
					}
				});
			}
		}

		Array<Page> pages = new Array();
		while (inputRects.size > 0) {
			Page result = packPage(inputRects);
			pages.add(result);
			inputRects = result.remainingRects;
		}
		return pages;
	}

	private Page packPage (Array<Rect> inputRects) {
		int edgePaddingX = 0, edgePaddingY = 0;
		if (!settings.duplicatePadding) { // if duplicatePadding, edges get only half padding.
			edgePaddingX = settings.paddingX;
			edgePaddingY = settings.paddingY;
		}
		// Find min size.
		int minWidth = Integer.MAX_VALUE;
		int minHeight = Integer.MAX_VALUE;
		for (int i = 0, nn = inputRects.size; i < nn; i++) {
			Rect rect = inputRects.get(i);
			minWidth = Math.min(minWidth, rect.width);
			minHeight = Math.min(minHeight, rect.height);
			if (settings.rotation) {
				if ((rect.width > settings.maxWidth || rect.height > settings.maxHeight)
					&& (rect.width > settings.maxHeight || rect.height > settings.maxWidth)) {
					throw new RuntimeException("Image does not fit with max page size " + settings.maxWidth + "x" + settings.maxHeight
						+ " and padding " + settings.paddingX + "," + settings.paddingY + ": " + rect);
				}
			} else {
				if (rect.width > settings.maxWidth) {
					throw new RuntimeException("Image does not fit with max page width " + settings.maxWidth + " and paddingX "
						+ settings.paddingX + ": " + rect);
				}
				if (rect.height > settings.maxHeight && (!settings.rotation || rect.width > settings.maxHeight)) {
					throw new RuntimeException("Image does not fit in max page height " + settings.maxHeight + " and paddingY "
						+ settings.paddingY + ": " + rect);
				}
			}
		}
		minWidth = Math.max(minWidth, settings.minWidth);
		minHeight = Math.max(minHeight, settings.minHeight);

		if (!settings.silent) System.out.print("Packing");

		// Find the minimal page size that fits all rects.
		Page bestResult = null;
		if (settings.square) {
			int minSize = Math.max(minWidth, minHeight);
			int maxSize = Math.min(settings.maxWidth, settings.maxHeight);
			BinarySearch sizeSearch = new BinarySearch(minSize, maxSize, settings.fast ? 25 : 15, settings.pot);
			int size = sizeSearch.reset(), i = 0;
			while (size != -1) {
				Page result = packAtSize(true, size - edgePaddingX, size - edgePaddingY, inputRects);
				if (!settings.silent) {
					if (++i % 70 == 0) L.d();
					System.out.print(".");
				}
				bestResult = getBest(bestResult, result);
				size = sizeSearch.next(result == null);
			}
			if (!settings.silent) L.d();
			// Rects don't fit on one page. Fill a whole page and return.
			if (bestResult == null) bestResult = packAtSize(false, maxSize - edgePaddingX, maxSize - edgePaddingY, inputRects);
			sort.sort(bestResult.outputRects, rectComparator);
			bestResult.width = Math.max(bestResult.width, bestResult.height);
			bestResult.height = Math.max(bestResult.width, bestResult.height);
			return bestResult;
		} else {
			BinarySearch widthSearch = new BinarySearch(minWidth, settings.maxWidth, settings.fast ? 25 : 15, settings.pot);
			BinarySearch heightSearch = new BinarySearch(minHeight, settings.maxHeight, settings.fast ? 25 : 15, settings.pot);
			int width = widthSearch.reset(), i = 0;
			int height = settings.square ? width : heightSearch.reset();
			while (true) {
				Page bestWidthResult = null;
				while (width != -1) {
					Page result = packAtSize(true, width - edgePaddingX, height - edgePaddingY, inputRects);
					if (!settings.silent) {
						if (++i % 70 == 0) L.d();
						System.out.print(".");
					}
					bestWidthResult = getBest(bestWidthResult, result);
					width = widthSearch.next(result == null);
					if (settings.square) height = width;
				}
				bestResult = getBest(bestResult, bestWidthResult);
				if (settings.square) break;
				height = heightSearch.next(bestWidthResult == null);
				if (height == -1) break;
				width = widthSearch.reset();
			}
			if (!settings.silent) L.d();
			// Rects don't fit on one page. Fill a whole page and return.
			if (bestResult == null)
				bestResult = packAtSize(false, settings.maxWidth - edgePaddingX, settings.maxHeight - edgePaddingY, inputRects);
			sort.sort(bestResult.outputRects, rectComparator);
			return bestResult;
		}
	}

	/** @param fully If true, the only results that pack all rects will be considered. If false, all results are considered, not all
	 *           rects may be packed. */
	private Page packAtSize (boolean fully, int width, int height, Array<Rect> inputRects) {
		Page bestResult = null;
		for (int i = 0, n = methods.length; i < n; i++) {
			maxRects.init(width, height);
			Page result;
			if (!settings.fast) {
				result = maxRects.pack(inputRects, methods[i]);
			} else {
				Array<Rect> remaining = new Array();
				for (int ii = 0, nn = inputRects.size; ii < nn; ii++) {
					Rect rect = inputRects.get(ii);
					if (maxRects.insert(rect, methods[i]) == null) {
						while (ii < nn)
							remaining.add(inputRects.get(ii++));
					}
				}
				result = maxRects.getResult();
				result.remainingRects = remaining;
			}
			if (fully && result.remainingRects.size > 0) continue;
			if (result.outputRects.size == 0) continue;
			bestResult = getBest(bestResult, result);
		}
		return bestResult;
	}

	private Page getBest (Page result1, Page result2) {
		if (result1 == null) return result2;
		if (result2 == null) return result1;
		return result1.occupancy > result2.occupancy ? result1 : result2;
	}

	static public enum FreeRectChoiceHeuristic {
		// BSSF: Positions the rectangle against the short side of a free rectangle into which it fits the best.
		BestShortSideFit,
		// BLSF: Positions the rectangle against the long side of a free rectangle into which it fits the best.
		BestLongSideFit,
		// BAF: Positions the rectangle into the smallest free rect into which it fits.
		BestAreaFit,
		// BL: Does the Tetris placement.
		BottomLeftRule,
		// CP: Choosest the placement where the rectangle touches other rects as much as possible.
		ContactPointRule
	}
}
