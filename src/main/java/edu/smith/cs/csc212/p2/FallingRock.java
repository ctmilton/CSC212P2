package edu.smith.cs.csc212.p2;

/**
 * An extension of the rock class
 * @author carol milton
 *
 */

public class FallingRock extends Rock {

	public FallingRock(World world) {
		super(world);

	}
	
	@Override
	public void step() {
		this.moveDown();
	}
	
}
