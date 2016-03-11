/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools.etc1;

import java.util.ArrayList;

import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.log.L;

public class ETC1AtlasCompressionResult {

    private File atlas;
    private ArrayList<TextureFileRenaming> textures = new ArrayList<TextureFileRenaming>();

    private Color transparentColor;

    public void setAtlasFile(File atlas) {
	this.atlas = atlas;
    }

    public void setTransparentColor(Color transparentColor) {
	this.transparentColor = transparentColor;
    }

    public void addCompressedTextureNames(String oldPageFileName, String newPageFileName) {
	TextureFileRenaming renaming = new TextureFileRenaming(oldPageFileName, newPageFileName);
	textures.add(renaming);
    }

    public void print() {
	L.d("AtlasETC1CompressionResult[" + textures.size() + "]");
	L.d(" atlas file", atlas);
	L.d(" transparent color", transparentColor);
	for (int i = 0; i < textures.size(); i++) {
	    TextureFileRenaming renaming = textures.get(i);
	    L.d("   " + i, renaming);
	}

    }

    public File getAtlasPath() {
	return this.atlas;
    }

}
