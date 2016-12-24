
package com.badlogic.gdx.tools;

import com.badlogic.gdx.files.FileHandle;
import com.jfixby.scarabei.adopted.gdx.fs.ToGdxFileAdaptor;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.log.L;

public class FileWrapper {

	public static File file (final String path) {

		final java.io.File f = new java.io.File(path);
		L.d("File", f.getAbsolutePath());
		//
		final File file = LocalFileSystem.newFile(path);

		L.d("File >>> ", file);
		Err.throwNotImplementedYet();
		return file;

		// return;
	}

	public static File file (final File root, final String string) {
		return root.child(string);
	}

	public static File file (final File input) {
		return input;
	}

	public static FileHandle newFileHandle (final File outputFile) {
		final ToGdxFileAdaptor gdx_file = new ToGdxFileAdaptor(outputFile);
		return gdx_file;
	}

}
