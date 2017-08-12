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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.FileWrapper;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jfixby.scarabei.api.desktop.ImageAWT;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.log.L;

/** @author Nathan Sweet */
public class TexturePacker {
	private final Settings settings;
	private final Packer packer;
	private final ImageProcessor imageProcessor;
	private final Array<InputImage> inputImages = new Array();
	private final File rootDir;

	/** @param rootDir Used to strip the root directory prefix from image file names, can be null. */
	public TexturePacker (final File rootDir, final Settings settings) {
		this.rootDir = rootDir;
		this.settings = settings;

		if (settings.pot) {
			if (settings.maxWidth != MathUtils.nextPowerOfTwo(settings.maxWidth)) {
				throw new RuntimeException("If pot is true, maxWidth must be a power of two: " + settings.maxWidth);
			}
			if (settings.maxHeight != MathUtils.nextPowerOfTwo(settings.maxHeight)) {
				throw new RuntimeException("If pot is true, maxHeight must be a power of two: " + settings.maxHeight);
			}
		}

		if (settings.grid) {
			this.packer = new GridPacker(settings);
		} else {
			this.packer = new MaxRectsPacker(settings);
		}
		this.imageProcessor = new ImageProcessor(rootDir, settings);
	}

	public TexturePacker (final Settings settings) {
		this(null, settings);
	}

	public void addImage (final File file) {
		final InputImage inputImage = new InputImage();
		inputImage.file = file;
		this.inputImages.add(inputImage);
	}

	public void addImage (final BufferedImage image, final String name) {
		final InputImage inputImage = new InputImage();
		inputImage.image = image;
		inputImage.name = name;
		this.inputImages.add(inputImage);
	}

	public void pack (final File outputDir, String packFileName) throws IOException {
		if (packFileName.endsWith(this.settings.atlasExtension)) {
			packFileName = packFileName.substring(0, packFileName.length() - this.settings.atlasExtension.length());
		}
		outputDir.makeFolder();

		for (int i = 0, n = this.settings.scale.length; i < n; i++) {
			this.imageProcessor.setScale(this.settings.scale[i]);
			for (final InputImage inputImage : this.inputImages) {
				if (inputImage.file != null) {
					this.imageProcessor.addImage(inputImage.file);
				} else {
					this.imageProcessor.addImage(inputImage.image, inputImage.name);
				}
			}

			final Array<Page> pages = this.packer.pack(this.imageProcessor.getImages());

			final String scaledPackFileName = this.settings.getScaledPackFileName(packFileName, i);
			this.writeImages(outputDir, scaledPackFileName, pages);
			try {
				this.writePackFile(outputDir, scaledPackFileName, pages);
			} catch (final IOException ex) {
				throw new RuntimeException("Error writing pack file.", ex);
			}
			this.imageProcessor.clear();
		}
	}

