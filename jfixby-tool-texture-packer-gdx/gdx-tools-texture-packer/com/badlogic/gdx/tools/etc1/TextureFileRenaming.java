package com.badlogic.gdx.tools.etc1;

class TextureFileRenaming {

    private String oldPageFileName;
    private String newPageFileName;

    public TextureFileRenaming(String oldPageFileName, String newPageFileName) {
        this.oldPageFileName = oldPageFileName;
        this.newPageFileName = newPageFileName;
    }

    @Override
    public String toString() {
        return oldPageFileName + " :-> " + newPageFileName;
    }

}