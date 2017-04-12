
public class Ship extends AsteroidsSprite {

	// Ship data.

	int shipsLeft; // Number of ships left in game, including current one.
	int shipCounter; // Timer counter for ship explosion.
	int hyperCounter; // Timer counter for hyperspace.

	AsteroidsSprite ship;
	AsteroidsSprite fwdThruster, revThruster;

	// Flags for game state and options.

	boolean loaded = false;
	boolean paused;
	boolean playing;

	// Key flags.

	boolean left = false;
	boolean right = false;
	boolean up = false;
	boolean down = false;

	public Ship() {
		// Create shape for the ship sprite.

		ship = new AsteroidsSprite();
		ship.shape.addPoint(0, -10);
		ship.shape.addPoint(7, 10);
		ship.shape.addPoint(-7, 10);

		// Create shapes for the ship thrusters.

		fwdThruster = new AsteroidsSprite();
		fwdThruster.shape.addPoint(0, 12);
		fwdThruster.shape.addPoint(-3, 16);
		fwdThruster.shape.addPoint(0, 26);
		fwdThruster.shape.addPoint(3, 16);
		revThruster = new AsteroidsSprite();
		revThruster.shape.addPoint(-2, 12);
		revThruster.shape.addPoint(-4, 14);
		revThruster.shape.addPoint(-2, 20);
		revThruster.shape.addPoint(0, 14);
		revThruster.shape.addPoint(2, 12);
		revThruster.shape.addPoint(4, 14);
		revThruster.shape.addPoint(2, 20);
		revThruster.shape.addPoint(0, 14);
	}

	public void initShip() {

		// Reset the ship sprite at the center of the screen.

		ship.active = true;
		ship.angle = 0.0;
		ship.deltaAngle = 0.0;
		ship.x = 0.0;
		ship.y = 0.0;
		ship.deltaX = 0.0;
		ship.deltaY = 0.0;
		ship.render();

		// Initialize thruster sprites.

		fwdThruster.x = ship.x;
		fwdThruster.y = ship.y;
		fwdThruster.angle = ship.angle;
		fwdThruster.render();
		revThruster.x = ship.x;
		revThruster.y = ship.y;
		revThruster.angle = ship.angle;
		revThruster.render();

		if (loaded)
			Game.thrustersSound.stop();
		Game.thrustersPlaying = false;
		hyperCounter = 0;
	}

	public void updateShip() {

		double dx, dy, speed;

		if (!playing)
			return;

		// Rotate the ship if left or right cursor key is down.

		if (left) {
			ship.angle += Game.SHIP_ANGLE_STEP;
			if (ship.angle > 2 * Math.PI)
				ship.angle -= 2 * Math.PI;
		}
		if (right) {
			ship.angle -= Game.SHIP_ANGLE_STEP;
			if (ship.angle < 0)
				ship.angle += 2 * Math.PI;
		}

		// Fire thrusters if up or down cursor key is down.

		dx = Game.SHIP_SPEED_STEP * -Math.sin(ship.angle);
		dy = Game.SHIP_SPEED_STEP * Math.cos(ship.angle);
		if (up) {
			ship.deltaX += dx;
			ship.deltaY += dy;
		}
		if (down) {
			ship.deltaX -= dx;
			ship.deltaY -= dy;
		}

		// Don't let ship go past the speed limit.

		if (up || down) {
			speed = Math.sqrt(ship.deltaX * ship.deltaX + ship.deltaY * ship.deltaY);
			if (speed > Game.MAX_SHIP_SPEED) {
				dx = Game.MAX_SHIP_SPEED * -Math.sin(ship.angle);
				dy = Game.MAX_SHIP_SPEED * Math.cos(ship.angle);
				if (up)
					ship.deltaX = dx;
				else
					ship.deltaX = -dx;
				if (up)
					ship.deltaY = dy;
				else
					ship.deltaY = -dy;
			}
		}

		// Move the ship. If it is currently in hyperspace, advance the
		// countdown.

		if (ship.active) {
			ship.advance();
			ship.render();
			if (hyperCounter > 0)
				hyperCounter--;

			// Update the thruster sprites to match the ship sprite.

			fwdThruster.x = ship.x;
			fwdThruster.y = ship.y;
			fwdThruster.angle = ship.angle;
			fwdThruster.render();
			revThruster.x = ship.x;
			revThruster.y = ship.y;
			revThruster.angle = ship.angle;
			revThruster.render();
		}

		// Ship is exploding, advance the countdown or create a new ship if it
		// is
		// done exploding. The new ship is added as though it were in
		// hyperspace.
		// (This gives the player time to move the ship if it is in imminent
		// danger.) If that was the last ship, end the game.

		else if (--shipCounter <= 0)
			if (shipsLeft > 0) {
				initShip();
				hyperCounter = Game.HYPER_COUNT;
			} else
				Game.endGame();
	}

	public void stopShip() {

		ship.active = false;
		shipCounter = Game.SCRAP_COUNT;
		if (shipsLeft > 0)
			shipsLeft--;
		if (loaded)
			Game.thrustersSound.stop();
		Game.thrustersPlaying = false;
	}

}
