package com.jfixby.tools.gdx.texturepacker;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.log.L;

public class TempPngNamesKeeper {
	long index = -1;

	Map<String, String> memories = Collections.newMap();

	public String newTempName() {
		index++;
		return "#temp-" + index + "#";
	}

	public void remember(String temporary_name, String original_name) {
		L.d("   renaming", original_name + " -> " + temporary_name);
		memories.put(temporary_name, original_name);
	}

	public int size() {
		return this.memories.size();
	}

	public String getTemporaryName(int i) {
		return this.memories.getKeyAt(i);
	}

	public String getOriginalName(String tmp_name) {
		return this.memories.get(tmp_name);
	}

}
