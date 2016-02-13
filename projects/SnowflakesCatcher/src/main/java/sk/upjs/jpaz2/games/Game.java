package sk.upjs.jpaz2.games;

import java.util.*;

import sk.upjs.jpaz2.*;

/**
 * Base class for creating game classes.
 */
public abstract class Game {

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
	 * Window where the game is displayed.
	 */
	private JPAZWindow mainWindow;

	/**
	 * The current scene of the game.
	 */
	private Scene currentScene;

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
	public Game(String name, int width, int height, ImageShape icon) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.icon = icon;
	}

	/**
	 * Constructs the game.
	 * 
	 * @param name
	 *            the name of game.
	 * @param width
	 *            the width of all scenes.
	 * @param height
	 *            the height of all scenes.
	 */
	public Game(String name, int width, int height) {
		this(name, width, height, null);
	}

	/**
	 * Returns the width of all scenes of the game.
	 * 
	 * @return the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of all scenes of the game.
	 * 
	 * @return the height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the name of game.
	 * 
	 * @return the name of game.
	 */
	public String getName() {
		return name;
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
		if (started) {
			throw new RuntimeException(
					"It is not allowed to change background music of a running game.");
		}

		this.backgroundMusic = backgroundMusic;
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
		if (started) {
			throw new RuntimeException(
					"It is not allowed to add a scene to a running game.");
		}

		if (name == null) {
			throw new NullPointerException("Name of scene must not be null.");
		}

		if (scene == null) {
			throw new NullPointerException("Scene must not be null.");
		}

		if (scene.getGame() != this) {
			throw new RuntimeException("Scene does not belong to this game.");
		}

		scenes.put(name, scene);
	}

	/**
	 * Changes the current scene.
	 * 
	 * @param name
	 *            the name of scene.
	 */
	public void changeScene(String name) {
		if (!started) {
			throw new RuntimeException("Game is not running.");
		}

		if (!scenes.containsKey(name)) {
			throw new RuntimeException("Unknown scene \"" + name + "\".");
		}

		if (currentScene != null) {
			currentScene.stop();
		}

		currentScene = scenes.get(name);
		currentScene.start();

		mainWindow.rebindWithEffect(currentScene,
				TransitionEffect.FADE_OUT_WHITE_FADE_IN, 1500);
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
			throw new RuntimeException("Unknown initial scene \""
					+ initialScene + "\"");
		}

		currentScene = scenes.get(initialScene);
		mainWindow = new JPAZWindow(currentScene);
		mainWindow.setTitle(name);
		if (icon != null) {
			mainWindow.setIconImage(icon);
		}
		
		if (backgroundMusic != null) {
			backgroundMusic.playInLoop();
		}
		
		currentScene.start();
	}
}
