
public class Photons {

    // Photon data.
    int photonIndex;    // Index to next available photon sprite.
    long photonTime;     // Time value used to keep firing rate constant.

    AsteroidsSprite[] photons = new AsteroidsSprite[Variables.MAX_SHOTS];

    public void initPhotons() {

        int i;

        for (i = 0; i < Variables.MAX_SHOTS; i++) {
            photons[i].active = false;
        }
        photonIndex = 0;
    }

    public void updatePhotons() {

        int i;

        // Move any active photons. Stop it when its counter has expired.
        for (i = 0; i < Variables.MAX_SHOTS; i++) {
            if (photons[i].active) {
                if (!photons[i].advance()) {
                    photons[i].render();
                } else {
                    photons[i].active = false;
                }
            }
        }
    }

}
