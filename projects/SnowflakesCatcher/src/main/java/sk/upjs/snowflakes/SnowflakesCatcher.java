package sk.upjs.snowflakes;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.games.*;

public class SnowflakesCatcher extends Game {

	public SnowflakesCatcher() {
		super("Snowflakes Catcher", 715, 600, new ImageShape("images", "lost_snowflake.png"));
	}

	@Override
	protected void initialize() {
		// background music
		AudioClip clip = new AudioClip("audio", "LetItSnow.mid", true);
		clip.setVolume(0.5);
		setBackgroundMusic(clip);

		// scenes
		addScene(IntroScene.NAME, new IntroScene(this));
		addScene(GameScene.NAME, new GameScene(this));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SnowflakesCatcher game = new SnowflakesCatcher();
		game.run(IntroScene.NAME);
	}
}
