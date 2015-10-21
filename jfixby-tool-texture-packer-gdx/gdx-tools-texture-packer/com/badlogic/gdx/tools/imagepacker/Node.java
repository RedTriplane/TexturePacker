package com.badlogic.gdx.tools.imagepacker;

import java.awt.Rectangle;

final class Node {
	public Node leftChild;
	public Node rightChild;
	public Rectangle rect;
	public String leaveName;

	public Node (int x, int y, int width, int height, Node leftChild, Node rightChild, String leaveName) {
		this.rect = new Rectangle(x, y, width, height);
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.leaveName = leaveName;
	}

	public Node () {
		rect = new Rectangle();
	}
}