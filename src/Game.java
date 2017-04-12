
/******************************************************************************
  Asteroids, Version 1.3

  Copyright 1998-2001 by Mike Hall.
  Please see http://www.brainjar.com for terms of use.

  Revision History:

  1.01, 12/18/1999: Increased number of active photons allowed.
                    Improved explosions for more realism.
                    Added progress bar for loading of sound clips.
  1.2,  12/23/1999: Increased frame rate for smoother animation.
                    Modified code to calculate game object speeds and timer
                    counters based on the frame rate so they will remain
                    constant.
                    Improved speed limit checking for ship.
                    Removed wrapping of photons around screen and set a fixed
                    firing rate.
                    Added sprites for ship's thrusters.
  1.3,  01/25/2001: Updated to JDK 1.1.8.

  Usage:

  <applet code="Asteroids.class" width=w height=h></applet>

  Keyboard Controls:

  S            - Start Game    P           - Pause Game
  Cursor Left  - Rotate Left   Cursor Up   - Fire Thrusters
  Cursor Right - Rotate Right  Cursor Down - Fire Retro Thrusters
  Spacebar     - Fire Cannon   H           - Hyperspace
  M            - Toggle Sound  D           - Toggle Graphics Detail

******************************************************************************/

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;

public class Game extends Applet implements Runnable, KeyListener {

	// Constants

	static final int DELAY = 20; // Milliseconds between screen and
	static final int FPS = // the resulting frame rate.
			Math.round(1000 / DELAY);

	static final int MAX_SHOTS = 8; // Maximum number of sprites
	static final int MAX_ROCKS = 8; // for photons, asteroids and
	static final int MAX_SCRAP = 40; // explosions.

	static final int SCRAP_COUNT = 2 * FPS; // Timer counter starting values
	static final int HYPER_COUNT = 3 * FPS; // calculated using number of
	static final int MISSLE_COUNT = 4 * FPS; // seconds x frames per second.
	static final int STORM_PAUSE = 2 * FPS;

	static final int MIN_ROCK_SIDES = 6; // Ranges for asteroid shape, size
	static final int MAX_ROCK_SIDES = 16; // speed and rotation.
	static final int MIN_ROCK_SIZE = 20;
	static final int MAX_ROCK_SIZE = 40;
	static final double MIN_ROCK_SPEED = 40.0 / FPS;
	static final double MAX_ROCK_SPEED = 240.0 / FPS;
	static final double MAX_ROCK_SPIN = Math.PI / FPS;

	static final int MAX_SHIPS = 3; // Starting number of ships for
									// each game.
	static final int UFO_PASSES = 3; // Number of passes for flying
										// saucer per appearance.

	// Ship's rotation and acceleration rates and maximum speed.

	static final double SHIP_ANGLE_STEP = Math.PI / FPS;
	static final double SHIP_SPEED_STEP = 15.0 / FPS;
	static final double MAX_SHIP_SPEED = 1.25 * MAX_ROCK_SPEED;

	static final int FIRE_DELAY = 50; // Minimum number of milliseconds
										// required between photon shots.

	// Probablility of flying saucer firing a missle during any given frame
	// (other conditions must be met).

	static final double MISSLE_PROBABILITY = 0.45 / FPS;

	static final int BIG_POINTS = 25; // Points scored for shooting
	static final int SMALL_POINTS = 50; // various objects.
	static final int UFO_POINTS = 250;
	static final int MISSLE_POINTS = 500;

	// Number of points the must be scored to earn a new ship or to cause the
	// flying saucer to appear.

	static final int NEW_SHIP_POINTS = 5000;
	static final int NEW_UFO_POINTS = 2750;

	// Flags for game state and options.

	static boolean loaded = false;
	boolean paused;
	static boolean playing;

	boolean detail;

	String copyLink = "http://www.brainjar.com";

	// Key flags.

	boolean left = false;
	boolean right = false;
	boolean up = false;
	boolean down = false;

	// Game data.

	static int score;
	int highScore;
	int newShipScore;
	int newUfoScore;

	// Counter and total used to track the loading of the sound clips.

	int clipTotal = 0;
	int clipsLoaded = 0;

	static boolean sound;

