package com.badlogic.gdx.tools;


/** @author Nathan Sweet */
public class Entry {
	public com.jfixby.cmns.api.file.File inputFile;
	/** May be null. */
	public com.jfixby.cmns.api.file.File outputDir;
	public com.jfixby.cmns.api.file.File outputFile;
	public int depth;

	public Entry () {
	}

	public Entry (com.jfixby.cmns.api.file.File inputFile, com.jfixby.cmns.api.file.File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String toString () {
		return inputFile.toString();
	}
}