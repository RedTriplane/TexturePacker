package com.badlogic.gdx.tools;


/** @author Nathan Sweet */
public class Entry {
	public com.jfixby.cmns.api.filesystem.File inputFile;
	/** May be null. */
	public com.jfixby.cmns.api.filesystem.File outputDir;
	public com.jfixby.cmns.api.filesystem.File outputFile;
	public int depth;

	public Entry () {
	}

	public Entry (com.jfixby.cmns.api.filesystem.File inputFile, com.jfixby.cmns.api.filesystem.File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String toString () {
		return inputFile.toString();
	}
}