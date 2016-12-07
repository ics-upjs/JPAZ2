package sk.upjs.snowflakes;

import java.awt.event.KeyEvent;
import java.util.*;

import sk.upjs.jpaz2.*;
import sk.upjs.jpaz2.theater.*;

public class GameScene extends Scene {

	/**
	 * Name of the scene.
	 */
	public static final String NAME = "Game";

	/**
	 * Minimal possible x of a falling snowflake.
	 */
	private static final int MIN_SNOWFLAKE_X = 85;

	/**
	 * Maximal possible x of a falling snowflake.
	 */
	private static final int MAX_SNOWFLAKE_X = 595;

	/**
	 * Minimal Y coordinate of snowflake to be considered as a lost snowflake.
	 */
	private static final int LOST_SNOWFLAKE_Y = 580;

	/**
	 * Initial time period between two generated snowflakes.
	 */
	private static final int START_TIME_BETWEEN_SNOWFLAKES = 1600;

	/**
	 * Number of snowflakes after which the number of lost snowflakes is
	 * decreased.
	 */
	private static final int PENALTY_DESCREASE = 5;

	/**
	 * Score of the player.
	 */
	private Scoreboard scoreboard;

	/**
	 * Panel displaying lost snowflakes.
	 */
	private LostSnowflakesPanel lostSnowflakesPanel;

	/**
	 * Panel displaying game over message
	 */
	private GameOverNotification gameoverNotification;

	/**
	 * Turtle that serves as an on/off switch for playing music
	 */
	private MusicOnOffSwitch musicOnOff;

	/**
	 * Back button that changes the screen (scene) to intro screen with menu.
	 */
	private BackButton backButton;

	/**
	 * The figure of a boy catching snowflakes.
	 */
	private Boy boy;

	/**
	 * All snowflakes that are currently falling down.
	 */
	private List<Snowflake> snowflakes = new ArrayList<Snowflake>();

	/**
	 * List of all shapes that a snowflake can have.
	 */
	private List<ImageShape> snowflakesShapes = new ArrayList<ImageShape>();

	/**
	 * Time when the last snowflake was generated.
	 */
	private long lastSnowflakeTime = 0;

	/**
	 * The number of snowflakes eaten after the last penalty decrease.
	 */
	private int penaltyDescreaseCounter = 0;

	/**
	 * Indicates that the game is running.
	 */
	private boolean isGameRunning;

	/**
	 * Audio for eating
	 */
	private AudioClip eatingClip;

	/**
	 * Audio for lost snowflake
	 */
	private AudioClip lostClip;

	/**
	 * Audio for gameover
	 */
	private AudioClip gameoverClip;

	/**
	 * Audio for event when the penalty (number of lost snowflakes) is
	 * decreased.
	 */
	private AudioClip penaltyDecreaseClip;

	/**
	 * Constructs a new main scene of the game.
	 */
	public GameScene(Stage stage) {
		super(stage);

		// construct all objects in this pane
		prepareScreen();
	}

	/**
	 * Initializes screen (creates all objects on the screen and paints the
	 * background).
	 */
	private void prepareScreen() {
		setBorderWidth(0);

		// paint background
		Turtle painter = new Turtle();
		painter.setShape(new ImageShape("images", "country.jpg"));
		add(painter);
		painter.center();
		painter.stamp();
		remove(painter);

		// create and add the music on/off switch
		musicOnOff = new MusicOnOffSwitch(getStage());
		musicOnOff.updateView();
		musicOnOff.setPosition(5, 5);
		add(musicOnOff);

		// create and place back button
		backButton = new BackButton(getStage());
		backButton.setPosition(9, 55);
		add(backButton);

		// create a boy turtle
		boy = new Boy();
		boy.setPosition(325, 540);
		add(boy);

		// load shapes for snowflakes
		snowflakesShapes.add(new ImageShape("images", "snowflake1.gif"));
		snowflakesShapes.add(new ImageShape("images", "snowflake2.gif"));
		snowflakesShapes.add(new ImageShape("images", "snowflake3.gif"));

		// create scoreboard
		scoreboard = new Scoreboard();
		scoreboard.setPosition(655, 7);
		add(scoreboard);

		// create panel (tube) for lost snowflakes
		lostSnowflakesPanel = new LostSnowflakesPanel();
		lostSnowflakesPanel.setPosition(665, 350);
		add(lostSnowflakesPanel);

		// create game-over notification
		gameoverNotification = new GameOverNotification(getStage());
		gameoverNotification.setPosition(0, 100);

		// load audio
		eatingClip = new AudioClip("audio", "eaten.wav", true);
		lostClip = new AudioClip("audio", "lost.wav", true);
		gameoverClip = new AudioClip("audio", "gameover.wav", false);
		penaltyDecreaseClip = new AudioClip("audio", "penaltydec.wav", true);

		// set repeat period for left and right keys
		setKeyRepeatPeriod(KeyEvent.VK_LEFT, 100);
		setKeyRepeatPeriod(KeyEvent.VK_RIGHT, 100);
	}

	/**
	 * Starts the game.
	 */
	private void startGame() {
		// set initial score
		scoreboard.resetScore();
		lostSnowflakesPanel.reset();
		penaltyDescreaseCounter = 0;

		// remove game-over panel (if displayed)
		remove(gameoverNotification);

		// lostSnowflakesCount = 0;
		// update the music on/off switch
		musicOnOff.updateView();

		// move the boy to the starting position
		boy.setX(325);

		// set that the game is running
		isGameRunning = true;
		// start tick timer to manage falling snowflakes
		setTickPeriod(50);
	}

