/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;
import com.jfixby.scarabei.api.collections.Collection;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.file.File;

/** Collects files recursively, filtering by file name. Callbacks are provided to process files and the results are collected,
 * either {@link #processFile(Entry)} or {@link #processDir(Entry, ArrayList)} can be overridden, or both. The entries provided to
 * the callbacks have the original file, the output directory, and the output file. If {@link #setFlattenOutput(boolean)} is
 * false, the output will match the directory structure of the input.
 *
 * @author Nathan Sweet */
public class FileProcessor {
	FilenameFilter inputFilter;
	Comparator<File> comparator = new Comparator<File>() {
		@Override
		public int compare (final File o1, final File o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	Array<Pattern> inputRegex = new Array();
	String outputSuffix;
	ArrayList<Entry> outputFiles = new ArrayList();
	boolean recursive = true;
	final boolean flattenOutput = true;

	Comparator<Entry> entryComparator = new Comparator<Entry>() {
		@Override
		public int compare (final Entry o1, final Entry o2) {
			return FileProcessor.this.comparator.compare(o1.inputFile, o2.inputFile);
		}
	};

	public FileProcessor () {
	}

	public FileProcessor setInputFilter (final FilenameFilter inputFilter) {
		this.inputFilter = inputFilter;
		return this;
	}

	/** Sets the comparator for {@link #processDir(Entry, ArrayList)}. By default the files are sorted by alpha. */
	public FileProcessor setComparator (final Comparator<File> comparator) {
		this.comparator = comparator;
		return this;
	}

	/** Adds a case insensitive suffix for matching input files. */
	public FileProcessor addInputSuffix (final String... suffixes) {
		for (final String suffix : suffixes) {
			this.addInputRegex("(?i).*" + Pattern.quote(suffix));
		}
		return this;
	}

	public FileProcessor addInputRegex (final String... regexes) {
		for (final String regex : regexes) {
			this.inputRegex.add(Pattern.compile(regex));
		}
		return this;
	}

	/** Sets the suffix for output files, replacing the extension of the input file. */
	public FileProcessor setOutputSuffix (final String outputSuffix) {
		this.outputSuffix = outputSuffix;
		return this;
	}

	/** Default is true. */
	public FileProcessor setRecursive (final boolean recursive) {
		this.recursive = recursive;
		return this;
	}

	/** @param outputRoot May be null.
	 * @see #process(File, File) */
	public ArrayList<Entry> process (final String inputFile, final String outputRoot) throws Exception {
		return this.process(FileWrapper.file(inputFile), outputRoot == null ? null : FileWrapper.file(outputRoot));
	}

	/** Processes the specified input file or directory.
	 *
	 * @param outputRoot May be null if there is no output from processing the files.
	 * @return the processed files added with {@link #addProcessedFile(Entry)}. */
	public ArrayList<Entry> process (final File inputFile, final File outputRoot) throws Exception {
		if (!inputFile.exists()) {
			throw new IllegalArgumentException("Input file does not exist: " + inputFile.toJavaFile().getAbsolutePath());
		}
		if (inputFile.isFile()) {
			return this.process(Collections.newList(inputFile), outputRoot);
		} else {
			return this.process(inputFile.listDirectChildren(), outputRoot);
		}
	}

	/** Processes the specified input files.
	 *
	 * @param outputRoot May be null if there is no output from processing the files.
	 * @return the processed files added with {@link #addProcessedFile(Entry)}. */
	public ArrayList<Entry> process (final Collection<File> files, final File outputRoot) throws Exception {

		// files.print("processing files");
		// L.d("outputRoot", outputRoot);

		Debug.checkNull("outputRoot", outputRoot);
		this.outputFiles.clear();
		final DirToEntries dirToEntries = new DirToEntries();
		this.process(files, outputRoot, outputRoot, dirToEntries, 0);
		// dirToEntries.print();

		final ArrayList<Entry> allEntries = new ArrayList();
		for (int i = 0; i < dirToEntries.size(); i++) {
			final File inputDir = dirToEntries.getKey(i);
			final ArrayList<Entry> dirEntries = dirToEntries.getValue(i);
			if (this.comparator != null) {
				java.util.Collections.sort(dirEntries, this.entryComparator);
			}

			File newOutputDir = null;
			if (this.flattenOutput) {
				newOutputDir = outputRoot;
			} else if (!dirEntries.isEmpty()) {
				newOutputDir = dirEntries.get(0).outputDir;
			}
			String outputName = inputDir.getName();
			if (this.outputSuffix != null) {
				outputName = outputName.replaceAll("(.*)\\..*", "$1") + this.outputSuffix;
			}

			final Entry entry = new Entry();
			entry.inputFile = inputDir;
			entry.outputDir = newOutputDir;

			if (newOutputDir != null) {
				// File v1 = F.file(outputName);
				final File v2 = FileWrapper.file(newOutputDir, outputName);
				entry.outputFile = v2;
			}
			try {
				this.processDir(entry, dirEntries);
			} catch (final Exception ex) {
				throw new Exception("Error processing directory: " + entry.inputFile.toJavaFile().getAbsolutePath(), ex);
			}
			allEntries.addAll(dirEntries);
		}

		if (this.comparator != null) {
			java.util.Collections.sort(allEntries, this.entryComparator);
		}
		for (final Entry entry : allEntries) {
			try {
				this.processFile(entry);
			} catch (final Exception ex) {
				throw new Exception("Error processing file: " + entry.inputFile.toJavaFile().getAbsolutePath(), ex);
			}
		}

		return this.outputFiles;
	}

	private void process (final Collection<File> files, final File outputRoot, final File outputDir,
		final DirToEntries dirToEntries, final int depth) throws IOException {
		// Store empty entries for every directory.
		for (final File file : files) {
			final File dir = file.parent();
			ArrayList<Entry> entries = dirToEntries.get(dir);
			if (entries == null) {
				entries = new ArrayList();
				dirToEntries.put(dir, entries);
			}
		}

		for (final File file : files) {
			if (file.isFile()) {
				if (this.inputRegex.size > 0) {
					boolean found = false;
					for (final Pattern pattern : this.inputRegex) {
						if (pattern.matcher(file.getName()).matches()) {
							found = true;
							continue;
						}
					}
					if (!found) {
						continue;
					}
				}

				final File dir = file.parent();
				if (this.inputFilter != null && !this.inputFilter.fits(file)) {
					continue;
				}

				String outputName = file.getName();
				if (this.outputSuffix != null) {
					outputName = outputName.replaceAll("(.*)\\..*", "$1") + this.outputSuffix;
				}

				final Entry entry = new Entry();
				entry.depth = depth;
				entry.inputFile = file;
				entry.outputDir = outputDir;

				if (this.flattenOutput) {
					entry.outputFile = FileWrapper.file(outputRoot, outputName);
				} else {
					entry.outputFile = FileWrapper.file(outputDir, outputName);
				}

				dirToEntries.get(dir).add(entry);
			}
			if (this.recursive && file.isFolder()) {
				final File subdir = outputDir.toJavaFile().getPath().length() == 0 ? FileWrapper.file(file.getName())
					: FileWrapper.file(outputDir, file.getName());
				this.process(file.listDirectChildren().filter(this.inputFilter), outputRoot, subdir, dirToEntries, depth + 1);
			}
		}
	}

	/** Called with each input file. */
	protected void processFile (final Entry entry) throws Exception {
	}

	/** Called for each input directory. The files will be {@link #setComparator(Comparator) sorted}. */
	protected void processDir (final Entry entryDir, final ArrayList<Entry> files) throws Exception {
	}

	/** This method should be called by {@link #processFile(Entry)} or {@link #processDir(Entry, ArrayList)} if the return value of
	 * {@link #process(File, File)} or {@link #process(File[], File)} should return all the processed files. */
	protected void addProcessedFile (final Entry entry) {
		this.outputFiles.add(entry);
	}
}