	private void writeImages (final File outputDir, final String scaledPackFileName, final Array<Page> pages) throws IOException {
		final File packFileNoExt = FileWrapper.file(outputDir, scaledPackFileName);
		final File packDir = packFileNoExt.parent();
		final String imageName = packFileNoExt.getName();

		int fileIndex = 0;
		for (final Page page : pages) {
			int width = page.width, height = page.height;
			int paddingX = this.settings.paddingX;
			int paddingY = this.settings.paddingY;
			if (this.settings.duplicatePadding) {
				paddingX /= 2;
				paddingY /= 2;
			}
			width -= this.settings.paddingX;
			height -= this.settings.paddingY;
			if (this.settings.edgePadding) {
				page.x = paddingX;
				page.y = paddingY;
				width += paddingX * 2;
				height += paddingY * 2;
			}
			if (this.settings.pot) {
				width = MathUtils.nextPowerOfTwo(width);
				height = MathUtils.nextPowerOfTwo(height);
			}
			width = Math.max(this.settings.minWidth, width);
			height = Math.max(this.settings.minHeight, height);
			page.imageWidth = width;
			page.imageHeight = height;

			File outputFile;
			while (true) {
				outputFile = FileWrapper.file(packDir,
					imageName + (fileIndex++ == 0 ? "" : fileIndex) + "." + this.settings.outputFormat);
				if (!outputFile.exists()) {
					break;
				}
			}
			FileWrapper.newFileHandle(outputFile).parent().mkdirs();
			page.imageName = outputFile.getName();

			BufferedImage canvas = new BufferedImage(width, height, this.getBufferedImageType(this.settings.format));
			Graphics2D g = (Graphics2D)canvas.getGraphics();

			if (!this.settings.silent) {
				L.d("writing " + canvas.getWidth() + "x" + canvas.getHeight());
				L.d("     ", outputFile);
			}

			for (final Rect rect : page.outputRects) {
				final BufferedImage image = rect.getImage(this.imageProcessor);
				final int iw = image.getWidth();
				final int ih = image.getHeight();
				final int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;
				if (this.settings.duplicatePadding) {
					final int amountX = this.settings.paddingX / 2;
					final int amountY = this.settings.paddingY / 2;
					if (rect.rotated) {
						// Copy corner pixels to fill corners of the padding.
						for (int i = 1; i <= amountX; i++) {
							for (int j = 1; j <= amountY; j++) {
								Pack.plot(canvas, rectX - j, rectY + iw - 1 + i, image.getRGB(0, 0));
								Pack.plot(canvas, rectX + ih - 1 + j, rectY + iw - 1 + i, image.getRGB(0, ih - 1));
								Pack.plot(canvas, rectX - j, rectY - i, image.getRGB(iw - 1, 0));
								Pack.plot(canvas, rectX + ih - 1 + j, rectY - i, image.getRGB(iw - 1, ih - 1));
							}
						}
						// Copy edge pixels into padding.
						for (int i = 1; i <= amountY; i++) {
							for (int j = 0; j < iw; j++) {
								Pack.plot(canvas, rectX - i, rectY + iw - 1 - j, image.getRGB(j, 0));
								Pack.plot(canvas, rectX + ih - 1 + i, rectY + iw - 1 - j, image.getRGB(j, ih - 1));
							}
						}
						for (int i = 1; i <= amountX; i++) {
							for (int j = 0; j < ih; j++) {
								Pack.plot(canvas, rectX + j, rectY - i, image.getRGB(iw - 1, j));
								Pack.plot(canvas, rectX + j, rectY + iw - 1 + i, image.getRGB(0, j));
							}
						}
					} else {
						// Copy corner pixels to fill corners of the padding.
						for (int i = 1; i <= amountX; i++) {
							for (int j = 1; j <= amountY; j++) {
								Pack.plot(canvas, rectX - i, rectY - j, image.getRGB(0, 0));
								Pack.plot(canvas, rectX - i, rectY + ih - 1 + j, image.getRGB(0, ih - 1));
								Pack.plot(canvas, rectX + iw - 1 + i, rectY - j, image.getRGB(iw - 1, 0));
								Pack.plot(canvas, rectX + iw - 1 + i, rectY + ih - 1 + j, image.getRGB(iw - 1, ih - 1));
							}
						}
						// Copy edge pixels into padding.
						for (int i = 1; i <= amountY; i++) {
							Pack.copy(image, 0, 0, iw, 1, canvas, rectX, rectY - i, rect.rotated);
							Pack.copy(image, 0, ih - 1, iw, 1, canvas, rectX, rectY + ih - 1 + i, rect.rotated);
						}
						for (int i = 1; i <= amountX; i++) {
							Pack.copy(image, 0, 0, 1, ih, canvas, rectX - i, rectY, rect.rotated);
							Pack.copy(image, iw - 1, 0, 1, ih, canvas, rectX + iw - 1 + i, rectY, rect.rotated);
						}
					}
				}
				Pack.copy(image, 0, 0, iw, ih, canvas, rectX, rectY, rect.rotated);
				if (this.settings.debug) {
					g.setColor(Color.magenta);
					g.drawRect(rectX, rectY, rect.width - this.settings.paddingX - 1, rect.height - this.settings.paddingY - 1);
				}
			}

			if (this.settings.bleed && !this.settings.premultiplyAlpha && !this.settings.outputFormat.equalsIgnoreCase("jpg")) {
				canvas = new ColorBleedEffect().processImage(canvas, 2);
				g = (Graphics2D)canvas.getGraphics();
			}

			if (this.settings.debug) {
				g.setColor(Color.magenta);
				g.drawRect(0, 0, width - 1, height - 1);
			}

			ImageOutputStream ios = null;
			try {
				if (this.settings.outputFormat.equalsIgnoreCase("jpg")) {
					final BufferedImage newImage = new BufferedImage(canvas.getWidth(), canvas.getHeight(),
						BufferedImage.TYPE_3BYTE_BGR);
					newImage.getGraphics().drawImage(canvas, 0, 0, null);
					canvas = newImage;

					final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
					final ImageWriter writer = writers.next();
					final ImageWriteParam param = writer.getDefaultWriteParam();
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(this.settings.jpegQuality);
					ios = ImageIO.createImageOutputStream(outputFile);
					writer.setOutput(ios);
					writer.write(null, new IIOImage(canvas, null, null), param);
				} else {
					if (this.settings.premultiplyAlpha) {
						canvas.getColorModel().coerceData(canvas.getRaster(), true);
					}
// ImageIO.write(canvas, "png", outputFile.toJavaFile());
					ImageAWT.writeToFile(canvas, outputFile, "png");
				}
			} catch (final IOException ex) {
				throw new RuntimeException("Error writing file: " + outputFile, ex);
			} finally {
				if (ios != null) {
					try {
						ios.close();
					} catch (final Exception ignored) {
						ignored.printStackTrace();
					}
				}
			}
		}
	}

