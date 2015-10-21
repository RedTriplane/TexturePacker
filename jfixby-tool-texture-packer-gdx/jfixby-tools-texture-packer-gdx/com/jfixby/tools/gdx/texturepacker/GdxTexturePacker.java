package com.jfixby.tools.gdx.texturepacker;

import com.jfixby.tools.gdx.texturepacker.api.Packer;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackerComponent;
import com.jfixby.tools.gdx.texturepacker.api.TexturePackingSpecs;

public class GdxTexturePacker implements TexturePackerComponent {

	@Override
	public TexturePackingSpecs newPackingSpecs() {
		return new RedTexturePackingSpecs();
	}

	@Override
	public Packer newPacker(TexturePackingSpecs packing_specs) {
		return new RedTexturePacker(packing_specs);
	}

}
