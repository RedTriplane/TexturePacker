package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.tools.texturepacker.MaxRectsPacker;
import com.badlogic.gdx.tools.texturepacker.MaxRectsPacker.FreeRectChoiceHeuristic;
import com.badlogic.gdx.utils.Array;

/** Maximal rectangles bin packing algorithm. Adapted from this C++ public domain source:
 * http://clb.demon.fi/projects/even-more-rectangle-bin-packing
 * @author Jukka Jyl�nki
 * @author Nathan Sweet */
class MaxRects {
	/**
	 * 
	 */
	private final MaxRectsPacker maxRectsPacker;

	/**
	 * @param maxRectsPacker
	 */
	MaxRects(MaxRectsPacker maxRectsPacker) {
		this.maxRectsPacker = maxRectsPacker;
	}

	private int binWidth;
	private int binHeight;
	private final Array<Rect> usedRectangles = new Array();
	private final Array<Rect> freeRectangles = new Array();

	public void init (int width, int height) {
		binWidth = width;
		binHeight = height;

		usedRectangles.clear();
		freeRectangles.clear();
		Rect n = new Rect();
		n.x = 0;
		n.y = 0;
		n.width = width;
		n.height = height;
		freeRectangles.add(n);
	}

	/** Packs a single image. Order is defined externally. */
	public Rect insert (Rect rect, FreeRectChoiceHeuristic method) {
		Rect newNode = scoreRect(rect, method);
		if (newNode.height == 0) return null;

		int numRectanglesToProcess = freeRectangles.size;
		for (int i = 0; i < numRectanglesToProcess; ++i) {
			if (splitFreeNode(freeRectangles.get(i), newNode)) {
				freeRectangles.removeIndex(i);
				--i;
				--numRectanglesToProcess;
			}
		}

		pruneFreeList();

		Rect bestNode = new Rect();
		bestNode.set(rect);
		bestNode.score1 = newNode.score1;
		bestNode.score2 = newNode.score2;
		bestNode.x = newNode.x;
		bestNode.y = newNode.y;
		bestNode.width = newNode.width;
		bestNode.height = newNode.height;
		bestNode.rotated = newNode.rotated;

		usedRectangles.add(bestNode);
		return bestNode;
	}

	/** For each rectangle, packs each one then chooses the best and packs that. Slow! */
	public Page pack (Array<Rect> rects, FreeRectChoiceHeuristic method) {
		rects = new Array(rects);
		while (rects.size > 0) {
			int bestRectIndex = -1;
			Rect bestNode = new Rect();
			bestNode.score1 = Integer.MAX_VALUE;
			bestNode.score2 = Integer.MAX_VALUE;

			// Find the next rectangle that packs best.
			for (int i = 0; i < rects.size; i++) {
				Rect newNode = scoreRect(rects.get(i), method);
				if (newNode.score1 < bestNode.score1 || (newNode.score1 == bestNode.score1 && newNode.score2 < bestNode.score2)) {
					bestNode.set(rects.get(i));
					bestNode.score1 = newNode.score1;
					bestNode.score2 = newNode.score2;
					bestNode.x = newNode.x;
					bestNode.y = newNode.y;
					bestNode.width = newNode.width;
					bestNode.height = newNode.height;
					bestNode.rotated = newNode.rotated;
					bestRectIndex = i;
				}
			}

			if (bestRectIndex == -1) break;

			placeRect(bestNode);
			rects.removeIndex(bestRectIndex);
		}

		Page result = getResult();
		result.remainingRects = rects;
		return result;
	}

	public Page getResult () {
		int w = 0, h = 0;
		for (int i = 0; i < usedRectangles.size; i++) {
			Rect rect = usedRectangles.get(i);
			w = Math.max(w, rect.x + rect.width);
			h = Math.max(h, rect.y + rect.height);
		}
		Page result = new Page();
		result.outputRects = new Array(usedRectangles);
		result.occupancy = getOccupancy();
		result.width = w;
		result.height = h;
		return result;
	}

	private void placeRect (Rect node) {
		int numRectanglesToProcess = freeRectangles.size;
		for (int i = 0; i < numRectanglesToProcess; i++) {
			if (splitFreeNode(freeRectangles.get(i), node)) {
				freeRectangles.removeIndex(i);
				--i;
				--numRectanglesToProcess;
			}
		}

		pruneFreeList();

		usedRectangles.add(node);
	}

