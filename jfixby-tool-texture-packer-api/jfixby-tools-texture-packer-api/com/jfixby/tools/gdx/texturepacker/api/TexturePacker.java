package com.jfixby.tools.gdx.texturepacker.api;

import com.jfixby.scarabei.api.ComponentInstaller;

public class TexturePacker {

    static private ComponentInstaller<TexturePackerComponent> componentInstaller = new ComponentInstaller<TexturePackerComponent>(
	    "TexturePacker");

    public static final void installComponent(TexturePackerComponent component_to_install) {
	componentInstaller.installComponent(component_to_install);
    }

    public static final TexturePackerComponent invoke() {
	return componentInstaller.invokeComponent();
    }

    public static final TexturePackerComponent component() {
	return componentInstaller.getComponent();
    }

    public static TexturePackingSpecs newPackingSpecs() {
	return invoke().newPackingSpecs();
    }

    public static Packer newPacker(TexturePackingSpecs packing_specs) {
	return invoke().newPacker(packing_specs);
    }
}
