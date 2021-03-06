package edu.smith.cs.csc212.p2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	/**
	 * These are fish that have returned home!
	 */
	List<Fish> atHome;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		atHome = new ArrayList<Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		// The number of rocks in the game is declared by this variable
		final int NUM_ROCKS = 10;
		for (int i=0; i<NUM_ROCKS; i++) {
			world.insertRockRandomly();
		}
		
		final int NUM_FALLING_ROCKS = 3;
		for (int i=0; i<NUM_FALLING_ROCKS; i++) {
			world.insertFallingRockRandomly();
		}
		
		world.insertSnailRandomly();
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the PlayFish app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		if (atHome.size() == 7) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				
				// Setting f to be the Fish object wo
				Fish f = (Fish) wo;
				
				// Remove this fish from the missing list.
				missing.remove(f);
				
				// Add this fish to the found list.
				found.add(f);
				
				// Increase score when you find a fish!
				if (f.isFastScared == true) {
					score += 15;
				} else {
					score += 10;
				}
			}
		}
		
		// If the player bring followers back home, put all of the found followers fish in the atHome list
		if (this.player.getX() == this.home.getX() && this.player.getY() == this.home.getY()) {
			for (Fish f : found) {
				atHome.add(f);
				world.remove(f);
			}
			found.clear();
			stepsTaken = 0;
		}
		
		// If more than 20 steps are taken, the last fish will wander away
		if (this.stepsTaken > 20) {
			if (!found.isEmpty() && found.size() > 1) {
				int lastFish = found.size() - 1;
				Fish loss = found.get(lastFish);
				missing.add(loss);
				found.remove(loss);
			}
		}
		
		// If any wandering fish gets home, it is stuck there!
		for (Fish lost : missing) {
			if (lost.getX() == this.home.getX() && lost.getY() == this.home.getY()) {
				atHome.add(lost);
				missing.remove(lost);
			}
		}
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			double scaredPercent = 0.1;
			if (rand.nextDouble() < scaredPercent) {
				lost.isFastScared = true;
			}
			
			double p = 0.3;
			if (lost.isFastScared) {
				p = 0.8;
			}
			// p percent of the time, lost fish move randomly.
			if (rand.nextDouble() < p) {
				lost.moveRandomly();
			
			}
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// Prints to the console whether the player Fish can swim through the WorldObject or not
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		
		// Allows the user to click and remove rocks.
		for (WorldObject w: atPoint) {
			if (world.canSwim(player, x, y) == false) {
				w.remove();
			}
		}
	}
}
