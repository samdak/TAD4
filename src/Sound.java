import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

public class Sound extends Applet {

	// Counter and total used to track the loading of the sound clips.

	int clipTotal = 0;
	int clipsLoaded = 0;
	
	boolean sound;

	// Sound clips.

	AudioClip crashSound;
	AudioClip explosionSound;
	AudioClip fireSound;
	AudioClip missleSound;
	AudioClip saucerSound;
	AudioClip thrustersSound;
	AudioClip warpSound;

	// Flags for looping sound clips.

	boolean thrustersPlaying;
	boolean saucerPlaying;
	boolean misslePlaying;

	public void loadSounds() {

		// Load all sound clips by playing and immediately stopping them. Update
		// counter and total for display.

		try {
			crashSound = getAudioClip(new URL(getCodeBase(), "crash.au"));
			clipTotal++;
			explosionSound = getAudioClip(new URL(getCodeBase(), "explosion.au"));
			clipTotal++;
			fireSound = getAudioClip(new URL(getCodeBase(), "fire.au"));
			clipTotal++;
			missleSound = getAudioClip(new URL(getCodeBase(), "missle.au"));
			clipTotal++;
			saucerSound = getAudioClip(new URL(getCodeBase(), "saucer.au"));
			clipTotal++;
			thrustersSound = getAudioClip(new URL(getCodeBase(), "thrusters.au"));
			clipTotal++;
			warpSound = getAudioClip(new URL(getCodeBase(), "warp.au"));
			clipTotal++;
		} catch (MalformedURLException e) {
		}

		try {
			crashSound.play();
			crashSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			explosionSound.play();
			explosionSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			fireSound.play();
			fireSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			missleSound.play();
			missleSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			saucerSound.play();
			saucerSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			thrustersSound.play();
			thrustersSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
			warpSound.play();
			warpSound.stop();
			clipsLoaded++;
			repaint();
			Thread.currentThread().sleep(Game.DELAY);
		} catch (InterruptedException e) {
		}
	}

}
