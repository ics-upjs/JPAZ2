package sk.upjs.jpaz2.theater;

import java.util.*;
import java.util.prefs.Preferences;

import sk.upjs.jpaz2.*;

/**
 * Base class for creating stage. Each stage can configured to show several
 * scenes.
 */
public abstract class Stage {

	/**
	 * Preference key storing whether the music is muted.
	 */
	private static final String MUTE_MUSIC_KEY = "$mute-music";

	/**
	 * Indicates whether the game started.
	 */
	private boolean started;

	/**
	 * Width of all scenes of the game.
	 */
	private final int width;

	/**
	 * Height of all scenes of the game.
	 */
	private final int height;

	/**
	 * Name of the game.
	 */
	private final String name;

	/**
	 * Game icon.
	 */
	private final ImageShape icon;

	/**
	 * Scenes of the game.
	 */
	private final Map<String, Scene> scenes = new HashMap<String, Scene>();

	/**
	 * Music clip played in the background.
	 */
	private AudioClip backgroundMusic;

	/**
	 * Indicates whether background music is muted.
	 */
	private boolean mutedMusic;

	/**
	 * Window where the game is displayed.
	 */
	private JPAZWindow mainWindow;

	/**
	 * The current scene of the game.
	 */
	private Scene currentScene;

	/**
	 * Name of the current scene.
	 */
	private String nameOfCurrentScene;

	private final Preferences preferences;

	/**
	 * Constructs the game.
	 * 
	 * @param name
	 *            the name of game.
	 * @param width
	 *            the width of all scenes.
	 * @param height
	 *            the height of all scenes.
	 * @param icon
	 *            the icon of the game.
	 */
	public Stage(String name, int width, int height, ImageShape icon) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.icon = icon;
		this.preferences = createUserPreferences();

