
package com.badlogic.gdx.tools;

import java.util.ArrayList;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.file.File;

public class DirToEntries {
	class EntryData {

		@Override
		public String toString () {
			return this.value.toString();
		}

		public File key;
		public ArrayList<Entry> value;
	}

	final Map<File, EntryData> map = Collections.newMap();

	public ArrayList<Entry> get (final File dir) {
		final File key = dir;
		final EntryData val = this.map.get(key);
		if (val == null) {
// this.map.print("map");
			return null;
		}
		return val.value;
	}

	public void put (final File dir, final ArrayList<Entry> entries) {
		Debug.checkNull("dir", dir);
		Debug.checkNull("entries", entries);
		final File key = dir;
		final EntryData data = new EntryData();
		data.key = dir;
		data.value = entries;
		this.map.put(key, data);
	}

	public int size () {
		return this.map.size();
	}

	public File getKey (final int i) {
		return this.map.getValueAt(i).key;
	}

	public ArrayList<Entry> getValue (final int i) {
		return this.map.getValueAt(i).value;
	}

	

}
