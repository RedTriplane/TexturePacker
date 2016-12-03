
package com.badlogic.gdx.tools;

import com.badlogic.gdx.files.FileHandle;
import com.jfixby.cmns.adopted.gdx.fs.ToGdxFileAdaptor;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.log.L;

public class FileWrapper {

	public static File file (final String path) {

		final java.io.File f = new java.io.File(path);
		L.d("File", f.getAbsolutePath());
		//
		final File file = LocalFileSystem.newFile(path);

		L.d("File >>> ", file);
		Err.reportNotImplementedYet();
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
