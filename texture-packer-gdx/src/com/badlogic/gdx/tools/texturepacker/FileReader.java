package com.badlogic.gdx.tools.texturepacker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import com.jfixby.scarabei.api.file.File;

public class FileReader extends Reader {

	private java.io.FileReader reader;

	public FileReader(File settingsFile) throws FileNotFoundException {
		reader = new java.io.FileReader(settingsFile.toJavaFile());
	}

	@Override
	public int read(char[] cbuf, int offset, int length) throws IOException {
		return reader.read(cbuf, offset, length);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

}
