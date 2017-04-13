
public class Missle {
    
    // Missle data.

  int missleCounter;    // Counter for life of missle.

    AsteroidsSprite   missle;
    
  public void initMissle() {

    missle.active = true;
    missle.angle = 0.0;
    missle.deltaAngle = 0.0;
    missle.x = Game.ufo.ufo.x;
    missle.y = Game.ufo.ufo.y;
    missle.deltaX = 0.0;
    missle.deltaY = 0.0;
    missle.render();
    missleCounter = Variables.MISSLE_COUNT;
    if (Game.sound)
      Game.missleSound.loop();
    Game.misslePlaying = true;
  }

  
  public void stopMissle() {

    missle.active = false;
    missleCounter = 0;
    if (Game.loaded)
      Game.missleSound.stop();
    Game.misslePlaying = false;
  }
  
  public void guideMissle() {

    double dx, dy, angle;

    if (!Game.ship.ship.active || Game.ship.hyperCounter > 0)
      return;

    // Find the angle needed to hit the ship.

    dx = Game.ship.ship.x - missle.x;
    dy = Game.ship.ship.y - missle.y;
    if (dx == 0 && dy == 0)
      angle = 0;
    if (dx == 0) {
      if (dy < 0)
        angle = -Math.PI / 2;
      else
        angle = Math.PI / 2;
    }
    else {
      angle = Math.atan(Math.abs(dy / dx));
      if (dy > 0)
        angle = -angle;
      if (dx < 0)
        angle = Math.PI - angle;
    }

    // Adjust angle for screen coordinates.

    missle.angle = angle - Math.PI / 2;

    // Change the missle's angle so that it points toward the ship.

    missle.deltaX = 0.75 * Variables.MAX_ROCK_SPEED * -Math.sin(missle.angle);
    missle.deltaY = 0.75 * Variables.MAX_ROCK_SPEED *  Math.cos(missle.angle);
  }

  public void updateMissle() {

    int i;

    // Move the guided missle and check for collision with ship or photon. Stop
    // it when its counter has expired.

    if (missle.active) {
      if (--missleCounter <= 0)
        stopMissle();
      else {
        guideMissle();
        missle.advance();
        missle.render();
        for (i = 0; i < Variables.MAX_SHOTS; i++)
          if (Game.photons.photons[i].active && missle.isColliding(Game.photons.photons[i])) {
            if (Game.sound)
              Game.crashSound.play();
            Game.explosions.explode(missle);
            stopMissle();
            Game.score += Variables.MISSLE_POINTS;
          }
        if (missle.active && Game.ship.ship.active &&
            Game.ship.hyperCounter <= 0 && Game.ship.ship.isColliding(missle)) {
          if (Game.sound)
            Game.crashSound.play();
          Game.explosions.explode(Game.ship.ship);
          Game.ship.stopShip();
          Game.ufo.stopUfo();
          stopMissle();
        }
      }
    }
  }

}
