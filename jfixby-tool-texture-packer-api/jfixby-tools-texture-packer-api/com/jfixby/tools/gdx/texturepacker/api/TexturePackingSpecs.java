
package com.jfixby.tools.gdx.texturepacker.api;

import com.jfixby.cmns.api.file.File;

public interface TexturePackingSpecs {

	void setInputRasterFolder (File input_raster_folder);

	void setOutputAtlasFolder (File output_atlas_folder);

	void setOutputAtlasFileName (String file_name);

	File getInputRasterFolder ();

	File getOutputAtlasFolder ();

	String getAtlasFileName ();

	void setDebugMode (boolean b);

	boolean getDebugMode ();

	void setMaxPageSize (int max_page_size);

	int getMaxPageSize ();

	void setPadding (int padding);

	int getPadding ();

	void setMinPageSize (int min_page_size);

	int getMinPageSize ();

}
