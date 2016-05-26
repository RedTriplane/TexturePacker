
package com.jfixby.tools.gdx.texturepacker;

import com.jfixby.cmns.api.assets.AssetID;
import com.jfixby.cmns.api.collections.Collection;
import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.log.L;
import com.jfixby.tools.gdx.texturepacker.api.AtlasPackingResult;

public class RedAtlasPackingResult implements AtlasPackingResult {

	private File atlas_file;
	private final List<AssetID> list = Collections.newList();
	private final List<File> textures = Collections.newList();

	@Override
	public File getAtlasOutputFile () {
		return this.atlas_file;
	}

	@Override
	public Collection<AssetID> listPackedAssets () {
		return this.list;
	}

	public void setAtlasOutputFile (final File atlas_file) {
		this.atlas_file = atlas_file;
	}

	public void addPackedAssetID (final AssetID newAssetID) {
		this.list.add(newAssetID);
	}

	public static final String Atlas = "libGDX.Atlas";

	@Override
	public void print () {
		L.d("---" + Atlas + "[" + this.atlas_file.getName() + "]-------------------------------------");
		this.textures.print("output pages");
		this.list.print("packed assets");
	}

	@Override
	public Collection<File> listPages () {
		return this.textures;
	}

	public void addPage (final File pageFile) {
		this.textures.add(pageFile);
	}

}
