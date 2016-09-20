package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.thoughtworks.paranamer.*;

import sk.upjs.jpaz2.inspector.OIInvoker.*;

@SuppressWarnings("serial")
class OIMethodInvocationFrame extends JFrame implements ResultHandler {

	// --------------------------------------------------------------------------------------------
	// ParameterItem
	// --------------------------------------------------------------------------------------------

	/**
	 * A data record representing a parameter and its value.
	 */
	private static class ParameterItem {
		/**
		 * Name of the parameter (null, if unavailable)
		 */
		String name;

		/**
		 * Type of the parameter
		 */
		Class<?> classType;

		/**
		 * The value of the parameter
		 */
		Object value;
	}

	// --------------------------------------------------------------------------------------------
	// ParametersTableModel
	// --------------------------------------------------------------------------------------------

	/**
	 * The table model for method parameters.
	 */
	private class ParametersTableModel extends OITableModel {

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			ParameterItem rowObject = (ParameterItem) rowBinding.get(rowIndex);
			if (columnIndex == 1) {
				rowObject.value = value;
				checkValidityOfParameters();
			}
		}

		public Object getValueAt(int rowIndex, int colIndex) {
			ParameterItem rowObject = (ParameterItem) rowBinding.get(rowIndex);
			if (colIndex == 0) {
				if (rowObject.name != null)
					return new OIParameterValue(rowObject.name,
							rowObject.classType);
				else
					return new OITypeValue(rowObject.classType);
			} else
				return rowObject.value;
		}

