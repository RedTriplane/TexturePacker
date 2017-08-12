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

import java.awt.image.BufferedImage;

import com.badlogic.gdx.tools.texturepacker.Mask.MaskIterator;

/** @author Ruben Garat
 * @author Ariel Coppes
 * @author Nathan Sweet */
public class ColorBleedEffect {
	static int TO_PROCESS = 0;
	static int IN_PROCESS = 1;
	static int REALDATA = 2;
	static int[][] offsets = { {-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};

	ARGBColor color = new ARGBColor();

	public BufferedImage processImage (BufferedImage image, int maxIterations) {
		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
		Mask mask = new Mask(rgb);

		int iterations = 0;
		int lastPending = -1;
		while (mask.pendingSize > 0 && mask.pendingSize != lastPending && iterations < maxIterations) {
			lastPending = mask.pendingSize;
			executeIteration(rgb, mask, width, height);
			iterations++;
		}

		processedImage.setRGB(0, 0, width, height, rgb, 0, width);
		return processedImage;
	}

	private void executeIteration (int[] rgb, Mask mask, int width, int height) {
		MaskIterator iterator = mask.new MaskIterator();
		while (iterator.hasNext()) {
			int pixelIndex = iterator.next();
			int x = pixelIndex % width;
			int y = pixelIndex / width;
			int r = 0, g = 0, b = 0;
			int count = 0;

			for (int i = 0, n = offsets.length; i < n; i++) {
				int[] offset = offsets[i];
				int column = x + offset[0];
				int row = y + offset[1];

				if (column < 0 || column >= width || row < 0 || row >= height) continue;

				int currentPixelIndex = getPixelIndex(width, column, row);
				if (mask.getMask(currentPixelIndex) == REALDATA) {
					color.argb = rgb[currentPixelIndex];
					r += color.red();
					g += color.green();
					b += color.blue();
					count++;
				}
			}

			if (count != 0) {
				color.setARGBA(0, r / count, g / count, b / count);
				rgb[pixelIndex] = color.argb;
				iterator.markAsInProgress();
			}
		}

		iterator.reset();
	}

	private int getPixelIndex (int width, int x, int y) {
		return y * width + x;
	}
}
