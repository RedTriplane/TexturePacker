package com.badlogic.gdx.tools;

import com.badlogic.gdx.files.FileHandle;
import com.jfixby.cmns.adopted.gdx.fs.ToGdxFileAdaptor;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.log.L;

public class FileWrapper {

	public static File file(String path) {

		java.io.File f = new java.io.File(path);
		L.d("File", f.getAbsolutePath());
		//
		File file = LocalFileSystem.newFile(path);

		L.d("File >>> ", file);
		throw new Error();

		// return;
	}

	public static File file(File root, String string) {
		return root.child(string);
	}

	public static File file(File input) {
		return input;
	}

	public static FileHandle newFileHandle(File outputFile) {
		ToGdxFileAdaptor gdx_file = new ToGdxFileAdaptor(outputFile);
		return gdx_file;
	}

}