	// Sound clips.

	static AudioClip crashSound;
	static AudioClip explosionSound;
	static AudioClip fireSound;
	static AudioClip missleSound;
	static AudioClip saucerSound;
	static AudioClip thrustersSound;
	static AudioClip warpSound;

	// Flags for looping sound clips.

	static boolean thrustersPlaying;
	static boolean saucerPlaying;
	static boolean misslePlaying;

	// Thread control variables.

	Thread loadThread;
	Thread loopThread;

	static Explosion explosion = new Explosion();
	static Photon photon = new Photon();
	static Asteroids asteroids = new Asteroids();
	// static Sound sound = new Sound();
	static Ufo ufo = new Ufo();
	static Missle missle = new Missle();
	static Ship ship = new Ship();
	static Ship fwdThruster = new Ship();
	static Ship revThruster = new Ship();

	public void loadSounds() {

		// Load all sound clips by playing and immediately stopping them. Update
		// counter and total for display.

		try {
			crashSound = getAudioClip(new URL(getCodeBase(), "crash.au"));
			clipTotal++;
			explosionSound = getAudioClip(new URL(getCodeBase(), "explosion.au"));
			clipTotal++;
			fireSound = getAudioClip(new URL(getCodeBase(), "fire.au"));
			clipTotal++;
			missleSound = getAudioClip(new URL(getCodeBase(), "missle.au"));
			clipTotal++;
			saucerSound = getAudioClip(new URL(getCodeBase(), "saucer.au"));
			clipTotal++;
			thrustersSound = getAudioClip(new URL(getCodeBase(), "thrusters.au"));
			clipTotal++;
			warpSound = getAudioClip(new URL(getCodeBase(), "warp.au"));
			clipTotal++;
		} catch (MalformedURLException e) {
		}

		try {
			crashSound.play();
			crashSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			explosionSound.play();
			explosionSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			fireSound.play();
			fireSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			missleSound.play();
			missleSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			saucerSound.play();
			saucerSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			thrustersSound.play();
			thrustersSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			warpSound.play();
			warpSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
		} catch (InterruptedException e) {
		}
	}

	public void initGame() {

		// Initialize game data and sprites.

		score = 0;
		ship.shipsLeft = MAX_SHIPS;
		asteroids.asteroidsSpeed = MIN_ROCK_SPEED;
		newShipScore = NEW_SHIP_POINTS;
		newUfoScore = NEW_UFO_POINTS;
		ship.initShip();
		photon.initPhotons();
		ufo.stopUfo();
		missle.stopMissle();
		asteroids.initAsteroids();
		explosion.initExplosions();
		playing = true;
		paused = false;
		photon.photonTime = System.currentTimeMillis();
	}

	public static void endGame() {

		// Stop ship, flying saucer, guided missle and associated sounds.

		playing = false;
		ship.stopShip();
		ufo.stopUfo();
		missle.stopMissle();
	}

	public void start() {

		if (loopThread == null) {
			loopThread = new Thread(this);
			loopThread.start();
		}
		if (!loaded && loadThread == null) {
			loadThread = new Thread(this);
			loadThread.start();
		}
	}

	public void stop() {

		if (loopThread != null) {
			loopThread.stop();
			loopThread = null;
		}
		if (loadThread != null) {
			loadThread.stop();
			loadThread = null;
		}
	}

