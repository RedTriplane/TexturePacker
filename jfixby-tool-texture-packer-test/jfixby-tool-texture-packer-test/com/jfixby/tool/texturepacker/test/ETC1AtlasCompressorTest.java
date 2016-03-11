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

package com.jfixby.tool.texturepacker.test;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.tools.etc1.ETC1AtlasCompressionResult;
import com.badlogic.gdx.tools.etc1.ETC1AtlasCompressor;
import com.badlogic.gdx.tools.etc1.ETC1AtlasCompressorSettings;
import com.badlogic.gdx.utils.Array;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.log.L;
import com.jfixby.red.desktop.DesktopAssembler;
import com.jfixby.tools.gdx.texturepacker.GdxTexturePacker;
import com.jfixby.tools.gdx.texturepacker.api.AtlasPackingResult;
import com.jfixby.tools.gdx.texturepacker.api.Packer;
import com.jfixby.tools.gdx.texturepacker.api.TexturePacker;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackingSpecs;

public class ETC1AtlasCompressorTest implements ApplicationListener {

    private File regularAtlasPathFile;

    private File etc1AtlasPathFile;

    public ETC1AtlasCompressorTest(File regularAtlasPathFile, File etc1AtlasPathFile) {
	this.regularAtlasPathFile = regularAtlasPathFile;
	this.etc1AtlasPathFile = etc1AtlasPathFile;
    }

    public static void main(String[] args) throws Exception {
	DesktopAssembler.setup();
	TexturePacker.installComponent(new GdxTexturePacker());

	File homeFolder = LocalFileSystem.ApplicationHome();
	File spritesFolder = homeFolder.child("sprites");
	File regularAtlasFolder = homeFolder.child("atlas");
	File etc1AtlasFolder = homeFolder.child("atlas-etc1");

	String atlasFilename = "atlas_test.atlas";

	prepareTestAtlas(spritesFolder, regularAtlasFolder, atlasFilename);
	AtlasPackingResult atlas_packing_result = prepareTestAtlas(spritesFolder, etc1AtlasFolder, atlasFilename);

	String outputAtlasFilename = atlas_packing_result.getAtlasOutputFile().getName();
	File regularAtlasFilePath = regularAtlasFolder.child(outputAtlasFilename);
	File etc1AtlasFilePath = etc1AtlasFolder.child(outputAtlasFilename);

	boolean COMPRESS = true;

	if (COMPRESS) {
	    ETC1AtlasCompressorSettings settings = ETC1AtlasCompressor.newCompressionSettings();
	    settings.setAtlasFile(etc1AtlasFilePath);
	    // Color fuxia = new com.badlogic.gdx.graphics.Color(1f, 0f, 1f,
	    // 1f);
	    // settings.setTransparentColor(fuxia);
	    settings.setDeleteOriginalPNG(!true);
	    L.d();
	    ETC1AtlasCompressionResult compressionResult = ETC1AtlasCompressor.compress(settings);
	    L.d();
	    compressionResult.print();
	}

	L.d("Showing compressed sprites");
	new LwjglApplication(new ETC1AtlasCompressorTest(regularAtlasFilePath, etc1AtlasFilePath), "", 1024, 768);

    }

    private static AtlasPackingResult prepareTestAtlas(File input_raster_folder, File output_atlas_folder,
	    String outputAtlasFilename) throws IOException {
	L.d("input_raster_folder", input_raster_folder);
	L.d("output_atlas_folder", output_atlas_folder);
	L.d("outputAtlasFilename", outputAtlasFilename);

	TexturePackingSpecs specs = TexturePacker.newPackingSpecs();
	specs.setDebugMode(!true);
	specs.setInputRasterFolder(input_raster_folder);
	output_atlas_folder.makeFolder();
	specs.setOutputAtlasFolder(output_atlas_folder);
	specs.setOutputAtlasFileName(outputAtlasFilename);
	Packer packer = TexturePacker.newPacker(specs);
	AtlasPackingResult result = packer.pack();
	result.print();
	return result;

    }

    /// -------------------------------------------------------------------------------------------------

    SpriteBatch batch;

    private TextureAtlas regularAtlas;
    private Array<Sprite> regularSprites;

    private TextureAtlas etc1Atlas;
    private Array<Sprite> etc1Sprites;

    public void create() {
	batch = new SpriteBatch();

	regularAtlas = new TextureAtlas(this.regularAtlasPathFile.toJavaFile().getAbsolutePath());
	regularSprites = regularAtlas.createSprites();

	etc1Atlas = new TextureAtlas(this.etc1AtlasPathFile.toJavaFile().getAbsolutePath());
	etc1Sprites = etc1Atlas.createSprites();
	float x = 10;
	float y = 10;
	for (int i = 0; i < regularSprites.size; i++) {
	    Sprite sprite = regularSprites.get(i);
	    sprite.setX(x);
	    sprite.setY(10);
	    x = x + sprite.getWidth() * 0.9f;
	    y = Math.max(y, sprite.getHeight());
	}
	x = 10;
	for (int i = 0; i < etc1Sprites.size; i++) {
	    Sprite sprite = etc1Sprites.get(i);
	    sprite.setX(x);
	    sprite.setY(y * 1.1f);
	    x = x + sprite.getWidth() * 0.9f;
	}

    }

    public void render() {
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

	int x = 20, y = 20;
	batch.begin();
	for (Sprite sprite : regularSprites) {
	    sprite.draw(batch);
	}
	for (Sprite sprite : etc1Sprites) {
	    sprite.draw(batch);
	}
	batch.end();
    }

    public void resize(int width, int height) {
	float m = 0.6f;
	batch.setProjectionMatrix(
		new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth() * m, Gdx.graphics.getHeight() * m));
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