	/**
	 * Stops the game.
	 */
	private void stopGame() {
		if (!isGameRunning)
			return;

		isGameRunning = false;
		gameoverNotification.showSlowly();
		add(gameoverNotification);

		if (musicOnOff.isMusicOn())
			gameoverClip.play();
	}

	/**
	 * Starts execution of the screen.
	 */
	public void start() {
		startGame();
	}

	/**
	 * Stops execution of the screen.
	 */
	public void stop() {
		// remove all snowflakes and stop ticking
		setTickPeriod(0);
		for (Snowflake snowflake : snowflakes)
			remove(snowflake);

		snowflakes.clear();
	}

	public void cancelGame() {
		// stop falling of snowflakes
		setTickPeriod(0);

		// remove all falling snowflakes
		for (Snowflake snowflake : snowflakes)
			this.remove(snowflake);

		snowflakes.clear();
	}

	@Override
	protected void onKeyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			boy.stepLeft();
			tryEatSnowflakes();
		}

		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			boy.stepRight();
			tryEatSnowflakes();
		}
	}

	@Override
	protected void onTick() {
		// a small step for all falling snowflakes
		for (Snowflake snowflake : snowflakes)
			snowflake.doFallingStep();

		// try eat snowflakes
		tryEatSnowflakes();

		// remove lost snowflakes
		removeLostSnowflakes();

		// generate new snowflake (if necessary)
		if (isGameRunning)
			generateNewSnowflake();

		// check whether the game is over
		if (isGameRunning && lostSnowflakesPanel.isFull())
			stopGame();

		// if the game is over and there are no snowflakes, then stop ticking
		if (!isGameRunning && snowflakes.isEmpty())
			setTickPeriod(0);
	}

	/**
	 * Tries to eat falling snowflakes.
	 */
	private void tryEatSnowflakes() {
		// do nothing, if the game is not running
		if (!isGameRunning)
			return;

		// find snowflakes that can be eaten
		ArrayList<Snowflake> eatenSnowflakes = new ArrayList<Snowflake>();
		for (Snowflake snowflake : snowflakes)
			if (boy.canEat(snowflake))
				eatenSnowflakes.add(snowflake);

		// if no snowflake can be eaten, then return
		if (eatenSnowflakes.isEmpty())
			return;

		// remove all eaten snowflakes
		snowflakes.removeAll(eatenSnowflakes);
		for (Snowflake snowflake : eatenSnowflakes)
			this.remove(snowflake);

		// update score
		scoreboard.increaseScore(eatenSnowflakes.size());

		// play sound
		if (musicOnOff.isMusicOn())
			eatingClip.playAsActionSound();

		// if bonus is reached, then change penaly (lost snowflakes)
		penaltyDescreaseCounter += eatenSnowflakes.size();
		if (penaltyDescreaseCounter >= PENALTY_DESCREASE) {
			penaltyDescreaseCounter = 0;
			lostSnowflakesPanel.changeBy(-1);

			if (musicOnOff.isMusicOn())
				penaltyDecreaseClip.play();
		}
	}

	/**
	 * Removes all lost snowflakes from the scene (pane).
	 */
	private void removeLostSnowflakes() {
		// find lost snowflakes
		ArrayList<Snowflake> lostSnowflakes = new ArrayList<Snowflake>();
		for (Snowflake snowflake : snowflakes)
			if (snowflake.getY() >= LOST_SNOWFLAKE_Y)
				lostSnowflakes.add(snowflake);

		// if no snowflake is lost, then return
		if (lostSnowflakes.isEmpty())
			return;

		// remove all lost snowflakes
		snowflakes.removeAll(lostSnowflakes);
		for (Snowflake snowflake : lostSnowflakes)
			this.remove(snowflake);

		// update the number of lost snowflakes (only if the game is running)
		if (isGameRunning) {
			lostSnowflakesPanel.changeBy(lostSnowflakes.size());
			if (musicOnOff.isMusicOn())
				lostClip.playAsActionSound();
		}
	}

	/**
	 * Generates new snowflake if necessary
	 */
	private void generateNewSnowflake() {
		long currentTime = System.currentTimeMillis();

		// don't generate a new snowflake too soon
		if (lastSnowflakeTime + START_TIME_BETWEEN_SNOWFLAKES - 3 * scoreboard.getScore() > currentTime)
			return;

		Snowflake snowflake = new Snowflake();

		// choose a random shape and initial animation frame
		int randomShapeIdx = (int) (Math.random() * snowflakesShapes.size());
		snowflake.setShape(snowflakesShapes.get(randomShapeIdx));
		int randomFrameIdx = (int) (Math.random() * snowflake.getFrameCount());
		snowflake.setFrameIndex(randomFrameIdx);

		// increase/decrease animation speed +- 20%
		long frameDuration = (int) (snowflake.getFrameDuration() * (0.8 + Math.random() * 0.40));
		snowflake.setFrameDuration(frameDuration);

		// set random horizontal position for this falling snowflake
		double randomX = MIN_SNOWFLAKE_X + (Math.random()) * (MAX_SNOWFLAKE_X - MIN_SNOWFLAKE_X);
		snowflake.setPosition(randomX, -snowflakesShapes.get(randomShapeIdx).getWidth() / 2);

		snowflake.setSpeed(3 + Math.random() * 2);
		// add the snowflake to the pane and to the list of falling snowflakes
		snowflakes.add(snowflake);
		this.add(snowflake);

		lastSnowflakeTime = currentTime;
	}
}