import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

public class MainGame extends Game {
		
	// Copyright information.

	String copyName = "Asteroids";
	String copyVers = "Version 1.3";
	String copyInfo = "Copyright 1998-2001 by Mike Hall";
	String copyLink = "http://www.brainjar.com";
	String copyText = copyName + '\n' + copyVers + '\n' + copyInfo + '\n' + copyLink;



	// Off screen image.

	Dimension offDimension;
	Image offImage;
	Graphics offGraphics;

	// Data for the screen font.

	Font font = new Font("Helvetica", Font.BOLD, 12);
	FontMetrics fm = getFontMetrics(font);
	int fontWidth = fm.getMaxAdvance();
	int fontHeight = fm.getHeight();



	// Background stars.

	int numStars;
	Point[] stars;
	
	public String getAppletInfo() {

		// Return copyright information.

		return (copyText);
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
			stars[i] = new Point((int) (Math.random() * AsteroidsSprite.width),
					(int) (Math.random() * AsteroidsSprite.height));



		

		// Create shape for each photon sprites.

		for (i = 0; i < MAX_SHOTS; i++) {
			photon.photons[i] = new AsteroidsSprite();
			photon.photons[i].shape.addPoint(1, 1);
			photon.photons[i].shape.addPoint(1, -1);
			photon.photons[i].shape.addPoint(-1, 1);
			photon.photons[i].shape.addPoint(-1, -1);
		}

		

		

		

		// Create asteroid sprites.

		for (i = 0; i < MAX_ROCKS; i++)
			asteroids.asteroids[i] = new AsteroidsSprite();

		// Create explosion sprites.

		for (i = 0; i < MAX_SCRAP; i++)
			explosion.explosions[i] = new AsteroidsSprite();

		// Initialize game data and put us in 'game over' mode.

		highScore = 0;
		sound = true;
		detail = true;
		initGame();
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
		if (detail) {
			offGraphics.setColor(Color.white);
			for (i = 0; i < numStars; i++)
				offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
		}

		// Draw photon bullets.

		offGraphics.setColor(Color.white);
		for (i = 0; i < MAX_SHOTS; i++)
			if (photon.photons[i].active)
				offGraphics.drawPolygon(photon.photons[i].sprite);

		// Draw the guided missle, counter is used to quickly fade color to
		// black
		// when near expiration.

		c = Math.min(missle.missleCounter * 24, 255);
		offGraphics.setColor(new Color(c, c, c));
		if (missle.active) {
			offGraphics.drawPolygon(missle.sprite);
			offGraphics.drawLine(missle.sprite.xpoints[missle.sprite.npoints - 1],
					missle.sprite.ypoints[missle.sprite.npoints - 1], missle.sprite.xpoints[0],
					missle.sprite.ypoints[0]);
		}

		// Draw the asteroids.

		for (i = 0; i < MAX_ROCKS; i++)
			if (asteroids.asteroids[i].active) {
				if (detail) {
					offGraphics.setColor(Color.black);
					offGraphics.fillPolygon(asteroids.asteroids[i].sprite);
				}
				offGraphics.setColor(Color.white);
				offGraphics.drawPolygon(asteroids.asteroids[i].sprite);
				offGraphics.drawLine(asteroids.asteroids[i].sprite.xpoints[asteroids.asteroids[i].sprite.npoints - 1],
						asteroids.asteroids[i].sprite.ypoints[asteroids.asteroids[i].sprite.npoints - 1],
						asteroids.asteroids[i].sprite.xpoints[0], asteroids.asteroids[i].sprite.ypoints[0]);
			}

		// Draw the flying saucer.

		if (ufo.active) {
			if (detail) {
				offGraphics.setColor(Color.black);
				offGraphics.fillPolygon(ufo.sprite);
			}
			offGraphics.setColor(Color.white);
			offGraphics.drawPolygon(ufo.sprite);
			offGraphics.drawLine(ufo.sprite.xpoints[ufo.sprite.npoints - 1], ufo.sprite.ypoints[ufo.sprite.npoints - 1],
					ufo.sprite.xpoints[0], ufo.sprite.ypoints[0]);
		}

		// Draw the ship, counter is used to fade color to white on hyperspace.

		c = 255 - (255 / HYPER_COUNT) * ship.hyperCounter;
		if (ship.active) {
			if (detail && ship.hyperCounter == 0) {
				offGraphics.setColor(Color.black);
				offGraphics.fillPolygon(ship.sprite);
			}
			offGraphics.setColor(new Color(c, c, c));
			offGraphics.drawPolygon(ship.sprite);
			offGraphics.drawLine(ship.sprite.xpoints[ship.sprite.npoints - 1],
					ship.sprite.ypoints[ship.sprite.npoints - 1], ship.sprite.xpoints[0], ship.sprite.ypoints[0]);

			// Draw thruster exhaust if thrusters are on. Do it randomly to get
			// a
			// flicker effect.

			if (!paused && detail && Math.random() < 0.5) {
				if (up) {
					offGraphics.drawPolygon(fwdThruster.sprite);
					offGraphics.drawLine(fwdThruster.sprite.xpoints[fwdThruster.sprite.npoints - 1],
							fwdThruster.sprite.ypoints[fwdThruster.sprite.npoints - 1], fwdThruster.sprite.xpoints[0],
							fwdThruster.sprite.ypoints[0]);
				}
				if (down) {
					offGraphics.drawPolygon(revThruster.sprite);
					offGraphics.drawLine(revThruster.sprite.xpoints[revThruster.sprite.npoints - 1],
							revThruster.sprite.ypoints[revThruster.sprite.npoints - 1], revThruster.sprite.xpoints[0],
							revThruster.sprite.ypoints[0]);
				}
			}
		}

		// Draw any explosion debris, counters are used to fade color to black.

		for (i = 0; i < MAX_SCRAP; i++)
			if (explosion.explosions[i].active) {
				c = (255 / SCRAP_COUNT) * explosion.explosionCounter[i];
				offGraphics.setColor(new Color(c, c, c));
				offGraphics.drawPolygon(explosion.explosions[i].sprite);
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
			} else {
				s = "Game Over";
				offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
				s = "'S' to Start";
				offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
			}
		} else if (paused) {
			s = "Game Paused";
			offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
		}

		// Copy the off screen buffer to the screen.

		g.drawImage(offImage, 0, 0, this);
	}


}
