package com.jfixby.tools.gdx.texturepacker;

import com.jfixby.cmns.api.file.File;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackingSpecs;

public class RedTexturePackingSpecs implements TexturePackingSpecs {

	private File inputRasterFolder;
	private File outputAtlasFolder;
	private String atlasFileName = "";
	private boolean debug_mode;

	public String getAtlasFileName() {
		return atlasFileName;
	}

	public void setAtlasFileName(String atlasFileName) {
		this.atlasFileName = atlasFileName;
	}

	@Override
	public void setOutputAtlasFileName(String file_name) {
		this.atlasFileName = file_name;
	}

	@Override
	public void setDebugMode(boolean debug_mode) {
		this.debug_mode = debug_mode;
	}

	@Override
	public boolean getDebugMode() {
		return debug_mode;
	}

	@Override
	public void setInputRasterFolder(File input_raster_folder) {
		this.inputRasterFolder = input_raster_folder;
	}

	@Override
	public void setOutputAtlasFolder(File output_atlas_folder) {
		this.outputAtlasFolder = output_atlas_folder;
	}

	@Override
	public File getInputRasterFolder() {
		return inputRasterFolder;
	}

	@Override
	public File getOutputAtlasFolder() {
		return outputAtlasFolder;
	}

}
