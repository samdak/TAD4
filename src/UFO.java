
public class Ufo {
    // Flying saucer data.

  int ufoPassesLeft;    // Counter for number of flying saucer passes.
  int ufoCounter;       // Timer counter used to track each flying saucer pass.

  AsteroidsSprite   ufo;
  
  public void initUfo() {

    double angle, speed;

    // Randomly set flying saucer at left or right edge of the screen.

    ufo.active = true;
    ufo.x = -AsteroidsSprite.width / 2;
    ufo.y = Math.random() * 2 * AsteroidsSprite.height - AsteroidsSprite.height;
    angle = Math.random() * Math.PI / 4 - Math.PI / 2;
    speed = Variables.MAX_ROCK_SPEED / 2 + Math.random() * (Variables.MAX_ROCK_SPEED / 2);
    ufo.deltaX = speed * -Math.sin(angle);
    ufo.deltaY = speed *  Math.cos(angle);
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

  public void stopUfo() {

    ufo.active = false;
    ufoCounter = 0;
    ufoPassesLeft = 0;
    if (Game.loaded)
      Game.saucerSound.stop();
    Game.saucerPlaying = false;
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
        for (i = 0; i < Variables.MAX_SHOTS; i++)
          if (Game.photons.photons[i].active && ufo.isColliding(Game.photons.photons[i])) {
            if (Game.sound)
              Game.crashSound.play();
            Game.explosions.explode(ufo);
            stopUfo();
            Game.score += Variables.UFO_POINTS;
          }

          // On occassion, fire a missle at the ship if the saucer is not too
          // close to it.

          d = (int) Math.max(Math.abs(ufo.x - Game.ship.ship.x), Math.abs(ufo.y - Game.ship.ship.y));
          if (Game.ship.ship.active && Game.ship.hyperCounter <= 0 &&
              ufo.active && !Game.missle.missle.active &&
              d > Variables.MAX_ROCK_SPEED * Variables.FPS / 2 &&
              Math.random() < Variables.MISSLE_PROBABILITY)
            Game.missle.initMissle();
       }
    }
  }


}
