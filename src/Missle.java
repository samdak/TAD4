
public class Missle extends AsteroidsSprite {

	AsteroidsSprite missle = new AsteroidsSprite();

	// Missle data.

	int missleCounter; // Counter for life of missle.

	public void initMissle() {

		missle.active = true;
		missle.angle = 0.0;
		missle.deltaAngle = 0.0;
		missle.x = Game.ufo.x;
		missle.y = Game.ufo.y;
		missle.deltaX = 0.0;
		missle.deltaY = 0.0;
		missle.render();
		missleCounter = Game.MISSLE_COUNT;
		if (Game.sound)
			Game.missleSound.loop();
		Game.misslePlaying = true;
	}

	public void updateMissle() {

		int i;

		// Move the guided missle and check for collision with ship or photon.
		// Stop
		// it when its counter has expired.

		if (missle.active) {
			if (--missleCounter <= 0)
				stopMissle();
			else {
				guideMissle();
				missle.advance();
				missle.render();
				for (i = 0; i < Game.MAX_SHOTS; i++)
					if (Game.photon.photons[i].active && missle.isColliding(Game.photon.photons[i])) {
						if (Game.sound)
							Game.crashSound.play();
						Game.explosion.explode(missle);
						stopMissle();
						Game.score += Game.MISSLE_POINTS;
					}
				if (missle.active && Game.ship.active && Game.ship.hyperCounter <= 0 && Game.ship.isColliding(missle)) {
					if (Game.sound)
						Game.crashSound.play();
					Game.explosion.explode(Game.ship);
					Game.ship.stopShip();
					Game.ufo.stopUfo();
					stopMissle();
				}
			}
		}
	}

	public void guideMissle() {

		double dx, dy, angle;

		if (!Game.ship.active || Game.ship.hyperCounter > 0)
			return;

		// Find the angle needed to hit the ship.

		dx = Game.ship.x - missle.x;
		dy = Game.ship.y - missle.y;
		if (dx == 0 && dy == 0)
			angle = 0;
		if (dx == 0) {
			if (dy < 0)
				angle = -Math.PI / 2;
			else
				angle = Math.PI / 2;
		} else {
			angle = Math.atan(Math.abs(dy / dx));
			if (dy > 0)
				angle = -angle;
			if (dx < 0)
				angle = Math.PI - angle;
		}

		// Adjust angle for screen coordinates.

		missle.angle = angle - Math.PI / 2;

		// Change the missle's angle so that it points toward the ship.

		missle.deltaX = 0.75 * Game.MAX_ROCK_SPEED * -Math.sin(missle.angle);
		missle.deltaY = 0.75 * Game.MAX_ROCK_SPEED * Math.cos(missle.angle);
	}

	public void stopMissle() {

		missle.active = false;
		missleCounter = 0;
		if (Game.loaded)
			Game.missleSound.stop();
		Game.misslePlaying = false;
	}

}