	private Rect scoreRect (Rect rect, FreeRectChoiceHeuristic method) {
		int width = rect.width;
		int height = rect.height;
		int rotatedWidth = height - this.maxRectsPacker.settings.paddingY + this.maxRectsPacker.settings.paddingX;
		int rotatedHeight = width - this.maxRectsPacker.settings.paddingX + this.maxRectsPacker.settings.paddingY;
		boolean rotate = rect.canRotate && this.maxRectsPacker.settings.rotation;

		Rect newNode = null;
		switch (method) {
		case BestShortSideFit:
			newNode = findPositionForNewNodeBestShortSideFit(width, height, rotatedWidth, rotatedHeight, rotate);
			break;
		case BottomLeftRule:
			newNode = findPositionForNewNodeBottomLeft(width, height, rotatedWidth, rotatedHeight, rotate);
			break;
		case ContactPointRule:
			newNode = findPositionForNewNodeContactPoint(width, height, rotatedWidth, rotatedHeight, rotate);
			newNode.score1 = -newNode.score1; // Reverse since we are minimizing, but for contact point score bigger is better.
			break;
		case BestLongSideFit:
			newNode = findPositionForNewNodeBestLongSideFit(width, height, rotatedWidth, rotatedHeight, rotate);
			break;
		case BestAreaFit:
			newNode = findPositionForNewNodeBestAreaFit(width, height, rotatedWidth, rotatedHeight, rotate);
			break;
		}

		// Cannot fit the current rectangle.
		if (newNode.height == 0) {
			newNode.score1 = Integer.MAX_VALUE;
			newNode.score2 = Integer.MAX_VALUE;
		}

		return newNode;
	}

	// / Computes the ratio of used surface area.
	private float getOccupancy () {
		int usedSurfaceArea = 0;
		for (int i = 0; i < usedRectangles.size; i++)
			usedSurfaceArea += usedRectangles.get(i).width * usedRectangles.get(i).height;
		return (float)usedSurfaceArea / (binWidth * binHeight);
	}

