package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * Transition effects for JPAZPanel during switching process (rebinding) from
 * one pane to another pane.
 */
public enum TransitionEffect {

	/**
	 * Fade OUT - Fade IN transition.
	 */
	FADE_OUT_FADE_IN() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new FadeOutFadeInTransition(panel, duration);
		}
	},

	/**
	 * Fade OUT - BLACK - Fade IN transition.
	 */
	FADE_OUT_BLACK_FADE_IN() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new FadeOutColorFadeInTransition(panel, duration, Color.black);
		}
	},

	/**
	 * Fade OUT - WHITE - Fade IN transition.
	 */
	FADE_OUT_WHITE_FADE_IN() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new FadeOutColorFadeInTransition(panel, duration, Color.white);
		}
	},

	/**
	 * MOVE LEFT transition.
	 */
	MOVE_LEFT() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new MoveAwayTransition(panel, duration, 270);
		}
	},

	/**
	 * MOVE RIGHT transition.
	 */
	MOVE_RIGHT() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new MoveAwayTransition(panel, duration, 90);
		}
	},

	/**
	 * MOVE UP transition.
	 */
	MOVE_UP() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new MoveAwayTransition(panel, duration, 0);
		}
	},

	/**
	 * MOVE DOWN transition.
	 */
	MOVE_DOWN() {
		@Override
		Transition createImplementation(JPAZPanel panel, long duration) {
			return new MoveAwayTransition(panel, duration, 180);
		}
	};

	// ---------------------------------------------------------------------------------------------------
	// Basic class implementing a transition from one pane to another pane
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Basic class providing a core functionality to implement transition from
	 * one pane content to another.
	 */
	static abstract class Transition {

		/**
		 * Content of the source (from) pane at time, when transition started.
		 */
		private BufferedImage sourcePaneImage;

		/**
		 * Initial rotation of the source (from) pane.
		 */
		private double sourcePaneRotation;

		/**
		 * Position of the center of the from.
		 */
		private Point2D sourcePaneCenter;

		/**
		 * Position of the from pane.
		 */
		private Point2D sourcePanePosition;

		/**
		 * Align mode of the from pane.
		 */
		private boolean sourcePaneAlignMode;

		/**
		 * Time when transition started.
		 */
		final private long startTime;

		/**
		 * Transition duration in milliseconds.
		 */
		final private long duration;

		/**
		 * JPAZPanel where the transition is shown.
		 */
		final private JPAZPanel panel;

		/**
		 * Tick timer that controls timing of transition.
		 */
		private TickTimer tickTimer;

		/**
		 * Backbuffer for the source pane.
		 */
		private BufferedImage sourceBackbuffer;

		/**
		 * Backbuffer for the target pane.
		 */
		private BufferedImage targetBackbuffer;

		/**
		 * Constructs implementation of a transition effect.
		 * 
		 * @param panel
		 *            the panel where transition is displayed.
		 * @param duration
		 *            the duration of transition in milliseconds.
		 */
		Transition(JPAZPanel panel, long duration) {
			synchronized (JPAZUtilities.getJPAZLock()) {
				this.duration = Math.max(0, duration);
				this.panel = panel;
				copySourcePane(panel.getPane(), panel.isAlignMode());
				startTime = System.currentTimeMillis();
			}
		}

		/**
		 * Make copy of all related data from the source pane that are required
		 * for transition.
		 */
		private void copySourcePane(Pane sourcePane, boolean alignMode) {
			synchronized (JPAZUtilities.getJPAZLock()) {
				// copy location and orientation settings
				sourcePaneCenter = sourcePane.getCenter();
				sourcePanePosition = sourcePane.getPosition();
				sourcePaneRotation = sourcePane.getRotation();
				sourcePaneAlignMode = alignMode;

				// create copy of the pane content
				sourcePaneImage = new BufferedImage(sourcePane.getWidth(), sourcePane.getHeight(),
						BufferedImage.TYPE_INT_ARGB_PRE);
				Graphics2D g2d = sourcePaneImage.createGraphics();
				sourcePane.paintWithoutTransform(g2d);
				g2d.dispose();
			}
		}

		/**
		 * Starts the transition with given refresh period. This method must be
		 * called in the transition constructor.
		 */
		protected void startTransition(long refreshPeriod) {
			synchronized (JPAZUtilities.getJPAZLock()) {
				// refresh period must be at least 25 milliseconds
				refreshPeriod = Math.max(25, refreshPeriod);

				// create tick time
				tickTimer = new TickTimer(false) {
					@Override
					protected void onTick() {
						checkStop();
						panel.repaint();
					}
				};

				// set and enable tick timer
				tickTimer.setTickPeriod(refreshPeriod);
				tickTimer.setEnabled(true);
			}
		}

		/**
		 * Checks whether transition must be stopped.
		 */
		protected void checkStop() {
			long currentTime = System.currentTimeMillis();
			synchronized (JPAZUtilities.getJPAZLock()) {
				if (startTime + duration <= currentTime) {
					stop();
				}
			}
		}

		/**
		 * Returns whether this transition is completed.
		 * 
		 * @return true, if this transition is completed, false otherwise.
		 */
		public boolean isCompleted() {
			synchronized (JPAZUtilities.getJPAZLock()) {
				return (tickTimer == null);
			}
		}

		/**
		 * Stops the transition.
		 */
		public void stop() {
			synchronized (JPAZUtilities.getJPAZLock()) {
				if (tickTimer != null) {
					tickTimer.setEnabled(false);
					tickTimer = null;
					panel.repaint();
				}
			}
		}

		/**
		 * Paints the source pane to given image.
		 * 
		 * @param image
		 *            the image where the source pane is painted.
		 */
		private void paintSourceToImage(BufferedImage image) {
			synchronized (JPAZUtilities.getJPAZLock()) {
				if (sourcePaneImage == null)
					return;

				Graphics2D g2d = image.createGraphics();
				if (sourcePaneAlignMode) {
					g2d.drawImage(sourcePaneImage, null, 0, 0);
				} else {
					g2d.translate(sourcePanePosition.getX(), sourcePanePosition.getY());
					g2d.rotate(Math.toRadians(sourcePaneRotation));
					g2d.translate(-sourcePaneCenter.getX(), -sourcePaneCenter.getY());
					g2d.drawImage(sourcePaneImage, null, 0, 0);
				}

				g2d.dispose();
			}
		}

		/**
		 * Paints the pane to given image.
		 * 
		 * @param image
		 *            the image where the target pane is painted.
		 * @param pane
		 *            the pane to be painted.
		 * @param alignMode
		 *            the align mode.
		 */
		private void paintPaneToImage(BufferedImage image, Pane pane, boolean alignMode) {
			if (pane == null)
				return;

			Graphics2D g2d = image.createGraphics();
			if (alignMode)
				pane.paintWithoutTransform(g2d);
			else
				pane.paintToPaneGraphics(g2d);
			g2d.dispose();
		}

		/**
		 * Paints current state of transition.
		 * 
		 * @param pane
		 *            the pane that is the target of the transition.
		 * @param alignMode
		 *            the align mode of the panel where transition is realized.
		 * @param g
		 *            the graphics where the current state of transition is
		 *            painted.
		 * @param width
		 *            the width of the panel where transition is realized.
		 * @param height
		 *            the height of the panel where transition is realized.
		 */
		void paintToPanel(Pane pane, boolean alignMode, Graphics2D g, int width, int height) {
			synchronized (JPAZUtilities.getJPAZLock()) {
				// create or change backbuffer for the source pane
				if ((sourceBackbuffer == null) || (sourceBackbuffer.getWidth() != width)
						|| (sourceBackbuffer.getHeight() != height)) {
					sourceBackbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
					paintSourceToImage(sourceBackbuffer);
				}

				// create backbuffer for the target pane.
				if ((targetBackbuffer == null) || (targetBackbuffer.getWidth() != width)
						|| (targetBackbuffer.getHeight() != height))
					targetBackbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

				// repaint backbuffer for the target pane
				Graphics2D g2d = targetBackbuffer.createGraphics();
				g2d.setBackground(new Color(0, 0, 0, 0));
				g2d.clearRect(0, 0, width, height);
				g2d.dispose();
				paintPaneToImage(targetBackbuffer, pane, alignMode);

				// calculate current progress in the transition
				long currentTime = System.currentTimeMillis() - startTime;
				double percentage = (duration == 0) ? 1 : currentTime / (double) duration;
				// normalize percentage
				percentage = Math.min(percentage, 1);
				percentage = Math.max(percentage, 0);

				// check whether the transition is stopped - if it is stopped,
				// then it is considered as completed
				if (tickTimer == null)
					percentage = 1;

				// call painting defined by the subclass implementation
				g2d = (Graphics2D) g.create();
				onTransitionPaint(g2d, Math.min(percentage, 1), sourceBackbuffer, targetBackbuffer);
				g2d.dispose();
			}
		}

		/**
		 * The main method that paints current state of the transition. This
		 * method must be overridden by subclasses and is always called with
		 * JPAZLock.
		 * 
		 * @param g
		 *            the graphics where the transition state is painted.
		 * @param percentage
		 *            the current progress of transition (a number between 0 and
		 *            1)
		 * @param sourceImage
		 *            the visual representation of the source pane.
		 * @param targetImage
		 *            the visual representation of the target pane.
		 */
		protected abstract void onTransitionPaint(Graphics2D g, double percentage, BufferedImage sourceImage,
				BufferedImage targetImage);
	}

	// ---------------------------------------------------------------------------------------------------
	// Fade OUT - Fade IN transition
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Fade out - fade in transition.
	 */
	static class FadeOutFadeInTransition extends Transition {

		FadeOutFadeInTransition(JPAZPanel panel, long duration) {
			super(panel, duration);
			startTransition(50);
		}

		@Override
		protected void onTransitionPaint(Graphics2D g, double percentage, BufferedImage sourceImage,
				BufferedImage targetImage) {

			if (percentage <= 0.5) {
				float alpha = Math.max(0, (float) (1.0 - 2 * percentage));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				g.drawImage(sourceImage, null, 0, 0);
			} else {
				float alpha = (float) (2 * percentage - 1.0);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				g.drawImage(targetImage, null, 0, 0);
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Fade OUT - COLOR - Fade IN transition
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Fade out - Color - Fade in transition.
	 */
	static class FadeOutColorFadeInTransition extends Transition {

		/**
		 * Color in the middle.
		 */
		private Color color;

		FadeOutColorFadeInTransition(JPAZPanel panel, long duration, Color color) {
			super(panel, duration);
			this.color = color;
			startTransition(50);
		}

		@Override
		protected void onTransitionPaint(Graphics2D g, double percentage, BufferedImage sourceImage,
				BufferedImage targetImage) {
			g.setBackground(color);
			g.clearRect(0, 0, targetImage.getWidth(), targetImage.getHeight());
			if (percentage <= 0.5) {
				float alpha = Math.max(0, (float) (1.0 - 2 * percentage));
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				g.drawImage(sourceImage, null, 0, 0);
			} else {
				float alpha = (float) (2 * percentage - 1.0);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				g.drawImage(targetImage, null, 0, 0);
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Move away transition
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Move away transition.
	 */
	static class MoveAwayTransition extends Transition {

		/**
		 * Direction of the movement.
		 */
		private double direction;

		/**
		 * X-coordinate of the vector movement.
		 */
		private double dx = 0;

		/**
		 * Y-coordinate of the vector movement.
		 */
		private double dy = 0;

		MoveAwayTransition(JPAZPanel panel, long duration, double direction) {
			super(panel, duration);
			direction %= 360;
			if (direction < 0)
				direction += 360;

			dx = Math.cos(Math.toRadians(90 - direction));
			dy = -Math.sin(Math.toRadians(90 - direction));

			this.direction = direction;
			startTransition(50);
		}

		@Override
		protected void onTransitionPaint(Graphics2D g, double percentage, BufferedImage sourceImage,
				BufferedImage targetImage) {

			// draw target image
			g.drawImage(targetImage, null, 0, 0);

			// compute target length
			double currentLength = computeLength(targetImage.getWidth(), targetImage.getHeight()) * percentage;
			int xSource = (int) Math.round(dx * currentLength);
			int ySource = (int) Math.round(dy * currentLength);
			g.drawImage(sourceImage, null, xSource, ySource);
		}

		/**
		 * Compute the total length of the move.
		 */
		private double computeLength(int width, int height) {
			if (Math.abs(direction - 0) <= 1)
				return height;

			if (Math.abs(direction - 90) <= 1)
				return width;

			if (Math.abs(direction - 180) <= 1)
				return height;

			if (Math.abs(direction - 270) <= 1)
				return width;

			return 0;
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Factory for implementations.
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs a new transition implementation for given transition mode.
	 * 
	 * @param panel
	 *            the panel where transition is displayed.
	 * @param duration
	 *            the duration of transition in milliseconds.
	 * 
	 * @return an underlying transition implementation.
	 */
	abstract Transition createImplementation(JPAZPanel panel, long duration);
}
