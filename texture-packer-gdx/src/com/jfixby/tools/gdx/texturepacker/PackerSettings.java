package com.jfixby.tools.gdx.texturepacker;

public class PackerSettings {

	public FILTER_OPTIONS getFilterMag() {
		return filterMag;
	}

	public void setFilterMag(FILTER_OPTIONS filterMag) {
		this.filterMag = filterMag;
		if (filterMag == null) {
			filterMag = FILTER_OPTIONS.MipMapLinearLinear;
		}
	}

	public FILTER_OPTIONS getFilterMin() {
		if (filterMin == null) {
			filterMin = FILTER_OPTIONS.MipMapLinearLinear;
		}
		return filterMin;
	}

	public void setFilterMin(FILTER_OPTIONS filterMin) {
		this.filterMin = filterMin;
	}

	FILTER_OPTIONS filterMag = FILTER_OPTIONS.MipMapLinearLinear;
	FILTER_OPTIONS filterMin = FILTER_OPTIONS.MipMapLinearLinear;
	private boolean debug;

	// public static final String DEFAULT_SETTINGS_STRING = "alias=true"
	// + "alphaThreshold=0" + "\n" //
	// + "debug=false" + "\n" //
	// + "defaultFileFormat=PNG" + "\n" //
	// + "defaultFilterMag=MipMapLinearLinear" + "\n" //
	// + "defaultFilterMin=MipMapLinearLinear" + "\n" //
	// + "defaultFormat=RGBA8888" + "\n" //
	// + "defaultImageQuality=1.0" + "\n" //
	// + "duplicatePadding=true" + "\n" //
	// + "edgePadding=true" + "\n" //
	// + "ignoreBlankImages=true" + "\n" //
	// + "incremental=false" + "\n" //
	// + "incrementalFilePath=" + "\n" //
	// + "maxHeight=1024" + "\n" //
	// + "maxWidth=1024" + "\n" //
	// + "minHeight=1024" + "\n" //
	// + "minWidth=1024" + "\n" //
	// + "padding=24" + "\n" //
	// + "pot=true" + "\n" //
	// + "rotate=false" + "\n" //
	// + "stripWhitespace=false"; //

	private static final String TAG_FILTER_MAG = "#defaultFilterMag#";// MipMapLinearLinear
	private static final String TAG_FILTER_MIN = "#defaultFilterMin#";// MipMapLinearLinear
	private static final String TAG_DEBUG = "#debug#";// MipMapLinearLinear

	public static final String SETTINGS_STRING_TEMPLATE = "alias=true"
			+ "alphaThreshold=0" + "\n" //
			+ "debug="
			+ TAG_DEBUG + "\n" //
			+ "defaultFileFormat=PNG" + "\n" //
			+ "defaultFilterMag=" + TAG_FILTER_MAG + "\n" //
			+ "defaultFilterMin=" + TAG_FILTER_MIN + "\n" //
			+ "defaultFormat=RGBA8888" + "\n" //
			+ "defaultImageQuality=1.0" + "\n" //
			+ "duplicatePadding=true" + "\n" //
			+ "edgePadding=true" + "\n" //
			+ "ignoreBlankImages=false" + "\n" //
			+ "incremental=false" + "\n" //
			+ "incrementalFilePath=" + "\n" //
			+ "maxHeight=1024" + "\n" //
			+ "maxWidth=1024" + "\n" //
			+ "minHeight=1024" + "\n" //
			+ "minWidth=1024" + "\n" //
			+ "padding=8" + "\n" //
			+ "pot=true" + "\n" //
			+ "rotate=false" + "\n" //
			+ "stripWhitespace=false"; //

	public String toGDXSettingsString() {
		return SETTINGS_STRING_TEMPLATE//
				.replaceAll(TAG_FILTER_MAG, this.filterMag.toString())//
				.replaceAll(TAG_FILTER_MIN, this.filterMin.toString())//
				.replaceAll(TAG_DEBUG, "" + this.debug)//
		;
	}

	public void setDebugFlag(boolean b) {
		this.debug = b;
	}

}
