
package com.badlogic.gdx.tools.etc1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class AlphaInfoPage {

	byte[] bytes;
	int pointer = 0;
	private String newPageFileName;

	public AlphaInfoPage (String newPageFileName, int w, int h) {
		bytes = new byte[w * h];
		this.newPageFileName = newPageFileName;
	}

	public void addAlphaValue (int alpha) {
		bytes[pointer] = (byte)alpha;
		pointer++;
	}

	public void checkValid (String newPageFileName) {
		if (!this.newPageFileName.equals(newPageFileName)) {
			throw new Error("AlphaInfoPage<" + this.newPageFileName + "> is corrupted");
		}
		if (pointer != bytes.length) {
			throw new Error("AlphaInfoPage<" + this.newPageFileName + "> is corrupted");
		}
	}

	public void writeTo (ByteArrayOutputStream buffer) throws IOException {
		checkValid(newPageFileName);
		ObjectOutputStream obj = new ObjectOutputStream(buffer);
		obj.writeObject(newPageFileName);
		obj.writeObject(bytes);
		obj.close();
	}

}
