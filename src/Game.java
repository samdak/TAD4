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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;

/******************************************************************************
  Main applet code.
******************************************************************************/

public class Game extends Applet implements Runnable, KeyListener{
    
    
  // Copyright information.

  String copyName = "Asteroids";
  String copyVers = "Version 1.3";
  String copyInfo = "Copyright 1998-2001 by Mike Hall";
  String copyLink = "http://www.brainjar.com";
  String copyText = copyName + '\n' + copyVers + '\n'
                  + copyInfo + '\n' + copyLink;

  // Thread control variables.

  Thread loadThread;
  Thread loopThread;

  // Background stars.

  int     numStars;
  Point[] stars;

  // Game data.

  static int score;
  int highScore;
  int newShipScore;
  int newUfoScore;

  // Flags for game state and options.

  static boolean loaded = false;
  static boolean paused;
  static boolean playing;
  static boolean sound;

  static Explosions explosions = new Explosions();
  static Photons photons = new Photons();
  static Asteroids asteroids = new Asteroids();
  static Ufo ufo = new Ufo();
  static Missle missle = new Missle();
  static Ship ship = new Ship();

  // Key flags.

  boolean left  = false;
  boolean right = false;
  boolean up    = false;
  boolean down  = false;

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

  // Counter and total used to track the loading of the sound clips.

  int clipTotal   = 0;
  int clipsLoaded = 0;

  // Off screen image.

  Dimension offDimension;
  Image     offImage;
  Graphics  offGraphics;

  // Data for the screen font.

  Font font      = new Font("Helvetica", Font.BOLD, 12);
  FontMetrics fm = getFontMetrics(font);
  int fontWidth  = fm.getMaxAdvance();
  int fontHeight = fm.getHeight();

  public String getAppletInfo() {

    // Return copyright information.

    return(copyText);
  }

  public void init() {

    Dimension d = getSize();
    int i;

    // Display copyright information.

    System.out.println(copyText);

    // Set up key event handling and set focus to applet window.

    addKeyListener(this);
    requestFocus();

    // Save the screen size.

    AsteroidsSprite.width = d.width;
    AsteroidsSprite.height = d.height;

    // Generate the starry background.

    numStars = AsteroidsSprite.width * AsteroidsSprite.height / 5000;
    stars = new Point[numStars];
    for (i = 0; i < numStars; i++)
      stars[i] = new Point((int) (Math.random() * AsteroidsSprite.width), (int) (Math.random() * AsteroidsSprite.height));

    // Create shape for the ship sprite.

    ship.ship = new AsteroidsSprite();
    ship.ship.shape.addPoint(0, -10);
    ship.ship.shape.addPoint(7, 10);
    ship.ship.shape.addPoint(-7, 10);

    // Create shapes for the ship thrusters.

    ship.fwdThruster = new AsteroidsSprite();
    ship.fwdThruster.shape.addPoint(0, 12);
    ship.fwdThruster.shape.addPoint(-3, 16);
    ship.fwdThruster.shape.addPoint(0, 26);
    ship.fwdThruster.shape.addPoint(3, 16);
    ship.revThruster = new AsteroidsSprite();
    ship.revThruster.shape.addPoint(-2, 12);
    ship.revThruster.shape.addPoint(-4, 14);
    ship.revThruster.shape.addPoint(-2, 20);
    ship.revThruster.shape.addPoint(0, 14);
    ship.revThruster.shape.addPoint(2, 12);
    ship.revThruster.shape.addPoint(4, 14);
    ship.revThruster.shape.addPoint(2, 20);
    ship.revThruster.shape.addPoint(0, 14);

    // Create shape for each photon sprites.

    for (i = 0; i < Variables.MAX_SHOTS; i++) {
      photons.photons[i] = new AsteroidsSprite();
      photons.photons[i].shape.addPoint(1, 1);
      photons.photons[i].shape.addPoint(1, -1);
      photons.photons[i].shape.addPoint(-1, 1);
      photons.photons[i].shape.addPoint(-1, -1);
    }

    // Create shape for the flying saucer.

    ufo.ufo = new AsteroidsSprite();
    ufo.ufo.shape.addPoint(-15, 0);
    ufo.ufo.shape.addPoint(-10, -5);
    ufo.ufo.shape.addPoint(-5, -5);
    ufo.ufo.shape.addPoint(-5, -8);
    ufo.ufo.shape.addPoint(5, -8);
    ufo.ufo.shape.addPoint(5, -5);
    ufo.ufo.shape.addPoint(10, -5);
    ufo.ufo.shape.addPoint(15, 0);
    ufo.ufo.shape.addPoint(10, 5);
    ufo.ufo.shape.addPoint(-10, 5);

    // Create shape for the guided missle.

    missle.missle = new AsteroidsSprite();
    missle.missle.shape.addPoint(0, -4);
    missle.missle.shape.addPoint(1, -3);
    missle.missle.shape.addPoint(1, 3);
    missle.missle.shape.addPoint(2, 4);
    missle.missle.shape.addPoint(-2, 4);
    missle.missle.shape.addPoint(-1, 3);
    missle.missle.shape.addPoint(-1, -3);

    // Create asteroid sprites.

    for (i = 0; i < Variables.MAX_ROCKS; i++)
      asteroids.asteroids[i] = new AsteroidsSprite();

    // Create explosion sprites.

    for (i = 0; i < Variables.MAX_SCRAP; i++)
      explosions.explosions[i] = new AsteroidsSprite();

    // Initialize game data and put us in 'game over' mode.

    highScore = 0;
    sound = true;
    explosions.detail = true;
    initGame();
    endGame();
  }

