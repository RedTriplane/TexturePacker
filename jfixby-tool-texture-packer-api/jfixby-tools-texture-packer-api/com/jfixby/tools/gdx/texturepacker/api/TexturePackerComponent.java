package com.jfixby.tools.gdx.texturepacker.api;

public interface TexturePackerComponent {

	TexturePackingSpecs newPackingSpecs();

	Packer newPacker(TexturePackingSpecs packing_specs);

}
