package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import sk.upjs.jpaz2.JPAZUtilities;

/**
 * The one object inspector. OIPanel is the crucial class of the Object
 * Inspector system. It is responsible for inspection of one choosen object.
 * 
 */
@SuppressWarnings("serial")
public class OIPanel extends JPanel {

	// --------------------------------------------------------------------------------------------
	// PropertyItem
	// --------------------------------------------------------------------------------------------

	/**
	 * Data record representing a property of the inspected object.
	 */
	private static class PropertyItem {
		/**
		 * Property name
		 */
		String name;

		/**
		 * Last known value of the property for inspected object
		 */
		Object value;

		/**
		 * Introspection property descriptor
		 */
		PropertyDescriptor propertyDescriptor;

		/**
		 * Whether this property is supported by Object Inspector
		 */
		boolean isSupported = false;

		/**
		 * Indicates whether the property is read-only, i.e., there is no
		 * available setter-method
		 */
		boolean isReadOnly = true;

		/**
		 * Constructs a property record with a given name
		 * 
		 * @param name
		 *            the name of the property
		 */
		PropertyItem(String name) {
			this.name = name;
		}
	}

	// --------------------------------------------------------------------------------------------
	// MethodItem
	// --------------------------------------------------------------------------------------------

	/**
	 * Data record representing a method of the inspected object.
	 */
	private static class MethodItem {
		/**
		 * Property name
		 */
		String name;

		/**
		 * Introspection property descriptor
		 */
		MethodDescriptor methodDescriptor;

		/**
		 * Whether this property is supported by Object Inspector
		 */
		boolean isSupported = false;

		/**
		 * A frame created for invoking this method
		 */
		OIMethodInvocationFrame frame = null;

		/**
		 * Constructs a property record with a given name
		 * 
		 * @param name
		 *            the name of the property
		 */
		MethodItem(String name) {
			this.name = name;
		}
	}

	// --------------------------------------------------------------------------------------------
	// ClassItem
	// --------------------------------------------------------------------------------------------

	/**
	 * Data record representing a class in the class hierarchy of the inspected
	 * object.
	 */
	private static class ClassItem {
		/**
		 * Class object of this class record
		 */
		Class<?> cl;

		/**
		 * Class name that is shown in GUI components
		 */
		String className;

		/**
		 * List of properties defined in this class
		 */
		ArrayList<PropertyItem> properties = new ArrayList<PropertyItem>();

		/**
		 * List of methods defined in this class
		 */
		ArrayList<MethodItem> methods = new ArrayList<MethodItem>();

		/**
		 * Indicates whether information about properties defined in this class
		 * is collapsed
		 */
		boolean propertiesCollapsed = false;

		/**
		 * Indicates whether information about methods defined in this class is
		 * collapsed
		 */
		boolean methodsCollapsed = false;

		/**
		 * The number of supported properties
		 */
		int supportedPropertiesCount = 0;

		/**
		 * The number of supported methods
		 */
		int supportedMethodsCount = 0;

		/**
		 * Construct a data record.
		 * 
		 * @param objectClass
		 *            the class represented by this data record
		 */
		public ClassItem(Class<?> objectClass) {
			cl = objectClass;
			className = cl.getSimpleName();
			propertiesCollapsed = false;
		}
	}

	// --------------------------------------------------------------------------------------------
	// PropertiesTableModel
	// --------------------------------------------------------------------------------------------

	/**
	 * Table model for properties of the inspected object.
	 */
	private class PropertiesTableModel extends OITableModel {

