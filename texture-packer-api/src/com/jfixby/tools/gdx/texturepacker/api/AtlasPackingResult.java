package com.jfixby.tools.gdx.texturepacker.api;

import com.jfixby.scarabei.api.assets.ID;
import com.jfixby.scarabei.api.collections.Collection;
import com.jfixby.scarabei.api.file.File;

public interface AtlasPackingResult {

    File getAtlasOutputFile();

    Collection<ID> listPackedAssets();


    Collection<File> listPages();

}
