
package com.jfixby.tools.gdx.texturepacker;

import com.jfixby.scarabei.api.collections.Collection;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.names.ID;
import com.jfixby.tools.gdx.texturepacker.api.AtlasPackingResult;

public class RedAtlasPackingResult implements AtlasPackingResult {

	private File atlas_file;
	private final List<ID> list = Collections.newList();
	private final List<File> textures = Collections.newList();

	@Override
	public File getAtlasOutputFile () {
		return this.atlas_file;
	}

	@Override
	public Collection<ID> listPackedAssets () {
		return this.list;
	}

	public void setAtlasOutputFile (final File atlas_file) {
		this.atlas_file = atlas_file;
	}

	public void addPackedAssetID (final ID newAssetID) {
		this.list.add(newAssetID);
	}

	public static final String Atlas = "libGDX.Atlas";

// @Override
// public void print () {
// L.d("---" + Atlas + "[" + this.atlas_file.getName() + "]-------------------------------------");
// this.textures.print("output pages");
// this.list.print("packed assets");
// }

	@Override
	public Collection<File> listPages () {
		return this.textures;
	}

	public void addPage (final File pageFile) {
		this.textures.add(pageFile);
	}

}
