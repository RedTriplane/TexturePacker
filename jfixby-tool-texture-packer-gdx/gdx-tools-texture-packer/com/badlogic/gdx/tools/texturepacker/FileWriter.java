package com.badlogic.gdx.tools.texturepacker;

import java.io.IOException;

import com.jfixby.cmns.api.filesystem.File;

public class FileWriter {

	private java.io.FileWriter w;

	public FileWriter(File packFile, boolean b) throws IOException {
		w = new java.io.FileWriter(packFile.toJavaFile(), b);
	}

	public void write(String string) throws IOException {
		w.write(string);
	}

	public void close() throws IOException {
		w.close();
	}

}
