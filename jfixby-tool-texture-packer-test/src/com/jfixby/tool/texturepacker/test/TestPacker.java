package com.jfixby.tool.texturepacker.test;

import java.io.IOException;

import com.jfixby.scarabei.api.desktop.ScarabeiDesktop;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.tools.gdx.texturepacker.GdxTexturePacker;
import com.jfixby.tools.gdx.texturepacker.api.AtlasPackingResult;
import com.jfixby.tools.gdx.texturepacker.api.Packer;
import com.jfixby.tools.gdx.texturepacker.api.TexturePacker;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackingSpecs;

public class TestPacker {

    public static void main(String[] args) throws IOException {
	ScarabeiDesktop.deploy();
	TexturePacker.installComponent(new GdxTexturePacker());
	TexturePackingSpecs specs = TexturePacker.newPackingSpecs();
	specs.setDebugMode(!true);
	File input_raster_folder = LocalFileSystem.ApplicationHome().child("input");
	specs.setInputRasterFolder(input_raster_folder);
	File output_atlas_folder = LocalFileSystem.ApplicationHome().child("output");
	output_atlas_folder.makeFolder();

	specs.setOutputAtlasFolder(output_atlas_folder);
	specs.setOutputAtlasFileName("atlas");

	Packer packer = TexturePacker.newPacker(specs);

	AtlasPackingResult result = packer.pack();

	result.print();

    }

}
