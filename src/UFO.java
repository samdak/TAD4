
public class Ufo extends AsteroidsSprite {

	AsteroidsSprite ufo = new AsteroidsSprite();
	// Flying saucer data.

	int ufoPassesLeft; // Counter for number of flying saucer passes.
	int ufoCounter; // Timer counter used to track each flying saucer pass.

	public Ufo() {
		// Create shape for the flying saucer.

		ufo = new AsteroidsSprite();
		ufo.shape.addPoint(-15, 0);
		ufo.shape.addPoint(-10, -5);
		ufo.shape.addPoint(-5, -5);
		ufo.shape.addPoint(-5, -8);
		ufo.shape.addPoint(5, -8);
		ufo.shape.addPoint(5, -5);
		ufo.shape.addPoint(10, -5);
		ufo.shape.addPoint(15, 0);
		ufo.shape.addPoint(10, 5);
		ufo.shape.addPoint(-10, 5);
	}

	public void initUfo() {

		double angle, speed;

		// Randomly set flying saucer at left or right edge of the screen.

		ufo.active = true;
		ufo.x = -AsteroidsSprite.width / 2;
		ufo.y = Math.random() * 2 * AsteroidsSprite.height - AsteroidsSprite.height;
		angle = Math.random() * Math.PI / 4 - Math.PI / 2;
		speed = Game.MAX_ROCK_SPEED / 2 + Math.random() * (Game.MAX_ROCK_SPEED / 2);
		ufo.deltaX = speed * -Math.sin(angle);
		ufo.deltaY = speed * Math.cos(angle);
		if (Math.random() < 0.5) {
			ufo.x = AsteroidsSprite.width / 2;
			ufo.deltaX = -ufo.deltaX;
		}
		if (ufo.y > 0)
			ufo.deltaY = ufo.deltaY;
		ufo.render();
		Game.saucerPlaying = true;
		if (Game.sound)
			Game.saucerSound.loop();
		ufoCounter = (int) Math.abs(AsteroidsSprite.width / ufo.deltaX);
	}

	public void updateUfo() {

		int i, d;
		boolean wrapped;

		// Move the flying saucer and check for collision with a photon. Stop it
		// when its counter has expired.

		if (ufo.active) {
			if (--ufoCounter <= 0) {
				if (--ufoPassesLeft > 0)
					initUfo();
				else
					stopUfo();
			}
			if (ufo.active) {
				ufo.advance();
				ufo.render();
				for (i = 0; i < Game.MAX_SHOTS; i++)
					if (Game.photon.photons[i].active && ufo.isColliding(Game.photon.photons[i])) {
						if (Game.sound)
							Game.crashSound.play();
						Game.explosion.explode(ufo);
						stopUfo();
						Game.score += Game.UFO_POINTS;
					}

				// On occassion, fire a missle at the ship if the saucer is not
				// too
				// close to it.

				d = (int) Math.max(Math.abs(ufo.x - Game.ship.x), Math.abs(ufo.y - Game.ship.y));
				if (Game.ship.active && Game.ship.hyperCounter <= 0 && ufo.active && !Game.missle.active
						&& d > Game.MAX_ROCK_SPEED * Game.FPS / 2 && Math.random() < Game.MISSLE_PROBABILITY)
					Game.missle.initMissle();
			}
		}
	}

	public void stopUfo() {

		ufo.active = false;
		ufoCounter = 0;
		ufoPassesLeft = 0;
		if (Game.loaded)
			Game.saucerSound.stop();
		Game.saucerPlaying = false;
	}

}
