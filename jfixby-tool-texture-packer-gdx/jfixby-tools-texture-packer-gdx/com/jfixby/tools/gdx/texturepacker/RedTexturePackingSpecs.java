
package com.jfixby.tools.gdx.texturepacker;

import com.jfixby.scarabei.api.file.File;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackingSpecs;

public class RedTexturePackingSpecs implements TexturePackingSpecs {

	private File inputRasterFolder;
	private File outputAtlasFolder;
	private String atlasFileName = "";
	private boolean debug_mode;
	private int max_page_size = 1024;
	private int min_page_size = 1024;
	private int padding;

	@Override
	public String getAtlasFileName () {
		return this.atlasFileName;
	}

	public void setAtlasFileName (final String atlasFileName) {
		this.atlasFileName = atlasFileName;
	}

	@Override
	public void setOutputAtlasFileName (final String file_name) {
		this.atlasFileName = file_name;
	}

	@Override
	public void setDebugMode (final boolean debug_mode) {
		this.debug_mode = debug_mode;
	}

	@Override
	public boolean getDebugMode () {
		return this.debug_mode;
	}

	@Override
	public void setInputRasterFolder (final File input_raster_folder) {
		this.inputRasterFolder = input_raster_folder;
	}

	@Override
	public void setOutputAtlasFolder (final File output_atlas_folder) {
		this.outputAtlasFolder = output_atlas_folder;
	}

	@Override
	public File getInputRasterFolder () {
		return this.inputRasterFolder;
	}

	@Override
	public File getOutputAtlasFolder () {
		return this.outputAtlasFolder;
	}

	@Override
	public void setMaxPageSize (final int max_page_size) {
		this.max_page_size = max_page_size;
	}

	@Override
	public int getMaxPageSize () {
		return this.max_page_size;
	}

	@Override
	public void setPadding (final int padding) {
		this.padding = padding;
	}

	@Override
	public int getPadding () {
		return this.padding;
	}

	@Override
	public void setMinPageSize (final int min_page_size) {
		this.min_page_size = min_page_size;
	}

	@Override
	public int getMinPageSize () {
		return this.min_page_size;
	}

}
