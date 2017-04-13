
import java.awt.Polygon;


public class Explosions {
    
    AsteroidsSprite[] explosions = new AsteroidsSprite[Variables.MAX_SCRAP];
    
    boolean detail;
    // Explosion data.

  int[] explosionCounter = new int[Variables.MAX_SCRAP];  // Time counters for explosions.
  int   explosionIndex;                         // Next available explosion sprite.

  public void initExplosions() {

    int i;

    for (i = 0; i < Variables.MAX_SCRAP; i++) {
      explosions[i].shape = new Polygon();
      explosions[i].active = false;
      explosionCounter[i] = 0;
    }
    explosionIndex = 0;
  }

  public void explode(AsteroidsSprite s) {

    int c, i, j;
    int cx, cy;

    // Create sprites for explosion animation. The each individual line segment
    // of the given sprite is used to create a new sprite that will move
    // outward  from the sprite's original position with a random rotation.

    s.render();
    c = 2;
    if (detail || s.sprite.npoints < 6)
      c = 1;
    for (i = 0; i < s.sprite.npoints; i += c) {
      explosionIndex++;
      if (explosionIndex >= Variables.MAX_SCRAP)
        explosionIndex = 0;
      explosions[explosionIndex].active = true;
      explosions[explosionIndex].shape = new Polygon();
      j = i + 1;
      if (j >= s.sprite.npoints)
        j -= s.sprite.npoints;
      cx = (int) ((s.shape.xpoints[i] + s.shape.xpoints[j]) / 2);
      cy = (int) ((s.shape.ypoints[i] + s.shape.ypoints[j]) / 2);
      explosions[explosionIndex].shape.addPoint(
        s.shape.xpoints[i] - cx,
        s.shape.ypoints[i] - cy);
      explosions[explosionIndex].shape.addPoint(
        s.shape.xpoints[j] - cx,
        s.shape.ypoints[j] - cy);
      explosions[explosionIndex].x = s.x + cx;
      explosions[explosionIndex].y = s.y + cy;
      explosions[explosionIndex].angle = s.angle;
      explosions[explosionIndex].deltaAngle = 4 * (Math.random() * 2 * Variables.MAX_ROCK_SPIN - Variables.MAX_ROCK_SPIN);
      explosions[explosionIndex].deltaX = (Math.random() * 2 * Variables.MAX_ROCK_SPEED - Variables.MAX_ROCK_SPEED + s.deltaX) / 2;
      explosions[explosionIndex].deltaY = (Math.random() * 2 * Variables.MAX_ROCK_SPEED - Variables.MAX_ROCK_SPEED + s.deltaY) / 2;
      explosionCounter[explosionIndex] = Variables.SCRAP_COUNT;
    }
  }

  public void updateExplosions() {

    int i;

    // Move any active explosion debris. Stop explosion when its counter has
    // expired.

    for (i = 0; i < Variables.MAX_SCRAP; i++)
      if (explosions[i].active) {
        explosions[i].advance();
        explosions[i].render();
        if (--explosionCounter[i] < 0)
          explosions[i].active = false;
      }
  }

}