	public void run() {

		int i, j;
		long startTime;

		// Lower this thread's priority and get the current time.

		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		startTime = System.currentTimeMillis();

		// Run thread for loading sounds.

		if (!loaded && Thread.currentThread() == loadThread) {
			loadSounds();
			loaded = true;
			loadThread.stop();
		}

		// This is the main loop.

		while (Thread.currentThread() == loopThread) {

			if (!paused) {

				// Move and process all sprites.

				ship.updateShip();
				photon.updatePhotons();
				ufo.updateUfo();
				missle.updateMissle();
				asteroids.updateAsteroids();
				explosion.updateExplosions();

				// Check the score and advance high score, add a new ship or
				// start the
				// flying saucer as necessary.

				if (score > highScore)
					highScore = score;
				if (score > newShipScore) {
					newShipScore += NEW_SHIP_POINTS;
					ship.shipsLeft++;
				}
				if (playing && score > newUfoScore && !ufo.active) {
					newUfoScore += NEW_UFO_POINTS;
					ufo.ufoPassesLeft = UFO_PASSES;
					ufo.initUfo();
				}

				// If all asteroids have been destroyed create a new batch.

				if (asteroids.asteroidsLeft <= 0)
					if (--asteroids.asteroidsCounter <= 0)
						asteroids.initAsteroids();
			}

			// Update the screen and set the timer for the next loop.

			repaint();
			try {
				startTime += DELAY;
				Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void keyPressed(KeyEvent e) {

		char c;

		// Check if any cursor keys have been pressed and set flags.

		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			left = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			right = true;
		if (e.getKeyCode() == KeyEvent.VK_UP)
			up = true;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			down = true;

		if ((up || down) && ship.active && !thrustersPlaying) {
			if (sound && !paused)
				thrustersSound.loop();
			thrustersPlaying = true;
		}

		// Spacebar: fire a photon and start its counter.

		if (e.getKeyChar() == ' ' && ship.active) {
			if (sound & !paused)
				fireSound.play();
			photon.photonTime = System.currentTimeMillis();
			photon.photonIndex++;
			if (photon.photonIndex >= MAX_SHOTS)
				photon.photonIndex = 0;
			photon.photons[photon.photonIndex].active = true;
			photon.photons[photon.photonIndex].x = ship.x;
			photon.photons[photon.photonIndex].y = ship.y;
			photon.photons[photon.photonIndex].deltaX = 2 * MAX_ROCK_SPEED * -Math.sin(ship.angle);
			photon.photons[photon.photonIndex].deltaY = 2 * MAX_ROCK_SPEED * Math.cos(ship.angle);
		}

		// Allow upper or lower case characters for remaining keys.

		c = Character.toLowerCase(e.getKeyChar());

		// 'H' key: warp ship into hyperspace by moving to a random location and
		// starting counter.

		if (c == 'h' && ship.active && ship.hyperCounter <= 0) {
			ship.x = Math.random() * AsteroidsSprite.width;
			ship.y = Math.random() * AsteroidsSprite.height;
			ship.hyperCounter = HYPER_COUNT;
			if (sound & !paused)
				warpSound.play();
		}

		// 'P' key: toggle pause mode and start or stop any active looping sound
		// clips.

		if (c == 'p') {
			if (paused) {
				if (sound && misslePlaying)
					missleSound.loop();
				if (sound && saucerPlaying)
					saucerSound.loop();
				if (sound && thrustersPlaying)
					thrustersSound.loop();
			} else {
				if (misslePlaying)
					missleSound.stop();
				if (saucerPlaying)
					saucerSound.stop();
				if (thrustersPlaying)
					thrustersSound.stop();
			}
			paused = !paused;
		}

		// 'M' key: toggle sound on or off and stop any looping sound clips.

		if (c == 'm' && loaded) {
			if (sound) {
				crashSound.stop();
				explosionSound.stop();
				fireSound.stop();
				missleSound.stop();
				saucerSound.stop();
				thrustersSound.stop();
				warpSound.stop();
			} else {
				if (misslePlaying && !paused)
					missleSound.loop();
				if (saucerPlaying && !paused)
					saucerSound.loop();
				if (thrustersPlaying && !paused)
					thrustersSound.loop();
			}
			sound = !sound;
		}

		// 'D' key: toggle graphics detail on or off.

		if (c == 'd')
			detail = !detail;

		// 'S' key: start the game, if not already in progress.

		if (c == 's' && loaded && !playing)
			initGame();

		// 'HOME' key: jump to web site (undocumented).

		if (e.getKeyCode() == KeyEvent.VK_HOME)
			try {
				getAppletContext().showDocument(new URL(copyLink));
			} catch (Exception excp) {
			}
	}

	public void keyReleased(KeyEvent e) {

		// Check if any cursor keys where released and set flags.

		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			left = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			right = false;
		if (e.getKeyCode() == KeyEvent.VK_UP)
			up = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			down = false;

		if (!up && !down && thrustersPlaying) {
			thrustersSound.stop();
			thrustersPlaying = false;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

}
