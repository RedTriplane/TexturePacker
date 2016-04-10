package com.jfixby.tools.gdx.texturepacker;

import java.io.IOException;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.tools.texturepacker.Pack;
import com.badlogic.gdx.tools.texturepacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.jfixby.cmns.adopted.gdx.fs.ToGdxFileAdaptor;
import com.jfixby.cmns.api.assets.Names;
import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.debug.Debug;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.ChildrenList;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.FileFilter;
import com.jfixby.cmns.api.file.FileSystem;
import com.jfixby.cmns.api.java.ByteArray;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.util.JUtils;
import com.jfixby.tools.gdx.texturepacker.api.AtlasPackingResult;
import com.jfixby.tools.gdx.texturepacker.api.Packer;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackingSpecs;

public class RedTexturePacker implements Packer {

    private File png_input_dir;
    private File atlas_output_dir;
    private String output_atlas_filename = "";

    private FileSystem output_file_system;
    private FileSystem input_file_system;

    private FileFilter png_filter = new FileFilter() {
	@Override
	public boolean fits(File child) {
	    return child.getName().toLowerCase().endsWith(".png") || child.getName().toLowerCase().endsWith(".jpg");
	}
    };
    private boolean debug_mode;
    private int max_page_size;

    public RedTexturePacker(TexturePackingSpecs packer_specs) {
	Debug.checkNull("packer_specs", packer_specs);

	png_input_dir = packer_specs.getInputRasterFolder();
	Debug.checkNull("getInputRasterFolder()", png_input_dir);

	atlas_output_dir = packer_specs.getOutputAtlasFolder();
	Debug.checkNull("getOutputAtlasFolder()", atlas_output_dir);

	output_atlas_filename = packer_specs.getAtlasFileName();
	Debug.checkNull("getAtlasFileName()", output_atlas_filename);
	Debug.checkEmpty("getAtlasFileName()", output_atlas_filename);
	output_atlas_filename = output_atlas_filename + Settings.atlasExtension;

	debug_mode = packer_specs.getDebugMode();
	output_file_system = atlas_output_dir.getFileSystem();
	input_file_system = png_input_dir.getFileSystem();

	this.max_page_size = packer_specs.getMaxPageSize();

    }

    @Override
    public AtlasPackingResult pack() throws IOException {
	File output_home_folder = atlas_output_dir;
	File png_input_folder = png_input_dir;

	RedAtlasPackingResult result = new RedAtlasPackingResult();

	output_home_folder.makeFolder();

	File temp_folder = create_temp_folder(output_home_folder);
	File tmp_input_sprites_folder = output_file_system
		.newFile(temp_folder.child("#input_sprites_tmp_folder#").getAbsoluteFilePath());
	tmp_input_sprites_folder.makeFolder();

	boolean ok = copy_all_png_files(png_input_folder, tmp_input_sprites_folder);

	File tmp_output_atlas_folder = output_file_system
		.newFile(temp_folder.child("output_atlas").getAbsoluteFilePath());
	tmp_output_atlas_folder.makeFolder();

	TempPngNamesKeeper tmp_names_keeper = new TempPngNamesKeeper();
	rename_all_sprite_to_temp_names(tmp_names_keeper, tmp_input_sprites_folder, result);
	pack_atlas(tmp_input_sprites_folder, tmp_output_atlas_folder);

	File atlas_file = fix_atlas_file(tmp_output_atlas_folder, tmp_names_keeper, result);
	// L.d("---------------GEMSERK-----------------");
	if (false) {
	    // ChildrenList atlas_data_files = tmp_output_atlas_folder
	    // .listChildren().filterChildren(png_filter);
	    // for (int i = 0; i < atlas_data_files.size(); i++) {
	    // File atlas_data_file = DesktopFileSystem
	    // .newFile(atlas_data_files.getChild(i));
	    // // String file_path = atlas_data_file.getAbsoluteFilePath()
	    // // .toAbsolutePathString();
	    // // L.d("Gemserk-processing file", file_path);
	    //
	    // java.io.File file_to_process = DesktopFileSystem
	    // .toJavaFile(atlas_data_file);
	    //
	    // try {
	    // Magic.process(file_to_process, file_to_process);
	    // } catch (IOException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // }
	    // L.d("--------------------");
	    // }

	}

	output_file_system.copyFolderContentsToFolder(tmp_output_atlas_folder, output_home_folder);

	temp_folder.delete();
	// L.d("---------------------------------DONE---------------------------------");

	atlas_file = output_home_folder.child(atlas_file.getName());

	result.setAtlasOutputFile(atlas_file);

	collectTextures(atlas_file, result);

	return result;
    }

    private void collectTextures(File atlas_file, RedAtlasPackingResult result) {
	L.d("reading ", atlas_file);
	if (!atlas_file.exists()) {

	    atlas_file.parent().listChildren().print();
	    Err.reportError("File no found " + atlas_file);
	}
	ToGdxFileAdaptor gdxAtlasFile = new ToGdxFileAdaptor(atlas_file);
	TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(gdxAtlasFile, gdxAtlasFile.parent(),
		false);
	Array<Page> pages = data.getPages();
	for (int i = 0; i < pages.size; i++) {
	    Page page_i = pages.get(i);
	    ToGdxFileAdaptor gdxFile = (ToGdxFileAdaptor) page_i.textureFile;
	    File pageFile = gdxFile.getFixbyFile();
	    result.addPage(pageFile);
	}
    }

