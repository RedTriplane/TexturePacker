package com.badlogic.gdx.tools.texturepacker;

import java.awt.image.BufferedImage;
import java.util.Comparator;

import com.badlogic.gdx.tools.FileWrapper;
import com.jfixby.cmns.api.filesystem.File;

public class Pack {

	static void copy(BufferedImage src, int x, int y, int w, int h,
			BufferedImage dst, int dx, int dy, boolean rotated) {
		if (rotated) {
			for (int i = 0; i < w; i++)
				for (int j = 0; j < h; j++)
					Pack.plot(dst, dx + j, dy + w - i - 1,
							src.getRGB(x + i, y + j));
		} else {
			for (int i = 0; i < w; i++)
				for (int j = 0; j < h; j++)
					Pack.plot(dst, dx + i, dy + j, src.getRGB(x + i, y + j));
		}
	}

	/**
	 * Packs using defaults settings.
	 * 
	 * @see Pack#process(Settings, String, String, String)
	 */
	static public void process(File png_input_dir,
			File atlas_output_dir, String packFileName) {
		process(new Settings(), png_input_dir, atlas_output_dir, packFileName);
	}

	/**
	 * @param png_input_dir
	 *            Directory containing individual images to be packed.
	 * @param atlas_output_dir
	 *            Directory where the pack file and page images will be written.
	 * @param packFileName
	 *            The name of the pack file. Also used to name the page images.
	 */
	static public void process(Settings settings, com.jfixby.cmns.api.filesystem.File png_input_dir, com.jfixby.cmns.api.filesystem.File atlas_output_dir,
			String packFileName) {
		try {
			TexturePackerFileProcessor processor = new TexturePackerFileProcessor(
					settings, packFileName);
			// Sort input files by name to avoid platform-dependent atlas output
			// changes.
			processor.setComparator(new Comparator<File>() {
				public int compare(File file1, File file2) {
					return file1.getName().compareTo(file2.getName());
				}
			});
			processor.process(png_input_dir, atlas_output_dir);
		} catch (Exception ex) {
			throw new RuntimeException("Error packing images.", ex);
		}
	}

	/**
	 * @return true if the output file does not yet exist or its last
	 *         modification date is before the last modification date of the
	 *         input file
	 */
	static public boolean isModified(File input, File output,
			String packFileName, Settings settings) {
		String packFullFileName = output.toJavaFile().getAbsolutePath();

		if (!packFullFileName.endsWith("/")) {
			packFullFileName += "/";
		}

		// Check against the only file we know for sure will exist and will be
		// changed if any asset changes:
		// the atlas file
		packFullFileName += packFileName;
		packFullFileName += settings.atlasExtension;
		File outputFile = FileWrapper.file(packFullFileName);

		if (!outputFile.exists()) {
			return true;
		}

		File inputFile = FileWrapper.file(input);
		if (!inputFile.exists()) {
			throw new IllegalArgumentException("Input file does not exist: "
					+ inputFile.toJavaFile().getAbsolutePath());
		}

		return inputFile.lastModified() > outputFile.lastModified();
	}

	static public void processIfModified(File input, File output,
			String packFileName) {
		// Default settings (Needed to access the default atlas extension
		// string)
		Settings settings = new Settings();

		if (isModified(input, output, packFileName, settings)) {
			process(settings, input, output, packFileName);
		}
	}

	static public void processIfModified(Settings settings, File input,
			File output, String packFileName) {
		if (isModified(input, output, packFileName, settings)) {
			process(settings, input, output, packFileName);
		}
	}

	public static void plot(BufferedImage dst, int x, int y, int argb) {
		if (0 <= x && x < dst.getWidth() && 0 <= y && y < dst.getHeight())
			dst.setRGB(x, y, argb);
	}

}
