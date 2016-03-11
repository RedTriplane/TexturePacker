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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.jfixby.cmns.adopted.gdx.fs.ToGdxFileAdaptor;
import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.debug.Debug;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.log.L;

public class ETC1AtlasCompressor {

    public static ETC1AtlasCompressorSettings newCompressionSettings() {
	return new ETC1AtlasCompressorSettings();
    }

    public static ETC1AtlasCompressionResult compress(ETC1AtlasCompressorSettings settings) throws Exception {

	File atlasFile = Debug.checkNull("atlas_file_path_string", settings.getAtlasFile());

	L.d("compressing atlas to ETC1", atlasFile);
	GdxNativesLoader.load();

	ETC1AtlasCompressionResult result = new ETC1AtlasCompressionResult();

	result.setAtlasFile(atlasFile);

	File atlasFolder = atlasFile.parent();

	FileHandle gdxAtlasFile = new ToGdxFileAdaptor(atlasFile);
	FileHandle gdxAtlasFolder = new ToGdxFileAdaptor(atlasFolder);
	TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(gdxAtlasFile, gdxAtlasFolder, false);

	AlphaInfoContainer alphaInfo = new AlphaInfoContainer();

	Array<Page> pages = data.getPages();

	String atlasData = atlasFile.readToString();

	Color transparentColor = settings.getTransparentColor();

	boolean removeAlpha = settings.removeAlpha();
	transparentColor = checkNullCollorSetDefault(transparentColor);
	result.setTransparentColor(transparentColor);
	alphaInfo.setTransparentColor(transparentColor);
	for (int i = 0; i < pages.size; i++) {
	    Page page_i = pages.get(i);
	    ToGdxFileAdaptor gdxFile = (ToGdxFileAdaptor) page_i.textureFile;
	    File pageFile = gdxFile.getFixbyFile();
	    String oldPageFileName = pageFile.getName();
	    String pageFileName = pageFile.nameWithoutExtension();
	    String newPageFileName = pageFileName + ".etc1";

	    collectAlphaInfo(pageFile, alphaInfo, newPageFileName);

	    // tell ETC1Compressor to process only related files, not the whole
	    // folder
	    compressTexture(atlasFolder, oldPageFileName, newPageFileName, transparentColor, removeAlpha);

	    atlasData = atlasData.replaceAll(oldPageFileName, newPageFileName);

	    File compressedPageFile = pageFile.parent().child(newPageFileName);
	    L.d("  page " + i, pageFile);
	    L.d("   to", compressedPageFile);

	    result.addCompressedTextureNames(oldPageFileName, newPageFileName);
	    if (settings.deleteOriginalPNG()) {
		pageFile.delete();
	    }

	}

	byte[] alphaInfoBytes = alphaInfo.toByteArray();
	String alphaInfoFileName = atlasFile.getName() + ".alpha-info";
	File alphaInfoFile = atlasFolder.child(alphaInfoFileName);
	alphaInfoFile.writeBytes(alphaInfoBytes);

	atlasFile.writeString(atlasData);

	return result;
    }

    private static Color checkNullCollorSetDefault(Color color) {
	if (color == null) {
	    return Colors.FUCHSIA();
	}
	return color;
    }

    private static void collectAlphaInfo(File file, AlphaInfoContainer alphaInfo, String newPageFileName) {
	FileHandle pageFile = new FileHandle(file.toJavaFile());
	Pixmap pixmap = new Pixmap(pageFile);
	int W = pixmap.getWidth();
	int H = pixmap.getHeight();
	alphaInfo.beginFile(newPageFileName, W, H);
	for (int x = 0; x < W; x++) {
	    for (int y = 0; y < H; y++) {
		int value = pixmap.getPixel(x, y);
		int alpha = value & 0x000000ff;
		alphaInfo.addAlphaValue(alpha);
	    }
	}
	alphaInfo.endFile(newPageFileName);
	pixmap.dispose();
    }

    private static void compressTexture(File atlasFolder, String oldPageFileName, String newPageFileName,
	    Color transparentColor, boolean removeAlpha) {
	File oldPageFile = atlasFolder.child(oldPageFileName);
	File newPageFile = atlasFolder.child(newPageFileName);
	ETC1Compressor.compressFile(oldPageFile, newPageFile, transparentColor, removeAlpha);
    }

}
