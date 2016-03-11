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

import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.file.File;

public class ETC1AtlasCompressorSettings {

    private File atlasFile;
    private Color transparentColor;
    private boolean deleteOriginalPNG = false;
    private boolean removeAlpha = true;

    public void setRemoveAlpha(boolean removeAlpha) {
	this.removeAlpha = removeAlpha;
    }

    public void setAtlasFile(File atlasFile) {
	this.atlasFile = atlasFile;
    }

    public File getAtlasFile() {
	return atlasFile;
    }

    public Color getTransparentColor() {
	return transparentColor;
    }

    public void setTransparentColor(Color transparentColor) {
	this.transparentColor = transparentColor;
    }

    public boolean deleteOriginalPNG() {
	return deleteOriginalPNG;
    }

    public void setDeleteOriginalPNG(boolean deleteOriginalPNG) {
	this.deleteOriginalPNG = deleteOriginalPNG;
    }

    public boolean removeAlpha() {
	return removeAlpha;
    }

}
