package com.badlogic.gdx.tools;


/** @author Nathan Sweet */
public class Entry {
	public com.jfixby.scarabei.api.file.File inputFile;
	/** May be null. */
	public com.jfixby.scarabei.api.file.File outputDir;
	public com.jfixby.scarabei.api.file.File outputFile;
	public int depth;

	public Entry () {
	}

	public Entry (com.jfixby.scarabei.api.file.File inputFile, com.jfixby.scarabei.api.file.File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String toString () {
		return inputFile.toString();
	}
}