  public void initGame() {

    // Initialize game data and sprites.

    score = 0;
    ship.shipsLeft = Variables.MAX_SHIPS;
    asteroids.asteroidsSpeed = Variables.MIN_ROCK_SPEED;
    newShipScore = Variables.NEW_SHIP_POINTS;
    newUfoScore = Variables.NEW_UFO_POINTS;
    ship.initShip();
    photons.initPhotons();
    ufo.stopUfo();
    missle.stopMissle();
    asteroids.initAsteroids();
    explosions.initExplosions();
    playing = true;
    paused = false;
    photons.photonTime = System.currentTimeMillis();
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

        updateShip();
        photons.updatePhotons();
        ufo.updateUfo();
        missle.updateMissle();
        asteroids.updateAsteroids();
        explosions.updateExplosions();

        // Check the score and advance high score, add a new ship or start the
        // flying saucer as necessary.

        if (score > highScore)
          highScore = score;
        if (score > newShipScore) {
          newShipScore += Variables.NEW_SHIP_POINTS;
          ship.shipsLeft++;
        }
        if (playing && score > newUfoScore && !ufo.ufo.active) {
          newUfoScore += Variables.NEW_UFO_POINTS;
          ufo.ufoPassesLeft = Variables.UFO_PASSES;
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
        startTime += Variables.DELAY;
        Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
      }
      catch (InterruptedException e) {
        break;
      }
    }
  }

  public void loadSounds() {

    // Load all sound clips by playing and immediately stopping them. Update
    // counter and total for display.

    try {
      crashSound     = getAudioClip(new URL(getCodeBase(), "crash.au"));
      clipTotal++;
      explosionSound = getAudioClip(new URL(getCodeBase(), "explosion.au"));
      clipTotal++;
      fireSound      = getAudioClip(new URL(getCodeBase(), "fire.au"));
      clipTotal++;
      missleSound    = getAudioClip(new URL(getCodeBase(), "missle.au"));
      clipTotal++;
      saucerSound    = getAudioClip(new URL(getCodeBase(), "saucer.au"));
      clipTotal++;
      thrustersSound = getAudioClip(new URL(getCodeBase(), "thrusters.au"));
      clipTotal++;
      warpSound      = getAudioClip(new URL(getCodeBase(), "warp.au"));
      clipTotal++;
    }
    catch (MalformedURLException e) {}

    try {
      crashSound.play();     crashSound.stop();     clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
      explosionSound.play(); explosionSound.stop(); clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
      fireSound.play();      fireSound.stop();      clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
      missleSound.play();    missleSound.stop();    clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
      saucerSound.play();    saucerSound.stop();    clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
      thrustersSound.play(); thrustersSound.stop(); clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
      warpSound.play();      warpSound.stop();      clipsLoaded++;
      repaint(); Thread.currentThread().sleep(Variables.DELAY);
    }
    catch (InterruptedException e) {}
  }
  
  public void updateShip() {

    double dx, dy, speed;

    if (!playing)
      return;

    // Rotate the ship if left or right cursor key is down.

    if (left) {
      ship.ship.angle += Variables.SHIP_ANGLE_STEP;
      if (ship.ship.angle > 2 * Math.PI)
        ship.ship.angle -= 2 * Math.PI;
    }
    if (right) {
      ship.ship.angle -= Variables.SHIP_ANGLE_STEP;
      if (ship.ship.angle < 0)
        ship.ship.angle += 2 * Math.PI;
    }

    // Fire thrusters if up or down cursor key is down.

    dx = Variables.SHIP_SPEED_STEP * -Math.sin(ship.ship.angle);
    dy = Variables.SHIP_SPEED_STEP *  Math.cos(ship.ship.angle);
    if (up) {
      ship.ship.deltaX += dx;
      ship.ship.deltaY += dy;
    }
    if (down) {
        ship.ship.deltaX -= dx;
        ship.ship.deltaY -= dy;
    }

    // Don't let ship go past the speed limit.

    if (up || down) {
      speed = Math.sqrt(ship.ship.deltaX * ship.ship.deltaX + ship.ship.deltaY * ship.ship.deltaY);
      if (speed > Variables.MAX_SHIP_SPEED) {
        dx = Variables.MAX_SHIP_SPEED * -Math.sin(ship.ship.angle);
        dy = Variables.MAX_SHIP_SPEED *  Math.cos(ship.ship.angle);
        if (up)
          ship.ship.deltaX = dx;
        else
          ship.ship.deltaX = -dx;
        if (up)
          ship.ship.deltaY = dy;
        else
          ship.ship.deltaY = -dy;
      }
    }

    // Move the ship. If it is currently in hyperspace, advance the countdown.

    if (ship.ship.active) {
      ship.ship.advance();
      ship.ship.render();
      if (ship.hyperCounter > 0)
        ship.hyperCounter--;

      // Update the thruster sprites to match the ship sprite.

      ship.fwdThruster.x = ship.ship.x;
      ship.fwdThruster.y = ship.ship.y;
      ship.fwdThruster.angle = ship.ship.angle;
      ship.fwdThruster.render();
      ship.revThruster.x = ship.ship.x;
      ship.revThruster.y = ship.ship.y;
      ship.revThruster.angle = ship.ship.angle;
      ship.revThruster.render();
    }

    // Ship is exploding, advance the countdown or create a new ship if it is
    // done exploding. The new ship is added as though it were in hyperspace.
    // (This gives the player time to move the ship if it is in imminent
    // danger.) If that was the last ship, end the game.

    else
      if (--ship.shipCounter <= 0)
        if (ship.shipsLeft > 0) {
          ship.initShip();
          ship.hyperCounter = Variables.HYPER_COUNT;
        }
        else
          endGame();
  }

  public void update(Graphics g) {

    paint(g);
  }

  public void paint(Graphics g) {

    Dimension d = getSize();
    int i;
    int c;
    String s;
    int w, h;
    int x, y;

    // Create the off screen graphics context, if no good one exists.

    if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
    }

    // Fill in background and stars.

    offGraphics.setColor(Color.black);
    offGraphics.fillRect(0, 0, d.width, d.height);
    if (explosions.detail) {
      offGraphics.setColor(Color.white);
      for (i = 0; i < numStars; i++)
        offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
    }

    // Draw photon bullets.

    offGraphics.setColor(Color.white);
    for (i = 0; i < Variables.MAX_SHOTS; i++)
      if (photons.photons[i].active)
        offGraphics.drawPolygon(photons.photons[i].sprite);

    // Draw the guided missle, counter is used to quickly fade color to black
    // when near expiration.

    c = Math.min(missle.missleCounter * 24, 255);
    offGraphics.setColor(new Color(c, c, c));
    if (missle.missle.active) {
      offGraphics.drawPolygon(missle.missle.sprite);
      offGraphics.drawLine(missle.missle.sprite.xpoints[missle.missle.sprite.npoints - 1], missle.missle.sprite.ypoints[missle.missle.sprite.npoints - 1],
                           missle.missle.sprite.xpoints[0], missle.missle.sprite.ypoints[0]);
    }

    // Draw the asteroids.

    for (i = 0; i < Variables.MAX_ROCKS; i++)
      if (asteroids.asteroids[i].active) {
        if (explosions.detail) {
          offGraphics.setColor(Color.black);
          offGraphics.fillPolygon(asteroids.asteroids[i].sprite);
        }
        offGraphics.setColor(Color.white);
        offGraphics.drawPolygon(asteroids.asteroids[i].sprite);
        offGraphics.drawLine(asteroids.asteroids[i].sprite.xpoints[asteroids.asteroids[i].sprite.npoints - 1], asteroids.asteroids[i].sprite.ypoints[asteroids.asteroids[i].sprite.npoints - 1],
                             asteroids.asteroids[i].sprite.xpoints[0], asteroids.asteroids[i].sprite.ypoints[0]);
      }

    // Draw the flying saucer.

    if (ufo.ufo.active) {
      if (explosions.detail) {
        offGraphics.setColor(Color.black);
        offGraphics.fillPolygon(ufo.ufo.sprite);
      }
      offGraphics.setColor(Color.white);
      offGraphics.drawPolygon(ufo.ufo.sprite);
      offGraphics.drawLine(ufo.ufo.sprite.xpoints[ufo.ufo.sprite.npoints - 1], ufo.ufo.sprite.ypoints[ufo.ufo.sprite.npoints - 1],
                           ufo.ufo.sprite.xpoints[0], ufo.ufo.sprite.ypoints[0]);
    }

    // Draw the ship, counter is used to fade color to white on hyperspace.

    c = 255 - (255 / Variables.HYPER_COUNT) * ship.hyperCounter;
    if (ship.ship.active) {
      if (explosions.detail && ship.hyperCounter == 0) {
        offGraphics.setColor(Color.black);
        offGraphics.fillPolygon(ship.ship.sprite);
      }
      offGraphics.setColor(new Color(c, c, c));
      offGraphics.drawPolygon(ship.ship.sprite);
      offGraphics.drawLine(ship.ship.sprite.xpoints[ship.ship.sprite.npoints - 1], ship.ship.sprite.ypoints[ship.ship.sprite.npoints - 1],
                           ship.ship.sprite.xpoints[0], ship.ship.sprite.ypoints[0]);

      // Draw thruster exhaust if thrusters are on. Do it randomly to get a
      // flicker effect.

      if (!paused && explosions.detail && Math.random() < 0.5) {
        if (up) {
          offGraphics.drawPolygon(ship.fwdThruster.sprite);
          offGraphics.drawLine(ship.fwdThruster.sprite.xpoints[ship.fwdThruster.sprite.npoints - 1], ship.fwdThruster.sprite.ypoints[ship.fwdThruster.sprite.npoints - 1],
                               ship.fwdThruster.sprite.xpoints[0], ship.fwdThruster.sprite.ypoints[0]);
        }
        if (down) {
          offGraphics.drawPolygon(ship.revThruster.sprite);
          offGraphics.drawLine(ship.revThruster.sprite.xpoints[ship.revThruster.sprite.npoints - 1], ship.revThruster.sprite.ypoints[ship.revThruster.sprite.npoints - 1],
                               ship.revThruster.sprite.xpoints[0], ship.revThruster.sprite.ypoints[0]);
        }
      }
    }

    // Draw any explosion debris, counters are used to fade color to black.

    for (i = 0; i < Variables.MAX_SCRAP; i++)
      if (explosions.explosions[i].active) {
        c = (255 / Variables.SCRAP_COUNT) * explosions.explosionCounter [i];
        offGraphics.setColor(new Color(c, c, c));
        offGraphics.drawPolygon(explosions.explosions[i].sprite);
      }

    // Display status and messages.

    offGraphics.setFont(font);
    offGraphics.setColor(Color.white);

    offGraphics.drawString("Score: " + score, fontWidth, fontHeight);
    offGraphics.drawString("Ships: " + ship.shipsLeft, fontWidth, d.height - fontHeight);
    s = "High: " + highScore;
    offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
    if (!sound) {
      s = "Mute";
      offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
    }

    if (!playing) {
      s = copyName;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - 2 * fontHeight);
      s = copyVers;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - fontHeight);
      s = copyInfo;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
      s = copyLink;
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + 2 * fontHeight);
      if (!loaded) {
        s = "Loading sounds...";
        w = 4 * fontWidth + fm.stringWidth(s);
        h = fontHeight;
        x = (d.width - w) / 2;
        y = 3 * d.height / 4 - fm.getMaxAscent();
        offGraphics.setColor(Color.black);
          offGraphics.fillRect(x, y, w, h);
        offGraphics.setColor(Color.gray);
        if (clipTotal > 0)
          offGraphics.fillRect(x, y, (int) (w * clipsLoaded / clipTotal), h);
        offGraphics.setColor(Color.white);
        offGraphics.drawRect(x, y, w, h);
        offGraphics.drawString(s, x + 2 * fontWidth, y + fm.getMaxAscent());
      }
      else {
        s = "Game Over";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
        s = "'S' to Start";
        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
      }
    }
    else if (paused) {
      s = "Game Paused";
      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
    }

    // Copy the off screen buffer to the screen.

    g.drawImage(offImage, 0, 0, this);
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

    if ((up || down) && ship.ship.active && !thrustersPlaying) {
      if (sound && !paused)
        thrustersSound.loop();
      thrustersPlaying = true;
    }

    // Spacebar: fire a photon and start its counter.

    if (e.getKeyChar() == ' ' && ship.ship.active) {
      if (sound & !paused)
        fireSound.play();
      photons.photonTime = System.currentTimeMillis();
      photons.photonIndex++;
      if (photons.photonIndex >= Variables.MAX_SHOTS)
        photons.photonIndex = 0;
      photons.photons[photons.photonIndex].active = true;
      photons.photons[photons.photonIndex].x = ship.ship.x;
      photons.photons[photons.photonIndex].y = ship.ship.y;
      photons.photons[photons.photonIndex].deltaX = 2 * Variables.MAX_ROCK_SPEED * -Math.sin(ship.ship.angle);
      photons.photons[photons.photonIndex].deltaY = 2 * Variables.MAX_ROCK_SPEED *  Math.cos(ship.ship.angle);
    }

    // Allow upper or lower case characters for remaining keys.

    c = Character.toLowerCase(e.getKeyChar());

    // 'H' key: warp ship into hyperspace by moving to a random location and
    // starting counter.

    if (c == 'h' && ship.ship.active && ship.hyperCounter <= 0) {
      ship.ship.x = Math.random() * AsteroidsSprite.width;
      ship.ship.y = Math.random() * AsteroidsSprite.height;
      ship.hyperCounter = Variables.HYPER_COUNT;
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
      }
      else {
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
      }
      else {
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
      explosions.detail = !explosions.detail;

    // 'S' key: start the game, if not already in progress.

    if (c == 's' && loaded && !playing)
      initGame();

    // 'HOME' key: jump to web site (undocumented).

    if (e.getKeyCode() == KeyEvent.VK_HOME)
      try {
        getAppletContext().showDocument(new URL(copyLink));
      }
      catch (Exception excp) {}
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

  public void keyTyped(KeyEvent e) {}

}
