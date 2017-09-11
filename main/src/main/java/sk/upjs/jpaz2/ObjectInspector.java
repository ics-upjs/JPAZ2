package sk.upjs.jpaz2;

import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;

import sk.upjs.jpaz2.inspector.OIPanel;

/**
 * The visual GUI based object inspector. The object inspector allows to watch
 * and change object's properties and invoke object's methods.
 */
public class ObjectInspector {

	/**
	 * A record about an object inserted for inspection in the object inspector.
	 */
	private static class ObjectItem {
		/**
		 * Inspected object
		 */
		Object object;
		/**
		 * Alias for the object (if not null, this name is displayed in the
		 * ComboBox)
		 */
		String alias;
		/**
		 * Stop class for object inspection.
		 */
		Class<?> stopClass;

		/**
		 * Constructs a new record about an inspected object.
		 * 
		 * @param object
		 *            the inspected object
		 * @param stopClass
		 *            the stop class
		 * @param alias
		 *            the alias name for the object
		 */
		public ObjectItem(Object object, Class<?> stopClass, String alias) {
			if (object == null)
				throw new NullPointerException("The inspected object cannot be null.");

			if (alias != null) {
				alias = alias.trim();
				if (alias.length() == 0) {
					alias = null;
				}
			}

			this.object = object;
			this.alias = alias;
			this.stopClass = stopClass;
		}

		@Override
		public String toString() {
			if (alias == null) {
				return object.toString();
			} else {
				return alias;
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------
	// Internal GUI objects
	// ---------------------------------------------------------------------------------------------------

	/**
	 * JFrame visualizing the ObjectInspector
	 */
	private JFrame frame;

	/**
	 * Panel visualizing one object
	 */
	private OIPanel oipanel;

	/**
	 * JComboBox for choosing one object that is visualized in the oipanel
	 */
	private JComboBox<ObjectItem> objectComboBox;

	// ---------------------------------------------------------------------------------------------------
	// Constructors
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Constructs a new object inspector. Initially, the object inspector is
	 * invisible and empty.
	 */
	public ObjectInspector() {
		JPAZUtilities.lockHeadlessMode();
		if (JPAZUtilities.isHeadlessMode()) {
			throw new IllegalStateException("Object inspector cannot is not allowed in headless mode.");
		}

		JPAZUtilities.invokeAndWait(new Runnable() {
			public void run() {
				prepareGUI();
			}
		});
	}

	/**
	 * Constructs a new ObjectInspector that is visible and initially inspects a
	 * given object.
	 * 
	 * @param object
	 *            the inspected object
	 */
	public ObjectInspector(Object object) {
		this();
		inspect(object);
	}

	// ---------------------------------------------------------------------------------------------------
	// Management of inspected objects. All inspected object are stored in as
	// objects of the ComboBox.
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Adds a new object for inspection into the object inspector.
	 * 
	 * @param object
	 *            the inspected object
	 * @param stopClass
	 *            the stop class for inspection. The stop class is a super of
	 *            the object's class. All properties and methods defined in this
	 *            class and its super classes will not be displayed in the
	 *            object inspector.
	 * @param alias
	 *            the alias name for the object. Alias is displayed in the
	 *            ComboBox for choosing the currently inspected object.
	 */
	public void inspect(Object object, Class<?> stopClass, String alias) {
		// construct a record about inspected object
		final ObjectItem item = new ObjectItem(object, stopClass, alias);
		// insert the record for inspection into the JComboBox (objectComboBox)
		// within the SWING EDT.
		JPAZUtilities.invokeAndWait(new Runnable() {
			public void run() {
				// check for duplicity - maybe the inserted object is
				// already here
				boolean found = false;
				for (int i = 0; i < objectComboBox.getItemCount(); i++) {
					if (((ObjectItem) objectComboBox.getItemAt(i)).object == item.object) {
						found = true;
						break;
					}
				}

				// if the object is not in the Object Inspector, we insert
				// it.
				if (!found) {
					objectComboBox.addItem(item);
				}
			}
		});
	}

	/**
	 * Adds a new object for inspection into the object inspector.
	 * 
	 * @param object
	 *            the inspected object.
	 */
	public void inspect(Object object) {
		inspect(object, null, null);
	}

	/**
	 * Adds a new object for inspection into the object inspector.
	 * 
	 * @param object
	 *            the inspected object.
	 * @param stopClass
	 *            the stop class for inspection. The stop class is a super of
	 *            the object's class. All properties and methods defined in this
	 *            class and its super classes will not be displayed in the
	 *            object inspector.
	 */
	public void inspect(Object object, Class<?> stopClass) {
		inspect(object, stopClass, null);
	}

	/**
	 * Adds a new object for inspection into the object inspector.
	 * 
	 * @param object
	 *            the inspected object.
	 * @param alias
	 *            the alias name for the object. Alias is displayed in the
	 *            ComboBox for choosing the currently inspected object.
	 */
	public void inspect(Object object, String alias) {
		inspect(object, null, alias);
	}

	/**
	 * Removes the object from inspection of the object inspector.
	 * 
	 * @param object
	 *            the desired object
	 */
	public void remove(final Object object) {
		if (object == null) {
			return;
		}

		JPAZUtilities.invokeAndWait(new Runnable() {
			public void run() {
				for (int i = 0; i < objectComboBox.getItemCount(); i++) {
					if (((ObjectItem) objectComboBox.getItemAt(i)).object == object) {
						objectComboBox.removeItemAt(i);
						break;
					}
				}
			}
		});
	}

	// ---------------------------------------------------------------------------------------------------
	// Methods for simple configuration
	// ---------------------------------------------------------------------------------------------------

	// ---------------------------------------------------------------------------------------------------
	// GUI settings
	// ---------------------------------------------------------------------------------------------------

	/**
	 * Sets the visibility of the Object Inspector window.
	 * 
	 * @param visible
	 *            if true, shows Object Inspector; otherwise, hides it.
	 */
	public void setVisible(final boolean visible) {
		JPAZUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.setVisible(visible);
			}
		});
	}

	/**
	 * Gets the visibility of the Object Inspector window.
	 * 
	 * @return true, if the Object Inspector is visible, false otherwise.
	 */
	public boolean isVisible() {
		return (Boolean) JPAZUtilities.invokeAndWait(new JPAZUtilities.Computable() {
			public Object compute() {
				return frame.isVisible();
			}
		});
	}

	/**
	 * Prepares and initialized the GUI window.
	 */
	private void prepareGUI() {
		// create frame
		frame = new JFrame("Object Inspector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 250, 430);

		// create object inspector panel
		oipanel = new OIPanel();
		objectComboBox = new JComboBox<ObjectItem>();
		objectComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ObjectItem item = (ObjectItem) objectComboBox.getSelectedItem();
				if (item != null)
					oipanel.setInspectedObject(item.object, item.stopClass);
				else
					oipanel.setInspectedObject(null, null);
			}
		});

		// set icon
		try {
			frame.setIconImage(ImageIO.read(this.getClass().getResource("/sk/upjs/jpaz2/images/binocular.png")));
		} catch (Exception e) {
			// nothing to do
		}

		// set initial position
		Rectangle screenBounds = JPAZUtilities.getScreenBounds();
		if (screenBounds != null) {
			frame.setLocation(screenBounds.x, screenBounds.y);
		}

		// set layout and insert components
		frame.setLayout(new BorderLayout());
		frame.add(objectComboBox, BorderLayout.PAGE_START);
		frame.add(oipanel, BorderLayout.CENTER);
		// set frame visible
		frame.setVisible(true);
	}
}