		public Object getValueAt(int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// a separating row (class name)
			if (rowObject instanceof ClassItem) {
				ClassItem ci = (ClassItem) rowObject;

				OIRowSeparatorValue result = new OIRowSeparatorValue();
				result.collapsed = ci.propertiesCollapsed;

				result.text = ci.className;
				if ("".equals(result.text))
					result.text = ci.cl.getName();

				result.text += ": " + ci.supportedPropertiesCount;

				result.active = (ci.supportedPropertiesCount > 0);

				return result;
			}

			// a property row
			if (rowObject instanceof PropertyItem) {
				PropertyItem pi = (PropertyItem) rowObject;

				// name of the property row
				if (colIndex == 0)
					return new OIPropertyNameValue(pi.propertyDescriptor);

				// value of the property row
				if (colIndex == 1)
					return pi.value;

				return null;
			}

			return null;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// only property values can be changed
			if ((rowObject instanceof PropertyItem) && (colIndex == 1)) {
				PropertyItem pi = (PropertyItem) rowObject;
				// if it is not a read-only property, we change its value
				if (!pi.isReadOnly)
					OIInvoker.changePropertyValue(inspectedObject,
							pi.propertyDescriptor, value);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// only some property items can be edited
			if (rowObject instanceof PropertyItem) {
				PropertyItem pi = (PropertyItem) rowObject;
				if (colIndex == 1) {
					// cell is editable only if there is a support for its
					// read-only viewing or
					// there is a support for its editing and the the value is
					// not read only
					Class<?> propertyClass = pi.propertyDescriptor
							.getPropertyType();

					// if the property value is not known, the property is
					// surelly not editable
					if (pi.value instanceof UnknownValue)
						return false;

					return classSupporter
							.isAdvancedReadSupported(propertyClass)
							|| (!pi.isReadOnly && classSupporter
									.isWriteSupported(propertyClass));
				}
			}

			return false;
		}

		@Override
		public Class<?> getCellType(int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// retrieving a value from separating row
			if (rowObject instanceof ClassItem)
				return OIRowSeparatorValue.class;

			// retrieving a value from property row
			if (rowObject instanceof PropertyItem) {
				PropertyItem pi = (PropertyItem) rowObject;

				// retrieving name of the property row
				if (colIndex == 0)
					return OIPropertyNameValue.class;

				// retrieving value of the property row
				if (colIndex == 1)
					return pi.propertyDescriptor.getPropertyType();

				return null;
			}

			return null;
		}

		@Override
		public boolean isReadOnlyCell(int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// retrieving a value from separating row
			if (rowObject instanceof ClassItem)
				return true;

			// retrieving a value from property row
			if (rowObject instanceof PropertyItem) {
				PropertyItem pi = (PropertyItem) rowObject;

				// retrieving name of the property row
				if (colIndex == 0)
					return true;

				// retrieving value of the property row
				if (colIndex == 1)
					return pi.isReadOnly;

				return true;
			}

			return true;
		}

		@Override
		protected void mouseClicked(MouseEvent e, int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// handle click on class row (collapse or expand properties)
			if ((e.getButton() == MouseEvent.BUTTON1)
					&& (rowObject instanceof ClassItem)) {
				ClassItem ci = (ClassItem) rowObject;
				ci.propertiesCollapsed = !ci.propertiesCollapsed;
				updatePropertiesRowBinding();
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// MethodsTableModel
	// --------------------------------------------------------------------------------------------

	/**
	 * Table model for methods of the inspected object.
	 */
	private class MethodsTableModel extends OITableModel {

		public Object getValueAt(int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// a separating row
			if (rowObject instanceof ClassItem) {
				ClassItem ci = (ClassItem) rowObject;

				OIRowSeparatorValue result = new OIRowSeparatorValue();
				result.collapsed = ci.methodsCollapsed;

				result.text = ci.className;
				if ("".equals(result.text))
					result.text = ci.cl.getName();

				result.text += ": " + ci.supportedMethodsCount;

				result.active = (ci.supportedMethodsCount > 0);

				return result;
			}

			// a method row
			if (rowObject instanceof MethodItem) {
				MethodItem mi = (MethodItem) rowObject;
				if (colIndex == 0)
					return new OIMethodNameValue(
							mi.methodDescriptor.getMethod());

				if (colIndex == 1)
					return new OIMethodInvokerValue();

				return null;
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int colIndex) {
			return false;
		}

		@Override
		public Class<?> getCellType(int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// a separating row
			if (rowObject instanceof ClassItem)
				return OIRowSeparatorValue.class;

			// a method row
			if (rowObject instanceof MethodItem) {
				if (colIndex == 0)
					return OIMethodNameValue.class;

				if (colIndex == 1)
					return OIMethodInvokerValue.class;

				return null;
			}

			return null;
		}

		@Override
		protected void mouseClicked(MouseEvent e, int rowIndex, int colIndex) {
			Object rowObject = rowBinding.get(rowIndex);

			// handle click on class row (collapse or expand properties)
			if ((e.getButton() == MouseEvent.BUTTON1)
					&& (rowObject instanceof ClassItem)) {
				ClassItem ci = (ClassItem) rowObject;
				ci.methodsCollapsed = !ci.methodsCollapsed;
				updateMethodsRowBinding();
			}

			// handle double-click on method name to show the invocation frame
			if ((e.getButton() == MouseEvent.BUTTON1)
					&& (rowObject instanceof MethodItem)) {
				if (((colIndex == 0) && (e.getClickCount() > 1))
						|| (colIndex == 1)) {
					MethodItem mi = (MethodItem) rowObject;

					if (mi.frame == null) {
						mi.frame = new OIMethodInvocationFrame(inspectedObject,
								mi.methodDescriptor.getMethod());

						if (JPAZUtilities.isSmartLocationEnabled()) {
							try {
								OIMethodInvocationFrame.moveToSmartLocation(
										mi.frame, (Frame) SwingUtilities
												.getRoot((Component) e
														.getSource()));
							} catch (Exception ex) {
								// if smart location fails, nothing to do
							}
						}
					}

					mi.frame.setVisible(true);

					if (JPAZUtilities.isWindowShakingEnabled())
						mi.frame.shake();
				}
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// Instance variables
	// --------------------------------------------------------------------------------------------

	/**
	 * Currently inspected object
	 */
	private Object inspectedObject;

	/**
	 * Stop class for inspection of the object. All methods and properties
	 * defined in super classes of this class are not considered (displayed)
	 */
	private Class<?> inspectionStopClass = null;

	/**
	 * List of classes that form the hierarchy of the class of currently
	 * inspected object
	 */
	java.util.List<ClassItem> classes = new ArrayList<ClassItem>();

	/**
	 * Table model for properties of the inspected object
	 */
	private PropertiesTableModel propertiesTM;

	/**
	 * Table model for methods of the inspected object
	 */
	private MethodsTableModel methodsTM;

	/**
	 * Tabs - a GUI component
	 */
	private JTabbedPane tabbedPane;

	/**
	 * Thread responsible for periodical updates of current property values
	 */
	private PropertyUpdateThread propertyUpdateThread;

	/**
	 * Manager of supported class types
	 */
	private OIClassSupporter classSupporter = OIClassSupporter
			.createDefaultClassSupporter();

	// --------------------------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------------------------

	/**
	 * Constructs a new OI Panel for inspecting one object
	 */
	public OIPanel() {
		tabbedPane = new JTabbedPane();
		inspectedObject = this;

		constructPropertiesUI();
		constructMethodsUI();

		setLayout(new GridLayout(1, 1));
		this.add(tabbedPane);
	}

	// --------------------------------------------------------------------------------------------
	// Management of inspected object
	// --------------------------------------------------------------------------------------------

	/**
	 * Sets the inspected object.
	 * 
	 * @param object
	 *            the object to be inspected
	 * @param stopClass
	 *            the class where the inspection stops
	 */
	public void setInspectedObject(Object object, Class<?> stopClass) {
		// checked whether there is something new
		if ((this.inspectedObject == object)
				&& (this.inspectionStopClass == stopClass))
			return;

		// stop periodical update of values
		stopPeriodicUpdate();

		// set new inspected object and stop class
		this.inspectedObject = object;
		this.inspectionStopClass = stopClass;

		// analyze inspected object
		analyseInspectedObject();

		// start periodical update of property values
		if (this.inspectedObject != null)
			startPeriodicUpdate();
	}

	/**
	 * Gets the currently inspected object.
	 * 
	 * @return the object displayed in the panel.
	 */
	public Object getInspectedObject() {
		return this.inspectedObject;
	}

	/**
	 * Gets the stop class of the currently inspected object
	 * 
	 * @return the stop class.
	 */
	public Class<?> getStopClass() {
		return this.inspectionStopClass;
	}

	// --------------------------------------------------------------------------------------------
	// GUI building
	// --------------------------------------------------------------------------------------------

	/**
	 * Builds and initializes tab for properties
	 */
	private void constructPropertiesUI() {
		// define columns for property editor columns
		propertiesTM = new PropertiesTableModel();
		propertiesTM.setColumnNames("Property", "Value");
		OITable propertiesTable = new OITable(propertiesTM, classSupporter);
		propertiesTable.setRowColors(new Color[] { new Color(255, 255, 191),
				new Color(255, 255, 222) });

		// set columns widths
		TableColumnModel cm = propertiesTable.getColumnModel();
		int width = cm.getColumn(0).getWidth() + cm.getColumn(1).getWidth();
		cm.getColumn(0).setPreferredWidth((4 * width) / 5);
		cm.getColumn(1).setPreferredWidth(width / 5);

		// create panel for tab
		JPanel propertiesPanel = new JPanel(new GridLayout(1, 1));
		propertiesPanel.add(new JScrollPane(propertiesTable));
		tabbedPane.addTab("Properties", propertiesPanel);
	}

	/**
	 * Builds and initializes tab for methods
	 */
	private void constructMethodsUI() {
		methodsTM = new MethodsTableModel();
		methodsTM.setColumnNames("Method", "");
		OITable methodsTable = new OITable(methodsTM, classSupporter);
		methodsTable.setRowColors(new Color[] { new Color(255, 255, 191),
				new Color(255, 255, 222) });
		methodsTable.getColumnModel().getColumn(1).setMinWidth(25);
		methodsTable.getColumnModel().getColumn(1).setMaxWidth(25);
		methodsTable.getColumnModel().getColumn(1).setResizable(false);

		// create panel for tab
		JPanel methodsPanel = new JPanel(new GridLayout(1, 1));
		methodsPanel.add(new JScrollPane(methodsTable));
		tabbedPane.addTab("Methods", methodsPanel);
	}

	// --------------------------------------------------------------------------------------------
	// Methods for updating the view of inspected object
	// --------------------------------------------------------------------------------------------

	/**
	 * Creates new mapping for rows of the properties table model
	 */
	private void updatePropertiesRowBinding() {
		// empty old row binding
		propertiesTM.rowBinding = new ArrayList<Object>();

		// analyze each class in the hierarchy
		for (ClassItem ci : classes) {
			propertiesTM.rowBinding.add(ci);
			if (!ci.propertiesCollapsed)
				for (PropertyItem pi : ci.properties)
					if (pi.isSupported)
						propertiesTM.rowBinding.add(pi);
		}

		// notify change
		propertiesTM.fireTableDataChanged();
	}

	/**
	 * Creates new mapping for rows of the methods table model
	 */
	private void updateMethodsRowBinding() {
		// empty old row binding
		methodsTM.rowBinding = new ArrayList<Object>();

		// analyze each class in the hierarchy
		for (ClassItem ci : classes) {
			methodsTM.rowBinding.add(ci);
			if (!ci.methodsCollapsed)
				for (MethodItem mi : ci.methods)
					if (mi.isSupported)
						methodsTM.rowBinding.add(mi);
		}

		// notify change
		methodsTM.fireTableDataChanged();
	}

	// --------------------------------------------------------------------------------------------
	// Analysis of the inspected object
	// --------------------------------------------------------------------------------------------

	/**
	 * Realizes the basic analysis of the inspected object
	 */
	private void analyseInspectedObject() {
		// read whole class structure
		readClassStructure();
		// filter unsupported items (properties and methods)
		filterClassStructure();

		// change binding
		updatePropertiesRowBinding();
		updateMethodsRowBinding();
	}

	/**
	 * Constructs a basic structural information about the inspected object
	 */
	private void readClassStructure() {
		// empty old list of classes
		classes.clear();

		if (inspectedObject == null)
			return;

		// read structure
		Class<?> objectClass = inspectedObject.getClass();

		// load bean info
		BeanInfo bInfo = null;
		try {
			bInfo = Introspector.getBeanInfo(objectClass, inspectionStopClass);
		} catch (Exception e) {
			return;
		}

		// create inheritance list
		while ((objectClass != null) && (objectClass != inspectionStopClass)) {
			ClassItem ci = new ClassItem(objectClass);
			// only the first class is initially expanded
			ci.propertiesCollapsed = (classes.size() > 0);
			ci.methodsCollapsed = (classes.size() > 0);

			classes.add(ci);
			objectClass = objectClass.getSuperclass();
		}

		// distribute properties to appropriate classes
		PropertyDescriptor[] properties = bInfo.getPropertyDescriptors();
		if (properties != null) {
			for (PropertyDescriptor pd : properties) {
				// process only such properties that provide at least reading
				// capability
				if (pd.getReadMethod() != null) {
					// create and initialize a data record for the property
					PropertyItem pi = new PropertyItem(pd.getDisplayName());
					pi.propertyDescriptor = pd;
					pi.isReadOnly = (pd.getWriteMethod() == null);

					// add the property record to the proper class record
					Class<?> declaringClass = pd.getReadMethod()
							.getDeclaringClass();
					for (ClassItem ci : classes)
						if (ci.cl.equals(declaringClass)) {
							ci.properties.add(pi);
							break;
						}
				}
			}
		}

		// distribute methods to appropriate classes
		MethodDescriptor[] methods = bInfo.getMethodDescriptors();
		if (methods != null) {
			for (MethodDescriptor md : methods) {
				// create and initialize a data record for the method
				MethodItem mi = new MethodItem(md.getDisplayName());
				mi.methodDescriptor = md;

				// add the method record to the proper class record
				Class<?> declaringClass = md.getMethod().getDeclaringClass();
				for (ClassItem ci : classes)
					if (ci.cl.equals(declaringClass)) {
						ci.methods.add(mi);
						break;
					}
			}
		}

		// order properties and methods of each class in alphabetical order
		for (ClassItem ci : classes) {
			Collections.sort(ci.properties, new Comparator<PropertyItem>() {
				public int compare(PropertyItem o1, PropertyItem o2) {
					return o1.name.compareTo(o2.name);
				}
			});

			Collections.sort(ci.methods, new Comparator<MethodItem>() {
				public int compare(MethodItem o1, MethodItem o2) {
					return o1.name.compareTo(o2.name);
				}
			});
		}
	}

	/**
	 * Decides for each item (method or property) whether it is supported by the
	 * object inspector.
	 */
	private void filterClassStructure() {
		int propertiesActiveClasses = 0;
		int methodsActiveClasses = 0;

		for (ClassItem ci : classes) {
			// check properties - the property is supported if it is not an
			// indexed property
			// and there is a support for reading (displaying) the property
			// value
			for (PropertyItem pi : ci.properties) {
				pi.isSupported = (!(pi.propertyDescriptor instanceof IndexedPropertyDescriptor))
						&& (classSupporter
								.isReadSupported(pi.propertyDescriptor
										.getPropertyType()));

				if (pi.isSupported)
					ci.supportedPropertiesCount++;
			}

			// check methods
			for (MethodItem mi : ci.methods) {
				// checked whether all parameters are editable
				boolean allParametersOK = true;
				Class<?>[] parameterTypes = mi.methodDescriptor.getMethod()
						.getParameterTypes();
				if (parameterTypes != null)
					for (Class<?> parameterType : parameterTypes)
						if (!classSupporter.isWriteSupported(parameterType))
							allParametersOK = false;

				// check whether the result of the method can be displayed or is
				// void
				Class<?> returnType = mi.methodDescriptor.getMethod()
						.getReturnType();
				boolean resultOK = (Void.class.equals(returnType)
						|| void.class.equals(returnType) || classSupporter
						.isReadSupported(returnType));

				// decide about the support
				mi.isSupported = allParametersOK && resultOK;

				if (mi.isSupported)
					ci.supportedMethodsCount++;
			}

			// set expanded/collapsed in such a way that only the first active
			// class is expanded
			ci.propertiesCollapsed = true;
			ci.methodsCollapsed = true;

			if (ci.supportedPropertiesCount > 0) {
				ci.propertiesCollapsed = (propertiesActiveClasses > 0);
				propertiesActiveClasses++;
			}

			if (ci.supportedMethodsCount > 0) {
				ci.methodsCollapsed = (methodsActiveClasses > 0);
				methodsActiveClasses++;
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// Management of the thread updating current values of properties of the
	// inspected object
	// --------------------------------------------------------------------------------------------

	/**
	 * Restarts the periodic update
	 */
	private void startPeriodicUpdate() {
		stopPeriodicUpdate();

		// list properties that will be updated - only supported properties are
		// updated
		ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
		for (ClassItem ci : classes)
			for (PropertyItem pi : ci.properties)
				if (pi.isSupported)
					properties.add(pi.propertyDescriptor);

		// creates the update thread
		propertyUpdateThread = new PropertyUpdateThread(inspectedObject,
				properties) {
			@Override
			public void updateValuesInSwing(
					Map<PropertyDescriptor, Object> values) {
				// move current values from thread to OIPanel
				// this method is always invoked in EDT, i.e., no
				// synchronization is necessary
				for (ClassItem ci : classes)
					for (PropertyItem pi : ci.properties)
						if (values.containsKey(pi.propertyDescriptor))
							pi.value = values.get(pi.propertyDescriptor);

				propertiesTM.fireTableDataChanged();
			}
		};

		// start the thread
		propertyUpdateThread.start();
	}

	/**
	 * Stops periodic update
	 */
	private void stopPeriodicUpdate() {
		if (propertyUpdateThread != null) {
			propertyUpdateThread.interrupt();
			propertyUpdateThread = null;
		}
	}
}