	private Rect findPositionForNewNodeBottomLeft (int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
		Rect bestNode = new Rect();

		bestNode.score1 = Integer.MAX_VALUE; // best y, score2 is best x

		for (int i = 0; i < freeRectangles.size; i++) {
			// Try to place the rectangle in upright (non-rotated) orientation.
			if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
				int topSideY = freeRectangles.get(i).y + height;
				if (topSideY < bestNode.score1 || (topSideY == bestNode.score1 && freeRectangles.get(i).x < bestNode.score2)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = width;
					bestNode.height = height;
					bestNode.score1 = topSideY;
					bestNode.score2 = freeRectangles.get(i).x;
					bestNode.rotated = false;
				}
			}
			if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
				int topSideY = freeRectangles.get(i).y + rotatedHeight;
				if (topSideY < bestNode.score1 || (topSideY == bestNode.score1 && freeRectangles.get(i).x < bestNode.score2)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = rotatedWidth;
					bestNode.height = rotatedHeight;
					bestNode.score1 = topSideY;
					bestNode.score2 = freeRectangles.get(i).x;
					bestNode.rotated = true;
				}
			}
		}
		return bestNode;
	}

	private Rect findPositionForNewNodeBestShortSideFit (int width, int height, int rotatedWidth, int rotatedHeight,
		boolean rotate) {
		Rect bestNode = new Rect();
		bestNode.score1 = Integer.MAX_VALUE;

		for (int i = 0; i < freeRectangles.size; i++) {
			// Try to place the rectangle in upright (non-rotated) orientation.
			if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
				int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
				int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
				int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
				int longSideFit = Math.max(leftoverHoriz, leftoverVert);

				if (shortSideFit < bestNode.score1 || (shortSideFit == bestNode.score1 && longSideFit < bestNode.score2)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = width;
					bestNode.height = height;
					bestNode.score1 = shortSideFit;
					bestNode.score2 = longSideFit;
					bestNode.rotated = false;
				}
			}

			if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
				int flippedLeftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
				int flippedLeftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
				int flippedShortSideFit = Math.min(flippedLeftoverHoriz, flippedLeftoverVert);
				int flippedLongSideFit = Math.max(flippedLeftoverHoriz, flippedLeftoverVert);

				if (flippedShortSideFit < bestNode.score1
					|| (flippedShortSideFit == bestNode.score1 && flippedLongSideFit < bestNode.score2)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = rotatedWidth;
					bestNode.height = rotatedHeight;
					bestNode.score1 = flippedShortSideFit;
					bestNode.score2 = flippedLongSideFit;
					bestNode.rotated = true;
				}
			}
		}

		return bestNode;
	}

	private Rect findPositionForNewNodeBestLongSideFit (int width, int height, int rotatedWidth, int rotatedHeight,
		boolean rotate) {
		Rect bestNode = new Rect();

		bestNode.score2 = Integer.MAX_VALUE;

		for (int i = 0; i < freeRectangles.size; i++) {
			// Try to place the rectangle in upright (non-rotated) orientation.
			if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
				int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
				int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
				int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
				int longSideFit = Math.max(leftoverHoriz, leftoverVert);

				if (longSideFit < bestNode.score2 || (longSideFit == bestNode.score2 && shortSideFit < bestNode.score1)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = width;
					bestNode.height = height;
					bestNode.score1 = shortSideFit;
					bestNode.score2 = longSideFit;
					bestNode.rotated = false;
				}
			}

			if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
				int leftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
				int leftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
				int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
				int longSideFit = Math.max(leftoverHoriz, leftoverVert);

				if (longSideFit < bestNode.score2 || (longSideFit == bestNode.score2 && shortSideFit < bestNode.score1)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = rotatedWidth;
					bestNode.height = rotatedHeight;
					bestNode.score1 = shortSideFit;
					bestNode.score2 = longSideFit;
					bestNode.rotated = true;
				}
			}
		}
		return bestNode;
	}

	private Rect findPositionForNewNodeBestAreaFit (int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
		Rect bestNode = new Rect();

		bestNode.score1 = Integer.MAX_VALUE; // best area fit, score2 is best short side fit

		for (int i = 0; i < freeRectangles.size; i++) {
			int areaFit = freeRectangles.get(i).width * freeRectangles.get(i).height - width * height;

			// Try to place the rectangle in upright (non-rotated) orientation.
			if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
				int leftoverHoriz = Math.abs(freeRectangles.get(i).width - width);
				int leftoverVert = Math.abs(freeRectangles.get(i).height - height);
				int shortSideFit = Math.min(leftoverHoriz, leftoverVert);

				if (areaFit < bestNode.score1 || (areaFit == bestNode.score1 && shortSideFit < bestNode.score2)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = width;
					bestNode.height = height;
					bestNode.score2 = shortSideFit;
					bestNode.score1 = areaFit;
					bestNode.rotated = false;
				}
			}

			if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
				int leftoverHoriz = Math.abs(freeRectangles.get(i).width - rotatedWidth);
				int leftoverVert = Math.abs(freeRectangles.get(i).height - rotatedHeight);
				int shortSideFit = Math.min(leftoverHoriz, leftoverVert);

				if (areaFit < bestNode.score1 || (areaFit == bestNode.score1 && shortSideFit < bestNode.score2)) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = rotatedWidth;
					bestNode.height = rotatedHeight;
					bestNode.score2 = shortSideFit;
					bestNode.score1 = areaFit;
					bestNode.rotated = true;
				}
			}
		}
		return bestNode;
	}

	// / Returns 0 if the two intervals i1 and i2 are disjoint, or the length of their overlap otherwise.
	private int commonIntervalLength (int i1start, int i1end, int i2start, int i2end) {
		if (i1end < i2start || i2end < i1start) return 0;
		return Math.min(i1end, i2end) - Math.max(i1start, i2start);
	}

	private int contactPointScoreNode (int x, int y, int width, int height) {
		int score = 0;

		if (x == 0 || x + width == binWidth) score += height;
		if (y == 0 || y + height == binHeight) score += width;

		for (int i = 0; i < usedRectangles.size; i++) {
			if (usedRectangles.get(i).x == x + width || usedRectangles.get(i).x + usedRectangles.get(i).width == x)
				score += commonIntervalLength(usedRectangles.get(i).y, usedRectangles.get(i).y + usedRectangles.get(i).height, y,
					y + height);
			if (usedRectangles.get(i).y == y + height || usedRectangles.get(i).y + usedRectangles.get(i).height == y)
				score += commonIntervalLength(usedRectangles.get(i).x, usedRectangles.get(i).x + usedRectangles.get(i).width, x, x
					+ width);
		}
		return score;
	}

	private Rect findPositionForNewNodeContactPoint (int width, int height, int rotatedWidth, int rotatedHeight, boolean rotate) {
		Rect bestNode = new Rect();

		bestNode.score1 = -1; // best contact score

		for (int i = 0; i < freeRectangles.size; i++) {
			// Try to place the rectangle in upright (non-rotated) orientation.
			if (freeRectangles.get(i).width >= width && freeRectangles.get(i).height >= height) {
				int score = contactPointScoreNode(freeRectangles.get(i).x, freeRectangles.get(i).y, width, height);
				if (score > bestNode.score1) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = width;
					bestNode.height = height;
					bestNode.score1 = score;
					bestNode.rotated = false;
				}
			}
			if (rotate && freeRectangles.get(i).width >= rotatedWidth && freeRectangles.get(i).height >= rotatedHeight) {
				// This was width,height -- bug fixed?
				int score = contactPointScoreNode(freeRectangles.get(i).x, freeRectangles.get(i).y, rotatedWidth, rotatedHeight);
				if (score > bestNode.score1) {
					bestNode.x = freeRectangles.get(i).x;
					bestNode.y = freeRectangles.get(i).y;
					bestNode.width = rotatedWidth;
					bestNode.height = rotatedHeight;
					bestNode.score1 = score;
					bestNode.rotated = true;
				}
			}
		}
		return bestNode;
	}

	private boolean splitFreeNode (Rect freeNode, Rect usedNode) {
		// Test with SAT if the rectangles even intersect.
		if (usedNode.x >= freeNode.x + freeNode.width || usedNode.x + usedNode.width <= freeNode.x
			|| usedNode.y >= freeNode.y + freeNode.height || usedNode.y + usedNode.height <= freeNode.y) return false;

		if (usedNode.x < freeNode.x + freeNode.width && usedNode.x + usedNode.width > freeNode.x) {
			// New node at the top side of the used node.
			if (usedNode.y > freeNode.y && usedNode.y < freeNode.y + freeNode.height) {
				Rect newNode = new Rect(freeNode);
				newNode.height = usedNode.y - newNode.y;
				freeRectangles.add(newNode);
			}

			// New node at the bottom side of the used node.
			if (usedNode.y + usedNode.height < freeNode.y + freeNode.height) {
				Rect newNode = new Rect(freeNode);
				newNode.y = usedNode.y + usedNode.height;
				newNode.height = freeNode.y + freeNode.height - (usedNode.y + usedNode.height);
				freeRectangles.add(newNode);
			}
		}

		if (usedNode.y < freeNode.y + freeNode.height && usedNode.y + usedNode.height > freeNode.y) {
			// New node at the left side of the used node.
			if (usedNode.x > freeNode.x && usedNode.x < freeNode.x + freeNode.width) {
				Rect newNode = new Rect(freeNode);
				newNode.width = usedNode.x - newNode.x;
				freeRectangles.add(newNode);
			}

			// New node at the right side of the used node.
			if (usedNode.x + usedNode.width < freeNode.x + freeNode.width) {
				Rect newNode = new Rect(freeNode);
				newNode.x = usedNode.x + usedNode.width;
				newNode.width = freeNode.x + freeNode.width - (usedNode.x + usedNode.width);
				freeRectangles.add(newNode);
			}
		}

		return true;
	}

	private void pruneFreeList () {
		/*
		 * /// Would be nice to do something like this, to avoid a Theta(n^2) loop through each pair. /// But unfortunately it
		 * doesn't quite cut it, since we also want to detect containment. /// Perhaps there's another way to do this faster than
		 * Theta(n^2).
		 * 
		 * if (freeRectangles.size > 0) clb::sort::QuickSort(&freeRectangles[0], freeRectangles.size, NodeSortCmp);
		 * 
		 * for(int i = 0; i < freeRectangles.size-1; i++) if (freeRectangles[i].x == freeRectangles[i+1].x && freeRectangles[i].y
		 * == freeRectangles[i+1].y && freeRectangles[i].width == freeRectangles[i+1].width && freeRectangles[i].height ==
		 * freeRectangles[i+1].height) { freeRectangles.erase(freeRectangles.begin() + i); --i; }
		 */

		// / Go through each pair and remove any rectangle that is redundant.
		for (int i = 0; i < freeRectangles.size; i++)
			for (int j = i + 1; j < freeRectangles.size; ++j) {
				if (isContainedIn(freeRectangles.get(i), freeRectangles.get(j))) {
					freeRectangles.removeIndex(i);
					--i;
					break;
				}
				if (isContainedIn(freeRectangles.get(j), freeRectangles.get(i))) {
					freeRectangles.removeIndex(j);
					--j;
				}
			}
	}

	private boolean isContainedIn (Rect a, Rect b) {
		return a.x >= b.x && a.y >= b.y && a.x + a.width <= b.x + b.width && a.y + a.height <= b.y + b.height;
	}
}