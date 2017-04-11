/**
 * Created by samer on 2017-04-11.
 */
public class Photons {
    static final int MAX_SHOTS =  8;          // Maximum number of sprites
    static AsteroidsSprite[] photons    = new AsteroidsSprite[MAX_SHOTS];
    // Photon data.

    static int   photonIndex;    // Index to next available photon sprite.
    static long  photonTime;     // Time value used to keep firing rate constant.

    public static void initPhotons() {

        int i;

        for (i = 0; i < MAX_SHOTS; i++)
            photons[i].active = false;
        photonIndex = 0;
    }

    public static void updatePhotons() {

        int i;

        // Move any active photons. Stop it when its counter has expired.

        for (i = 0; i < MAX_SHOTS; i++)
            if (photons[i].active) {
                if (!photons[i].advance())
                    photons[i].render();
                else
                    photons[i].active = false;
            }
    }
}
