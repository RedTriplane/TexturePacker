
package com.badlogic.gdx.tools.texturepacker;

import java.io.IOException;
import java.io.OutputStream;

import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.FileOutputStream;
import com.jfixby.cmns.api.log.L;

public class FileWriter {

	private final java.io.OutputStreamWriter w;
	private final FileOutputStream os;

	public FileWriter (final File packFile, final boolean append) throws IOException {
// packFile.toJavaFile()
		this.os = packFile.newOutputStream(append);
		this.os.open();
		final OutputStream jos = this.os.toJavaOutputStream();
		this.w = new java.io.OutputStreamWriter(jos);
// this.w = new java.io.FileWriter(packFile.toJavaFile(), b);
	}

	public void write (final String string) throws IOException {
		L.d(">>>>", string);
		this.w.write(string);
	}

	public void close () throws IOException {
		this.w.close();
		this.os.close();

	}

}