	private void writePackFile (final File outputDir, final String scaledPackFileName, final Array<Page> pages)
		throws IOException {
		final File packFile = FileWrapper.file(outputDir, scaledPackFileName + Settings.atlasExtension);
		if (!this.settings.silent) {
			L.d("pack file");
			L.d("     ", packFile);
		}

		final File packDir = packFile.parent();
		packDir.makeFolder();

		if (packFile.exists()) {
			// Make sure there aren't duplicate names.
			final TextureAtlasData textureAtlasData = new TextureAtlasData(FileWrapper.newFileHandle(packFile),
				FileWrapper.newFileHandle(packFile), false);
			for (final Page page : pages) {
				for (final Rect rect : page.outputRects) {
					final String rectName = Rect.getAtlasName(rect.name, this.settings.flattenPaths);
					for (final Region region : textureAtlasData.getRegions()) {
						if (region.name.equals(rectName)) {
							throw new GdxRuntimeException(
								"A region with the name \"" + rectName + "\" has already been packed: " + rect.name);
						}
					}
				}
			}
		}

		final FileWriter writer = new FileWriter(packFile, true);
		for (final Page page : pages) {
			writer.write("\n" + page.imageName + "\n");
			writer.write("size: " + page.imageWidth + "," + page.imageHeight + "\n");
			writer.write("format: " + this.settings.format + "\n");
			writer.write("filter: " + this.settings.filterMin + "," + this.settings.filterMag + "\n");
			writer.write("repeat: " + this.getRepeatValue() + "\n");

			page.outputRects.sort();
			for (final Rect rect : page.outputRects) {
				this.writeRect(writer, page, rect, rect.name);
				final Array<Alias> aliases = new Array(rect.aliases.toArray());
				aliases.sort();
				for (final Alias alias : aliases) {
					final Rect aliasRect = new Rect();
					aliasRect.set(rect);
					alias.apply(aliasRect);
					this.writeRect(writer, page, aliasRect, alias.name);
				}
			}
		}
		writer.close();
	}

	private void writeRect (final FileWriter writer, final Page page, final Rect rect, final String name) throws IOException {
		writer.write(Rect.getAtlasName(name, this.settings.flattenPaths) + "\n");
		writer.write("  rotate: " + rect.rotated + "\n");
		writer.write("  xy: " + (page.x + rect.x) + ", " + (page.y + page.height - rect.height - rect.y) + "\n");

		writer.write("  size: " + rect.regionWidth + ", " + rect.regionHeight + "\n");
		if (rect.splits != null) {
			writer.write("  split: " //
				+ rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
		}
		if (rect.pads != null) {
			if (rect.splits == null) {
				writer.write("  split: 0, 0, 0, 0\n");
			}
			writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
		}
		writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
		writer.write("  offset: " + rect.offsetX + ", " + (rect.originalHeight - rect.regionHeight - rect.offsetY) + "\n");
		writer.write("  index: " + rect.index + "\n");
	}

	private String getRepeatValue () {
		if (this.settings.wrapX == TextureWrap.Repeat && this.settings.wrapY == TextureWrap.Repeat) {
			return "xy";
		}
		if (this.settings.wrapX == TextureWrap.Repeat && this.settings.wrapY == TextureWrap.ClampToEdge) {
			return "x";
		}
		if (this.settings.wrapX == TextureWrap.ClampToEdge && this.settings.wrapY == TextureWrap.Repeat) {
			return "y";
		}
		return "none";
	}

	private int getBufferedImageType (final Format format) {
		switch (this.settings.format) {
		case RGBA8888:
		case RGBA4444:
			return BufferedImage.TYPE_INT_ARGB;
		case RGB565:
		case RGB888:
			return BufferedImage.TYPE_INT_RGB;
		case Alpha:
			return BufferedImage.TYPE_BYTE_GRAY;
		default:
			throw new RuntimeException("Unsupported format: " + this.settings.format);
		}
	}

	static public void main (final String[] args) throws Exception {
		String input = null, output = null, packFileName = "pack.atlas";

		switch (args.length) {
		case 3:
			packFileName = args[2];
		case 2:
			output = args[1];
		case 1:
			input = args[0];
			break;
		default:
			L.d("Usage: inputDir [outputDir] [packFileName]");
			System.exit(0);
		}
		final File inputFile = FileWrapper.file(input);
		if (output == null) {

			output = FileWrapper.file(inputFile.parent(), inputFile.getName() + "-packed").toJavaFile().getAbsolutePath();
		}
		final File outputFile = FileWrapper.file(input);

		Pack.process(inputFile, outputFile, packFileName);
	}
}
