package com.jfixby.tools.gdx.texturepacker.api;

import com.jfixby.scarabei.api.collections.Collection;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.names.ID;

public interface AtlasPackingResult {

    File getAtlasOutputFile();

    Collection<ID> listPackedAssets();


    Collection<File> listPages();

}