		@Override
		public Class<?> getCellType(int rowIndex, int colIndex) {
			ParameterItem rowObject = (ParameterItem) rowBinding.get(rowIndex);
			if (colIndex == 0) {
				if (rowObject.name != null)
					return OIParameterValue.class;
				else
					return OITypeValue.class;
			} else
				return rowObject.classType;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 1);
		}

		@Override
		public boolean isReadOnlyCell(int rowIndex, int columnIndex) {
			return (columnIndex == 0);
		}
	}

	// --------------------------------------------------------------------------------------------
	// OutputTableModel
	// --------------------------------------------------------------------------------------------

	/**
	 * The table model for invocation result (output). Table consists of 1 row.
	 */
	private class OutputTableModel extends OITableModel {

		/**
		 * The returned value of the invocation.
		 */
		Object result = new UnknownValue();

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return new OITypeValue(method.getReturnType());
			else
				return result;
		}

		@Override
		public Class<?> getCellType(int rowIndex, int colIndex) {
			if (colIndex == 0)
				return OITypeValue.class;
			else
				return method.getReturnType();
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public boolean isReadOnlyCell(int rowIndex, int colIndex) {
			return true;
		}

		/**
		 * Sets the result of an invocation.
		 * 
		 * @param result
		 *            the result
		 */
		public void setResult(Object result) {
			this.result = result;
			fireTableDataChanged();
		}
	}

	// --------------------------------------------------------------------------------------------
	// Class variables
	// --------------------------------------------------------------------------------------------

	/**
	 * Shared instance of Paranamer that reads parameter names from bytecode
	 */
	private static Paranamer parameterNameReader = new CachingParanamer(new AdaptiveParanamer());

	// --------------------------------------------------------------------------------------------
	// Instance variables
	// --------------------------------------------------------------------------------------------

	/**
	 * Object whose method is invoked
	 */
	private Object object;

	/**
	 * Method for invocation
	 */
	private Method method;

	/**
	 * True, if the method for invocation can provide parameter names
	 */
	private boolean methodSupportsParameterNames = false;

	/**
	 * Table model for method parameters
	 */
	private ParametersTableModel parametersTM;

	/**
	 * Table model for invocation result
	 */
	private OutputTableModel outputTM;

	/**
	 * Button for execution (invoking) the method
	 */
	private JButton executeButton;

	/**
	 * Table with parameters
	 */
	private OITable parametersTable;

	/**
	 * Table with the result
	 */
	private OITable outputTable;

	/**
	 * Manager of supported class types
	 */
	private OIClassSupporter classSupporter = OIClassSupporter
			.createDefaultClassSupporter();

	// --------------------------------------------------------------------------------------------
	// Constructor
	// --------------------------------------------------------------------------------------------

	/**
	 * Constructs and show the invocation frame.
	 * 
	 * @param object
	 *            the object whose method can be invoked by this frame
	 * @param method
	 *            the method that can be invoked
	 */
	public OIMethodInvocationFrame(Object object, Method method) {
		super();
		this.object = object;
		this.method = method;

		// check whether parameter names are available
		String[] parameterNames = parameterNameReader.lookupParameterNames(
				method, false);
		if ((parameterNames == null) || (parameterNames.length == 0))
			methodSupportsParameterNames = false;
		else
			methodSupportsParameterNames = true;

		// GUI actions
		initializeGUI();
		pack();
		setLocationRelativeTo(null);

		// set icon
		try {
			setIconImage(ImageIO.read(this.getClass().getResource(
					"/sk/upjs/jpaz2/images/execute.png")));
		} catch (Exception e) {
			// nothing to do
		}

		setVisible(true);
	}

	// --------------------------------------------------------------------------------------------
	// GUI setup and initialization
	// --------------------------------------------------------------------------------------------

	/**
	 * Initializes the GUI
	 */
	private void initializeGUI() {
		setTitle("Execute " + method.getName());
		getContentPane().setLayout(new BorderLayout());

		// information about the method
		constructInfoPart();

		// if there are parameters, create a table for editing values of the
		// parameters
		if (method.getParameterTypes().length > 0)
			constructParametersPart();

		// add the GUI components for starting execution and displaying the
		// result
		constructExecuteAndResultPart();

		// enable/disable execute button
		checkValidityOfParameters();
	}

	/**
	 * Constructs the window part (panel) with information about the object and
	 * invoked method.
	 */
	private void constructInfoPart() {
		JLabel label;

		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createBevelBorder(1));
		infoPanel.setPreferredSize(new Dimension(200, 65));
		infoPanel.setLayout(new BorderLayout());
		infoPanel.setBackground(new Color(255, 255, 222));

		JPanel titlePanel = new JPanel();
		titlePanel.setPreferredSize(new Dimension(70, 10));
		titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
		label = new JLabel("Object:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		titlePanel.add(label);
		label = new JLabel("Class:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		titlePanel.add(label);
		label = new JLabel("Method:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		titlePanel.add(label);

		titlePanel.setBackground(new Color(255, 255, 222));
		infoPanel.add(titlePanel, BorderLayout.LINE_START);

		JPanel valuesPanel = new JPanel();
		valuesPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
		valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.PAGE_AXIS));

		label = new JLabel(object.toString());
		label.putClientProperty("html.disable", true);
		label.setFont(label.getFont().deriveFont(0));
		valuesPanel.add(label);

		label = new JLabel(object.getClass().getSimpleName() + " ("
				+ object.getClass().getName() + ")");
		label.setFont(label.getFont().deriveFont(0));
		valuesPanel.add(label);

		label = new JLabel(method.getName());
		label.setFont(label.getFont().deriveFont(0));
		valuesPanel.add(label);

		valuesPanel.setBackground(new Color(255, 255, 222));
		infoPanel.add(valuesPanel, BorderLayout.CENTER);

		add(infoPanel, BorderLayout.PAGE_START);
	}

	/**
	 * Creates and initializes GUI component for editing method parameters.
	 */
	private void constructParametersPart() {
		// get parameter names if available
		String[] parameterNames = null;
		if (methodSupportsParameterNames)
			parameterNames = parameterNameReader.lookupParameterNames(method,
					false);

		// create table model
		parametersTM = new ParametersTableModel();
		parametersTM.rowBinding = new ArrayList<Object>();
		int idx = 0;
		for (Class<?> cl : method.getParameterTypes()) {
			ParameterItem pi = new ParameterItem();
			if (parameterNames != null)
				pi.name = parameterNames[idx];

			pi.classType = cl;
			parametersTM.rowBinding.add(pi);
			idx++;
		}

		// setup visual aspects of GUI for editing parameters
		if (methodSupportsParameterNames)
			parametersTM.setColumnNames("Name (Type)", "Value");
		else
			parametersTM.setColumnNames("Type", "Value");

		parametersTable = new OITable(parametersTM, classSupporter);
		parametersTable.setRowColors(new Color[] { new Color(222, 255, 222),
				new Color(191, 255, 191) });
		parametersTable.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("tableCellEditor".equals(evt.getPropertyName())) {
					if (parametersTable.isEditing())
						executeButton.setEnabled(false);
					else
						checkValidityOfParameters();
				}
			}
		});

		// add surrounding GUI components
		JPanel parametersPanel = new JPanel();
		parametersPanel.setPreferredSize(new Dimension(100, parametersTM
				.getRowCount() * parametersTable.getRowHeight() + 52));
		parametersPanel.setLayout(new BorderLayout());
		parametersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JLabel header = new JLabel("Parameters:");
		header.setFont(header.getFont().deriveFont(Font.BOLD));
		header.setBorder(BorderFactory.createEmptyBorder(0, 3, 5, 0));
		parametersPanel.add(header, BorderLayout.PAGE_START);
		parametersPanel.add(new JScrollPane(parametersTable),
				BorderLayout.CENTER);

		add(parametersPanel, BorderLayout.CENTER);
	}

	/**
	 * Add GUI components for starting method invocation and displaying the
	 * invocation result.
	 */
	private void constructExecuteAndResultPart() {
		// table model for the result
		outputTM = new OutputTableModel();
		outputTM.setColumnNames("Type", "Value");
		outputTable = new OITable(outputTM, classSupporter);
		outputTable.setRowColors(new Color[] { new Color(222, 255, 222) });

		// surrounding GUI components
		JPanel resultPanel = new JPanel();
		resultPanel.setPreferredSize(new Dimension(100, 102));
		resultPanel.setLayout(new BorderLayout());
		resultPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// create button for starting invocation
		executeButton = new JButton("Execute");
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeMethod();
			}
		});
		executeButton.setPreferredSize(new Dimension(100, 25));
		resultPanel.add(executeButton, BorderLayout.PAGE_START);

		// if the return type is not void, add table for displaying the
		// invocation result
		if (!void.class.equals(method.getReturnType())) {
			JLabel header = new JLabel("Result:");
			header.setFont(header.getFont().deriveFont(Font.BOLD));
			header.setPreferredSize(new Dimension(100, 15));
			header.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 0));
			resultPanel.add(header, BorderLayout.CENTER);

			JScrollPane scrollPane = new JScrollPane(outputTable);
			scrollPane.setPreferredSize(new Dimension(100, 43));
			resultPanel.add(scrollPane, BorderLayout.PAGE_END);
		} else {
			resultPanel.setPreferredSize(new Dimension(100, 35));
		}

		add(resultPanel, BorderLayout.PAGE_END);
	}

	// --------------------------------------------------------------------------------------------
	// Extra GUI functions
	// --------------------------------------------------------------------------------------------

	/**
	 * Shakes this frame in order to make it more visible for the user
	 */
	public void shake() {
		final int startX = getX();
		final int startY = getY();

		// create a shaking thread
		Thread shaker = new Thread() {
			public void run() {
				try {
					long startTime = System.currentTimeMillis();
					do {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								setLocation(startX
										+ (int) (3 - Math.random() * 6), startY
										+ (int) (3 - Math.random() * 6));
							}
						});
						Thread.sleep(50);
					} while (System.currentTimeMillis() - startTime < 250);

				} catch (Exception e) {
					// nothing to do
				} finally {
					// place this frame to its original location
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setLocation(startX, startY);
						}
					});
				}
			}
		};
		
		// start shaker thread
		shaker.start();
	}

	// --------------------------------------------------------------------------------------------
	// Method invocation
	// --------------------------------------------------------------------------------------------

	/**
	 * Check whether all parameters required for method invocation are filled.
	 */
	private void checkValidityOfParameters() {
		if (parametersTM == null) {
			executeButton.setEnabled(true);
			return;
		}

		boolean allParametersOK = true;
		for (Object parameter : parametersTM.rowBinding) {
			ParameterItem pi = (ParameterItem) parameter;
			// if value of a primitive parameter is null, null value is not
			// valid for it
			if (pi.classType.isPrimitive() && (pi.value == null))
				allParametersOK = false;
		}

		executeButton.setEnabled(allParametersOK);
	}

	/**
	 * Executes the method.
	 */
	private void executeMethod() {
		// disable GUI components
		executeButton.setEnabled(false);
		if (parametersTable != null)
			parametersTable.setEnabled(false);
		if (outputTable != null)
			outputTable.setEnabled(false);

		// create array of parameters
		Object[] parameters = new Object[method.getParameterTypes().length];
		for (int i = 0; i < parameters.length; i++)
			parameters[i] = ((ParameterItem) parametersTM.rowBinding.get(i)).value;

		// invoke the method
		OIInvoker.executeMethod(object, method, parameters, this);
	}

	/**
	 * Handles the method invocation result
	 * 
	 * @param result
	 *            the result of invoked method
	 * @param thrownException
	 *            the exception thrown by the method
	 */
	private void handleMethodInvocationResult(Object result,
			Exception thrownException) {
		// set result
		if (thrownException != null)
			outputTM.setResult(new UnknownValue());
		else
			outputTM.setResult(result);

		// show message if exception thrown
		if (thrownException != null) {
			String message = "";
			if (thrownException instanceof InvocationTargetException)
				message = thrownException.getCause().toString();
			else
				message = thrownException.toString();

			JOptionPane.showMessageDialog(this,
					"Thrown exception:\n" + message, "Exception",
					JOptionPane.ERROR_MESSAGE);
		}

		// enable GUI components
		checkValidityOfParameters();

		if (parametersTable != null)
			parametersTable.setEnabled(true);
		if (outputTable != null)
			outputTable.setEnabled(true);
	}

	public void handleResult(final Object result,
			final Exception thrownException) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				handleMethodInvocationResult(result, thrownException);
			}
		});
	}

	// ---------------------------------------------------------------------------------------------------
	// Smart frame location
	// ---------------------------------------------------------------------------------------------------

	static void moveToSmartLocation(Frame frame, Frame dockingFrame) {
		// if one of parameters is null, there is nothing to do
		if ((dockingFrame == null) || (frame == null))
			return;

		// we try to get bounds of the docking screen
		Rectangle dockingScreenBounds = null;
		try {
			dockingScreenBounds = dockingFrame.getGraphicsConfiguration()
					.getBounds();
		} catch (Exception e) {
			return;
		}

		// we list all visible frames that are in the same screen as the docking
		// frame
		ArrayList<Frame> consideredFrames = new ArrayList<Frame>();
		for (Frame f : Frame.getFrames()) {
			// we are interested only in visible frames excluding the located
			// frame
			if ((!f.isShowing()) || (f == frame))
				continue;

			// we exclude frames that are not in the same screen as the docking
			// frame
			if (f.getBounds().intersects(dockingScreenBounds))
				consideredFrames.add(f);
		}

		// look for position with the smallest number of intersections with
		// considered frames

		// first, create a list of all possible top-left corner coordinates
		int fWidth = frame.getWidth();
		int fHeight = frame.getHeight();
		int[] xCandidates = new int[2 * consideredFrames.size() + 2];
		int[] yCandidates = new int[2 * consideredFrames.size() + 2];

		xCandidates[0] = (int) dockingScreenBounds.getMinX() + 1;
		xCandidates[1] = (int) (dockingScreenBounds.getMaxX() - fWidth - 1);
		yCandidates[0] = (int) dockingScreenBounds.getMinY() + 1;
		yCandidates[1] = (int) (dockingScreenBounds.getMaxY() - fHeight - 1);
		int idx = 2;
		for (Frame f : consideredFrames) {
			xCandidates[idx] = f.getX() - fWidth - 1;
			yCandidates[idx] = f.getY() - fHeight - 1;
			idx++;
			xCandidates[idx] = f.getX() + f.getWidth() + 1;
			yCandidates[idx] = f.getY() + f.getHeight() + 1;
			idx++;
		}

		Rectangle[] frameBounds = new Rectangle[consideredFrames.size()];
		for (int i = 0; i < consideredFrames.size(); i++)
			frameBounds[i] = consideredFrames.get(i).getBounds();

		// check "cost" of all possible positions
		int lowestCover = Integer.MAX_VALUE;
		int bestDistanceOfLowestCover = Integer.MAX_VALUE;
		Point location = null;
		Rectangle currentFrameBounds = new Rectangle(0, 0, fWidth, fHeight);
		for (int i = 0; i < xCandidates.length; i++)
			for (int j = 0; j < yCandidates.length; j++) {
				currentFrameBounds.setLocation(xCandidates[i], yCandidates[j]);
				if (!dockingScreenBounds.contains(currentFrameBounds))
					continue;

				int currentInfoCover = 0;
				for (Rectangle r : frameBounds) {
					if (r.intersects(currentFrameBounds)) {
						Rectangle infoCoverRect = r
								.intersection(currentFrameBounds);
						currentInfoCover += infoCoverRect.width
								* infoCoverRect.height;
					}
				}

				int currentDistance = Math.abs(dockingFrame.getX()
						- currentFrameBounds.x)
						+ Math.abs(dockingFrame.getY() - currentFrameBounds.y);

				if ((currentInfoCover < lowestCover)
						|| ((currentInfoCover == lowestCover) && (currentDistance < bestDistanceOfLowestCover))) {
					location = currentFrameBounds.getLocation();
					lowestCover = currentInfoCover;
					bestDistanceOfLowestCover = currentDistance;
				}
			}

		if (location != null)
			frame.setLocation(location);
	}
}
