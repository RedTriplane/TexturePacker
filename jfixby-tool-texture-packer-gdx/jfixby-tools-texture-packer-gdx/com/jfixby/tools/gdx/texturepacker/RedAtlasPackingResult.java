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
    private List<AssetID> list = Collections.newList();
    private List<File> textures = Collections.newList();

    @Override
    public File getAtlasOutputFile() {
	return atlas_file;
    }

    @Override
    public Collection<AssetID> listPackedAssets() {
	return list;
    }

    public void setAtlasOutputFile(File atlas_file) {
	this.atlas_file = atlas_file;
    }

    public void addPackedAssetID(AssetID newAssetID) {
	list.add(newAssetID);
    }

    public static final String Atlas = "libGDX.Atlas.Gwt";

    @Override
    public void print() {
	L.d("---" + Atlas + "[" + atlas_file.getName() + "]-------------------------------------");
	textures.print("output pages");
	list.print("packed assets");
    }

    @Override
    public Collection<File> listPages() {
	return textures;
    }

    public void addPage(File pageFile) {
	textures.add(pageFile);
    }

}
