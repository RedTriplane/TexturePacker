package com.badlogic.gdx.tools;

import java.util.ArrayList;

import com.jfixby.cmns.api.collections.Map;
import com.jfixby.cmns.api.debug.Debug;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.util.JUtils;

public class DirToEntries {
	class EntryData {

		@Override
		public String toString() {
			return value.toString();
		}

		public File key;
		public ArrayList<Entry> value;
	}

	Map<String, EntryData> map = JUtils.newMap();

	public ArrayList<Entry> get(File dir) {
		String key = dir.toJavaFile().getAbsolutePath();
		EntryData val = map.get(key);
		if (val == null) {
			return null;
		}
		return val.value;
	}

	public void put(File dir, ArrayList<Entry> entries) {
		Debug.checkNull("dir", dir);
		Debug.checkNull("entries", entries);
		String key = dir.toJavaFile().getAbsolutePath();
		EntryData data = new EntryData();
		data.key = dir;
		data.value = entries;
		map.put(key, data);
	}

	public int size() {
		return map.size();
	}

	public File getKey(int i) {
		return map.getValueAt(i).key;
	}

	public ArrayList<Entry> getValue(int i) {
		return map.getValueAt(i).value;
	}

	public void print() {
		this.map.print("dirToEntries");
	}

}