    private File fix_atlas_file(File tmp_output_atlas_folder, TempPngNamesKeeper tmp_names_keeper,
	    RedAtlasPackingResult result) throws IOException {
	File atlas_file = tmp_output_atlas_folder.child(output_atlas_filename);

	// output_file_system.newFile(tmp_output_atlas_folder
	// .child(output_atlas_filename).getAbsoluteFilePath());

	ByteArray bytes = atlas_file.readBytes();
	String file_content = JUtils.newString(bytes);
	for (int i = 0; i < tmp_names_keeper.size(); i++) {
	    String tmp_name = tmp_names_keeper.getTemporaryName(i);
	    String original_name = tmp_names_keeper.getOriginalName(tmp_name);
	    L.d("reverting", tmp_name + " -> " + original_name);

	    file_content = file_content.replaceAll(tmp_name, original_name);
	}

	ChildrenList children = tmp_output_atlas_folder.listChildren();
	ChildrenList atlases_list = children.filterFiles(png_filter);

	List<File> png_files_to_rename = Collections.newList();
	for (int i = 0; i < atlases_list.size(); i++) {
	    File atlas_png_file = (atlases_list.getElementAt(i));
	    png_files_to_rename.add(atlas_png_file);
	}

	// // png_files_to_rename.print("png_files_to_rename");
	// int number_of_files_to_rename = png_files_to_rename.size();
	// String file_name_pefix = "#input_sprites_tmp_folder#";
	// String file_name_postfix = ".png";
	// for (int i = 0; i < number_of_files_to_rename; i++) {
	// File atlas_png_file = tmp_output_atlas_folder.child(file_name_pefix
	// + i + file_name_postfix);
	//
	// }
	long id = 0;
	for (int i = 0; i < png_files_to_rename.size(); i++) {
	    File atlas_png_file = png_files_to_rename.getElementAt(i);

	    String old_atlas_png_file_short_name = atlas_png_file.getName();
	    // int string_len = old_atlas_png_file_short_name.length();
	    // old_atlas_png_file_short_name = old_atlas_png_file_short_name
	    // .substring(0, string_len - 4);
	    String new_atlas_png_file_short_name = this.output_atlas_filename + ".atlasdata." + id + ".png";

	    // L.d("renaming", atlas_png_file.getAbsoluteFilePath()
	    // .toAbsolutePathString());
	    atlas_png_file.rename(new_atlas_png_file_short_name);
	    // L.d(" to", new_atlas_png_file_short_name);
	    file_content = file_content.replaceAll(old_atlas_png_file_short_name, new_atlas_png_file_short_name);
	    // L.d("replace", old_atlas_png_file_short_name);
	    // L.d(" to", new_atlas_png_file_short_name);

	    id++;
	}

	bytes = JUtils.newByteArray(file_content.getBytes());
	atlas_file.writeBytes(bytes);

	return atlas_file;
	// L.d("atlas file &&&", file_content);

    }

    private void pack_atlas(File tmp_input_sprites_folder, File tmp_output_atlas_folder) {

	pack_atlas((tmp_input_sprites_folder), (tmp_output_atlas_folder), this.output_atlas_filename, this.debug_mode,
		this.max_page_size);

    }

    private static void pack_atlas(File png_input_dir, File atlas_output_dir, String output_atlas_filename,
	    boolean debug, int max_page_size) {

	L.d("png_input_dir        ", png_input_dir);
	L.d("atlas_output_dir     ", atlas_output_dir);
	L.d("output_atlas_filename", output_atlas_filename);

	L.d("---packing-atlas--------------------------------------");
	Settings settings = new Settings();
	settings.debug = debug;
	settings.maxWidth = max_page_size;
	settings.maxHeight = max_page_size;
	// settings.jpegQuality = 0.1f;
	settings.format = Format.RGBA8888;
	Pack.process(settings, png_input_dir, atlas_output_dir, output_atlas_filename);

	L.d("---packing-atlas-done---------------------------------");

    }

    private void rename_all_sprite_to_temp_names(TempPngNamesKeeper tmp_names_keeper, File tmp_input_sprites_folder,
	    RedAtlasPackingResult result) {
	ChildrenList sprites = tmp_input_sprites_folder.listChildren();
	for (int i = 0; i < sprites.size(); i++) {
	    File sprite_file = sprites.getElementAt(i);

	    String short_file_name = sprite_file.getName();

	    String old_name = short_file_name.substring(0, short_file_name.length() - ".png".length());
	    result.addPackedAssetID(Names.newAssetID(old_name));

	    String new_name = tmp_names_keeper.newTempName();
	    sprite_file.rename(new_name + ".png");
	    tmp_names_keeper.remember(new_name, old_name);
	}

    }

    private boolean copy_all_png_files(File from_folder, File to_folder) throws IOException {
	ChildrenList png_files = from_folder.listChildren().filterFiles(png_filter);
	if (png_files.size() == 0) {
	    from_folder.listChildren().print("input files list");
	    throw new IOException("No input found in folder " + from_folder);
	}

	output_file_system.copyFilesTo(png_files, to_folder);
	return true;
    }

    private File create_temp_folder(File output_home_folder) {
	String tmp_folder_name = "tmp-" + System.currentTimeMillis();
	File tmp_folder = output_file_system.newFile(output_home_folder.child(tmp_folder_name).getAbsoluteFilePath());
	tmp_folder.makeFolder();
	return tmp_folder;
    }

}
