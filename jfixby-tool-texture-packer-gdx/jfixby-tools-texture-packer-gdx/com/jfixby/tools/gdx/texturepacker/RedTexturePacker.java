
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

	private final File png_input_dir;
	private final File atlas_output_dir;
	private String output_atlas_filename = "";

	private final FileSystem output_file_system;
	private final FileSystem input_file_system;

	private final FileFilter png_filter = new FileFilter() {
		@Override
		public boolean fits (final File child) {
			return child.getName().toLowerCase().endsWith(".png") || child.getName().toLowerCase().endsWith(".jpg");
		}
	};
	private final boolean debug_mode;
	private final int max_page_size;
	private final int padding;
	private final int min_page_size;

	public RedTexturePacker (final TexturePackingSpecs packer_specs) {
		Debug.checkNull("packer_specs", packer_specs);

		this.png_input_dir = packer_specs.getInputRasterFolder();
		Debug.checkNull("getInputRasterFolder()", this.png_input_dir);

		this.atlas_output_dir = packer_specs.getOutputAtlasFolder();
		Debug.checkNull("getOutputAtlasFolder()", this.atlas_output_dir);

		this.output_atlas_filename = packer_specs.getAtlasFileName();
		Debug.checkNull("getAtlasFileName()", this.output_atlas_filename);
		Debug.checkEmpty("getAtlasFileName()", this.output_atlas_filename);
		this.output_atlas_filename = this.output_atlas_filename + Settings.atlasExtension;

		this.debug_mode = packer_specs.getDebugMode();
		this.output_file_system = this.atlas_output_dir.getFileSystem();
		this.input_file_system = this.png_input_dir.getFileSystem();

		this.max_page_size = packer_specs.getMaxPageSize();
		this.min_page_size = packer_specs.getMinPageSize();
		this.padding = packer_specs.getPadding();

	}

	@Override
	public AtlasPackingResult pack () throws IOException {
		final File output_home_folder = this.atlas_output_dir;
		final File png_input_folder = this.png_input_dir;

		final RedAtlasPackingResult result = new RedAtlasPackingResult();

		output_home_folder.makeFolder();

		final File temp_folder = this.create_temp_folder(output_home_folder);
		final File tmp_input_sprites_folder = this.output_file_system
			.newFile(temp_folder.child("#input_sprites_tmp_folder#").getAbsoluteFilePath());
		tmp_input_sprites_folder.makeFolder();

		final boolean ok = this.copy_all_png_files(png_input_folder, tmp_input_sprites_folder);

		final File tmp_output_atlas_folder = this.output_file_system
			.newFile(temp_folder.child("output_atlas").getAbsoluteFilePath());
		tmp_output_atlas_folder.makeFolder();

		final TempPngNamesKeeper tmp_names_keeper = new TempPngNamesKeeper();
		this.rename_all_sprite_to_temp_names(tmp_names_keeper, tmp_input_sprites_folder, result);
		this.pack_atlas(tmp_input_sprites_folder, tmp_output_atlas_folder);

		File atlas_file = this.fix_atlas_file(tmp_output_atlas_folder, tmp_names_keeper, result);
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

		this.output_file_system.copyFolderContentsToFolder(tmp_output_atlas_folder, output_home_folder);

		temp_folder.delete();
		// L.d("---------------------------------DONE---------------------------------");

		atlas_file = output_home_folder.child(atlas_file.getName());

		result.setAtlasOutputFile(atlas_file);

		this.collectTextures(atlas_file, result);

		return result;
	}

	private void collectTextures (final File atlas_file, final RedAtlasPackingResult result) {
		L.d("reading ", atlas_file);
		if (!atlas_file.exists()) {

			atlas_file.parent().listChildren().print();
			Err.reportError("File no found " + atlas_file);
		}
		final ToGdxFileAdaptor gdxAtlasFile = new ToGdxFileAdaptor(atlas_file);
		final TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(gdxAtlasFile, gdxAtlasFile.parent(), false);
		final Array<Page> pages = data.getPages();
		for (int i = 0; i < pages.size; i++) {
			final Page page_i = pages.get(i);
			final ToGdxFileAdaptor gdxFile = (ToGdxFileAdaptor)page_i.textureFile;
			final File pageFile = gdxFile.getFixbyFile();
			result.addPage(pageFile);
		}
	}

	private File fix_atlas_file (final File tmp_output_atlas_folder, final TempPngNamesKeeper tmp_names_keeper,
		final RedAtlasPackingResult result) throws IOException {
		final File atlas_file = tmp_output_atlas_folder.child(this.output_atlas_filename);

		// output_file_system.newFile(tmp_output_atlas_folder
		// .child(output_atlas_filename).getAbsoluteFilePath());

		ByteArray bytes = atlas_file.readBytes();
		String file_content = JUtils.newString(bytes);
		for (int i = 0; i < tmp_names_keeper.size(); i++) {
			final String tmp_name = tmp_names_keeper.getTemporaryName(i);
			final String original_name = tmp_names_keeper.getOriginalName(tmp_name);
			L.d("reverting", tmp_name + " -> " + original_name);

			file_content = file_content.replaceAll(tmp_name, original_name);
		}

		final ChildrenList children = tmp_output_atlas_folder.listChildren();
		final ChildrenList atlases_list = children.filterFiles(this.png_filter);

		final List<File> png_files_to_rename = Collections.newList();
		for (int i = 0; i < atlases_list.size(); i++) {
			final File atlas_png_file = (atlases_list.getElementAt(i));
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
			final File atlas_png_file = png_files_to_rename.getElementAt(i);

			final String old_atlas_png_file_short_name = atlas_png_file.getName();
			// int string_len = old_atlas_png_file_short_name.length();
			// old_atlas_png_file_short_name = old_atlas_png_file_short_name
			// .substring(0, string_len - 4);
			final String new_atlas_png_file_short_name = this.output_atlas_filename + ".atlasdata." + id + ".png";

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

	private void pack_atlas (final File tmp_input_sprites_folder, final File tmp_output_atlas_folder) {

		this.pack_atlas((tmp_input_sprites_folder), (tmp_output_atlas_folder), this.output_atlas_filename, this.debug_mode);

	}

	private void pack_atlas (final File png_input_dir, final File atlas_output_dir, final String output_atlas_filename,
		final boolean debug) {

		L.d("png_input_dir        ", png_input_dir);
		L.d("atlas_output_dir     ", atlas_output_dir);
		L.d("output_atlas_filename", output_atlas_filename);

		L.d("---packing-atlas--------------------------------------");
		final Settings settings = new Settings();
		settings.debug = debug;
		settings.maxWidth = this.max_page_size;
		settings.maxHeight = this.max_page_size;
		settings.minHeight = this.min_page_size;
		settings.minWidth = this.min_page_size;
		settings.paddingX = this.padding;
		settings.paddingY = this.padding;
		// settings.jpegQuality = 0.1f;
		settings.format = Format.RGBA8888;
		Pack.process(settings, png_input_dir, atlas_output_dir, output_atlas_filename);

		L.d("---packing-atlas-done---------------------------------");

	}

	private void rename_all_sprite_to_temp_names (final TempPngNamesKeeper tmp_names_keeper, final File tmp_input_sprites_folder,
		final RedAtlasPackingResult result) {
		final ChildrenList sprites = tmp_input_sprites_folder.listChildren();
		for (int i = 0; i < sprites.size(); i++) {
			final File sprite_file = sprites.getElementAt(i);

			final String short_file_name = sprite_file.getName();

			final String old_name = short_file_name.substring(0, short_file_name.length() - ".png".length());
			result.addPackedAssetID(Names.newAssetID(old_name));

			final String new_name = tmp_names_keeper.newTempName();
			sprite_file.rename(new_name + ".png");
			tmp_names_keeper.remember(new_name, old_name);
		}

	}

	private boolean copy_all_png_files (final File from_folder, final File to_folder) throws IOException {
		final ChildrenList png_files = from_folder.listChildren().filterFiles(this.png_filter);
		if (png_files.size() == 0) {
			from_folder.listChildren().print("input files list");
			throw new IOException("No input found in folder " + from_folder);
		}

		this.output_file_system.copyFilesTo(png_files, to_folder);
		return true;
	}

	private File create_temp_folder (final File output_home_folder) {
		final String tmp_folder_name = "tmp-" + System.currentTimeMillis();
		final File tmp_folder = this.output_file_system.newFile(output_home_folder.child(tmp_folder_name).getAbsoluteFilePath());
		tmp_folder.makeFolder();
		return tmp_folder;
	}

}
