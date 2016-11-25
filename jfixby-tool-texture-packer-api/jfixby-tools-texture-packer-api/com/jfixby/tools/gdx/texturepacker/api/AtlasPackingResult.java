package com.jfixby.tools.gdx.texturepacker.api;

import com.jfixby.cmns.api.assets.ID;
import com.jfixby.cmns.api.collections.Collection;
import com.jfixby.cmns.api.file.File;

public interface AtlasPackingResult {

    File getAtlasOutputFile();

    Collection<ID> listPackedAssets();

    void print();

    Collection<File> listPages();

}