		initializePreferences();
	}

	/**
	 * Constructs the stage.
	 * 
	 * @param name
	 *            the name of stage.
	 * @param width
	 *            the width of all scenes.
	 * @param height
	 *            the height of all scenes.
	 */
	public Stage(String name, int width, int height) {
		this(name, width, height, null);
	}

	/**
	 * Initializes components of the game.
	 */
	protected abstract void initialize();

	/**
	 * Starts the game.
	 */
	public void run(String initialScene) {
		if (started) {
			throw new RuntimeException("Game can be started only once.");
		}

		SplashFrame.showSplash(name);
		initialize();
		SplashFrame.hideSplash();

		started = true;
		if (!scenes.containsKey(initialScene)) {
			throw new RuntimeException("Unknown initial scene \"" + initialScene + "\"");
		}

		currentScene = scenes.get(initialScene);
		nameOfCurrentScene = initialScene;

		mainWindow = new JPAZWindow(currentScene);
		mainWindow.setTitle(name);
		if (icon != null) {
			mainWindow.setIconImage(icon);
		}

		if ((backgroundMusic != null) && (!mutedMusic)) {
			backgroundMusic.playInLoop();
		}

		currentScene.start();
	}

	/**
	 * Changes the current scene.
	 * 
	 * @param name
	 *            the name of scene.
	 */
	public void changeScene(String name) {
		changeScene(name, null, 0);
	}

	/**
	 * Changes the current scene with a transition effect.
	 * 
	 * @param name
	 *            the name of scene.
	 * @param effect
	 *            the transition effect.
	 * @param duration
	 *            the duration of effect in milliseconds.
	 */
	public void changeScene(String name, TransitionEffect effect, long duration) {
		if (!started) {
			throw new RuntimeException("Stage is not running.");
		}

		if (!scenes.containsKey(name)) {
			throw new RuntimeException("Unknown scene \"" + name + "\".");
		}

		if (currentScene != null) {
			currentScene.stop();
		}

		currentScene = scenes.get(name);
		nameOfCurrentScene = name;
		currentScene.start();

		if ((effect == null) || (duration <= 0)) {
			mainWindow.bindTo(currentScene);
		} else {
			mainWindow.rebindWithEffect(currentScene, effect, duration);
		}
	}

	/**
	 * Returns the width of all scenes of the stage.
	 * 
	 * @return the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of all scenes of the stage.
	 * 
	 * @return the height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the name of stage.
	 * 
	 * @return the name of stage.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the current scene.
	 * 
	 * @return the current scene.
	 */
	public Scene getCurrentScene() {
		return currentScene;
	}

	/**
	 * Returns name of the current scene.
	 * 
	 * @return the name of the current scene.
	 */
	public String getNameOfCurrentScene() {
		return nameOfCurrentScene;
	}

	/**
	 * Returns the background music.
	 * 
	 * @return the background music.
	 */
	public AudioClip getBackgroundMusic() {
		return backgroundMusic;
	}

	/**
	 * Sets the background music.
	 * 
	 * @param backgroundMusic
	 *            the desired background music.
	 */
	public void setBackgroundMusic(AudioClip backgroundMusic) {
		if (this.backgroundMusic == backgroundMusic) {
			return;
		}

		boolean playing = false;
		if (this.backgroundMusic != null) {
			playing = this.backgroundMusic.isPlaying();
			this.backgroundMusic.stop();
		}

		this.backgroundMusic = backgroundMusic;

		if ((this.backgroundMusic != null) && started && playing) {
			this.backgroundMusic.playInLoop();
		}
	}

	/**
	 * Returns whether the music is muted.
	 * 
	 * @return true, if the music is muted, false otherwise.
	 */
	public boolean isMutedMusic() {
		return mutedMusic;
	}

	/**
	 * Sets whether the music is muted.
	 * 
	 * @param mutedMusic
	 *            true, if the music should be muted, false otherwise.
	 */
	public void setMutedMusic(boolean mutedMusic) {
		if (this.mutedMusic == mutedMusic) {
			return;
		}

		this.mutedMusic = mutedMusic;
		if ((backgroundMusic != null) && started) {
			if (this.mutedMusic) {
				backgroundMusic.stop();
			} else {
				backgroundMusic.playInLoop();
			}
		}

		if (preferences != null) {
			try {
				preferences.putBoolean(MUTE_MUSIC_KEY, this.mutedMusic);
			} catch (Exception ignore) {
				System.err.println("Setting of preference " + MUTE_MUSIC_KEY + " failed.");
			}
		}
	}

	/**
	 * Adds scene to the game.
	 * 
	 * @param name
	 *            the name of scene.
	 * @param scene
	 *            the scene.
	 */
	public void addScene(String name, Scene scene) {
		if (name == null) {
			throw new NullPointerException("Name of scene must not be null.");
		}

		if (scene == null) {
			throw new NullPointerException("Scene must not be null.");
		}

		if (scene.getStage() != this) {
			throw new RuntimeException("Scene does not belong to this stage.");
		}

		if (scenes.containsKey(name)) {
			throw new RuntimeException("Scene " + name + " is already a part of the stage.");
		}

		scenes.put(name, scene);
	}

	/**
	 * Sets value of a preference.
	 * 
	 * @param key
	 *            the key.
	 * @param value
	 *            the value.
	 * @return true, when writing of preference was successful, false otherwise.
	 */
	public boolean setPreference(String key, String value) {
		checkPreferenceKey(key);
		key = key.trim();

		if (preferences == null) {
			return false;
		}

		try {
			if (value == null) {
				preferences.remove(key);
			} else {
				preferences.put(key, value);
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns value of a preference.
	 * 
	 * @param key
	 *            the key.
	 * @param defaultValue
	 *            the default value.
	 * @return the value of preference, or default value, if retrieval of the
	 *         preference value failed.
	 */
	public String getPreference(String key, String defaultValue) {
		checkPreferenceKey(key);
		key = key.trim();

		if (preferences == null) {
			return defaultValue;
		}

		try {
			return preferences.get(key, defaultValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Check whether preference key is valid.
	 * 
	 * @param key
	 *            the key.
	 */
	private void checkPreferenceKey(String key) {
		if (key == null) {
			throw new NullPointerException("Key of preference cannot be null.");
		}

		key = key.trim();
		if (key.isEmpty() || key.startsWith("$") || key.contains("/")) {
			throw new RuntimeException("Invalid key of preference.");
		}
	}

	/**
	 * Loads preferences.
	 */
	private Preferences createUserPreferences() {
		try {
			return Preferences.userNodeForPackage(this.getClass());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Initializes preferences.
	 */
	private void initializePreferences() {
		if (preferences == null) {
			return;
		}

		try {
			mutedMusic = preferences.getBoolean(MUTE_MUSIC_KEY, false);
		} catch (Exception ignore) {
			System.err.println("Retrieving of preference " + MUTE_MUSIC_KEY + " failed.");
		}
	}
}