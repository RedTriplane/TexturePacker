
package com.badlogic.gdx.tools.texturepacker;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;

import com.badlogic.gdx.tools.FileWrapper;
import com.badlogic.gdx.tools.texturepacker.TexturePackerFileProcessor;
import com.jfixby.scarabei.api.file.File;

public class Pack {

	static void copy (final BufferedImage src, final int x, final int y, final int w, final int h, final BufferedImage dst,
		final int dx, final int dy, final boolean rotated) {
		if (rotated) {
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Pack.plot(dst, dx + j, dy + w - i - 1, src.getRGB(x + i, y + j));
				}
			}
		} else {
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					Pack.plot(dst, dx + i, dy + j, src.getRGB(x + i, y + j));
				}
			}
		}
	}

	/** Packs using defaults settings.
	 *
	 * @see Pack#process(Settings, String, String, String) */
	static public void process (final File png_input_dir, final File atlas_output_dir, final String packFileName) {
		process(new Settings(), png_input_dir, atlas_output_dir, packFileName);
	}

	/** @param png_input_dir Directory containing individual images to be packed.
	 * @param atlas_output_dir Directory where the pack file and page images will be written.
	 * @param packFileName The name of the pack file. Also used to name the page images. */
	static public void process (final Settings settings, final com.jfixby.scarabei.api.file.File png_input_dir,
		final com.jfixby.scarabei.api.file.File atlas_output_dir, final String packFileName) {
		try {
			final TexturePackerFileProcessor processor = new TexturePackerFileProcessor(settings, packFileName);
			// Sort input files by name to avoid platform-dependent atlas output
			// changes.
			processor.setComparator(new Comparator<File>() {
				@Override
				public int compare (final File file1, final File file2) {
					return file1.getName().compareTo(file2.getName());
				}
			});
			processor.process(png_input_dir, atlas_output_dir);
		} catch (final Exception ex) {
			throw new RuntimeException("Error packing images.", ex);
		}
	}

	/** @return true if the output file does not yet exist or its last modification date is before the last modification date of
	 *         the input file
	 * @throws IOException */
	static public boolean isModified (final File input, final File output, final String packFileName, final Settings settings)
		throws IOException {
		String packFullFileName = output.toJavaFile().getAbsolutePath();

		if (!packFullFileName.endsWith("/")) {
			packFullFileName += "/";
		}

		// Check against the only file we know for sure will exist and will be
		// changed if any asset changes:
		// the atlas file
		packFullFileName += packFileName;
		packFullFileName += settings.atlasExtension;
		final File outputFile = FileWrapper.file(packFullFileName);

		if (!outputFile.exists()) {
			return true;
		}

		final File inputFile = FileWrapper.file(input);
		if (!inputFile.exists()) {
			throw new IllegalArgumentException("Input file does not exist: " + inputFile.toJavaFile().getAbsolutePath());
		}

		return inputFile.lastModified() > outputFile.lastModified();
	}

	static public void processIfModified (final File input, final File output, final String packFileName) throws IOException {
		// Default settings (Needed to access the default atlas extension
		// string)
		final Settings settings = new Settings();

		if (isModified(input, output, packFileName, settings)) {
			process(settings, input, output, packFileName);
		}
	}

	static public void processIfModified (final Settings settings, final File input, final File output, final String packFileName)
		throws IOException {
		if (isModified(input, output, packFileName, settings)) {
			process(settings, input, output, packFileName);
		}
	}

	public static void plot (final BufferedImage dst, final int x, final int y, final int argb) {
		if (0 <= x && x < dst.getWidth() && 0 <= y && y < dst.getHeight()) {
			dst.setRGB(x, y, argb);
		}
	}

}
