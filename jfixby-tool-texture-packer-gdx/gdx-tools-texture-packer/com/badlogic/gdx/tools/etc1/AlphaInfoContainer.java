
package com.badlogic.gdx.tools.etc1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import com.jfixby.cmns.api.color.Color;



public class AlphaInfoContainer {

	private Color transparentColor;
	final ArrayList<AlphaInfoPage> pages = new ArrayList<AlphaInfoPage>();

	public void setTransparentColor (Color transparentColor) {
		this.transparentColor = transparentColor;
	}

	public void beginFile (String newPageFileName, int w, int h) {
		AlphaInfoPage newPage = new AlphaInfoPage(newPageFileName, w, h);
		pages.add(newPage);
	}

	public void addAlphaValue (int alpha) {
		pages.get(pages.size() - 1).addAlphaValue(alpha);
	}

	public void endFile (String newPageFileName) {
		pages.get(pages.size() - 1).checkValid(newPageFileName);
	}

	public byte[] toByteArray () throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int i = 0; i < pages.size(); i++) {
			writePage(buffer, pages.get(i));
		}
		buffer.flush();
		buffer.close();
		byte[] bytes = buffer.toByteArray();
		bytes = compressZIP(bytes);
		return bytes;

	}

	private byte[] compressZIP (byte[] bytes) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(buffer);
		zip.write(bytes);
		zip.flush();
		zip.close();
		buffer.close();
		return buffer.toByteArray();
	}

	private void writePage (ByteArrayOutputStream buffer, AlphaInfoPage alphaInfoPage) throws IOException {
		alphaInfoPage.writeTo(buffer);
	}

}
