package sk.upjs.jpaz2.inspector;

import java.awt.*;
import java.io.File;
import java.util.*;
import javax.swing.table.*;

/**
 * The manager of supported class types. It contains list of supported types and
 * provides table renderers and editors for them.
 */
class OIClassSupporter {

	/**
	 * Additional information record for a class type
	 */
	private static class ClassRecord {
		/**
		 * Renderer
		 */
		TableCellRenderer renderer;
		/**
		 * External editor for read-only viewing
		 */
		TableCellEditor readOnlyEditor;
		/**
		 * Standard editor
		 */
		TableCellEditor editor;
	}

	/**
	 * The map for supported classes
	 */
	private Map<Class<?>, ClassRecord> supportedClasses = new HashMap<Class<?>, ClassRecord>();

	/**
	 * Add a supported class
	 * 
	 * @param aClass
	 *            the class type
	 * @param renderer
	 *            the table cell renderer
	 * @param readOnlyEditor
	 *            the external viewer
	 * @param editor
	 *            the standard editor
	 */
	public void addClassSupport(Class<?> aClass, TableCellRenderer renderer, TableCellEditor readOnlyEditor,
			TableCellEditor editor) {
		if (aClass == null)
			throw new NullPointerException("Class type must be given.");

		if (renderer == null)
			throw new NullPointerException("At least renderer must be non-null.");

		ClassRecord cr = new ClassRecord();
		cr.renderer = renderer;
		cr.readOnlyEditor = readOnlyEditor;
		cr.editor = editor;

		supportedClasses.put(aClass, cr);
	}

	/**
	 * Returns a class record for a specified class type
	 * 
	 * @param aClass
	 *            the class type
	 */
	private ClassRecord getClassRecord(Class<?> aClass) {
		ClassRecord result = null;
		while (aClass != null) {
			result = supportedClasses.get(aClass);
			if (result != null)
				break;

			aClass = aClass.getSuperclass();
		}

		return result;
	}

	/**
	 * Returns whether reading the class values (displaying the values) is
	 * supported by appropriate classes.
	 * 
	 * @param aClass
	 *            the class type
	 */
	public boolean isReadSupported(Class<?> aClass) {
		ClassRecord cr = getClassRecord(aClass);
		// since each class should have renderer, we check only existence of the
		// record
		return (cr != null);
	}

	/**
	 * Returns whether advanced reading the class values (displaying the values
	 * in separate read-only viewer) is supported by appropriate classes.
	 * 
	 * @param aClass
	 *            the class type
	 */
	public boolean isAdvancedReadSupported(Class<?> aClass) {
		ClassRecord cr = getClassRecord(aClass);
		if (cr == null)
			return false;
		else
			return (cr.readOnlyEditor != null);
	}

	/**
	 * Returns whether writing the class values (editing the values) is
	 * supported by appropriate classes.
	 * 
	 * @param aClass
	 *            the class type
	 */
	public boolean isWriteSupported(Class<?> aClass) {
		ClassRecord cr = getClassRecord(aClass);
		if (cr == null)
			return false;
		else
			return (cr.editor != null);
	}

	/**
	 * Gets a table cell renderer that renders the values of a specified class.
	 * 
	 * @param aClass
	 *            the class type
	 */
	public TableCellRenderer getTableCellRenderer(Class<?> aClass) {
		ClassRecord cr = getClassRecord(aClass);
		if (cr == null)
			return null;
		else
			return cr.renderer;
	}

	/**
	 * Gets a table cell editor for renders the values of a specified class.
	 * 
	 * @param aClass
	 *            the class type
	 * @param readOnly
	 *            true, if we ask for an read-only editor
	 */
	public TableCellEditor getTableCellEditor(Class<?> aClass, boolean readOnly) {
		ClassRecord cr = getClassRecord(aClass);
		if (cr == null)
			return null;
		else {
			TableCellEditor result = null;
			if (readOnly)
				result = cr.readOnlyEditor;
			else
				result = cr.editor;

			// special code for advanced cell support
			if (result instanceof OIEnumEditor)
				result = new OIEnumEditor(aClass);

			return result;
		}
	}

	/**
	 * Creates a basic ClassSupported filled with predefined supported classes.
	 */
	public static OIClassSupporter createDefaultClassSupporter() {
		OIClassSupporter result = new OIClassSupporter();

		// default "toString" renderer
		TableCellRenderer defaultRenderer = new OIDefaultCellRenderer();

		// visualization types
		result.addClassSupport(OIRowSeparatorValue.class, new OIRowSeparatorRenderer(), null, null);
		result.addClassSupport(OIPropertyNameValue.class, new OIPropertyNameRenderer(), null, null);
		result.addClassSupport(OIMethodNameValue.class, new OIMethodNameRenderer(), null, null);
		result.addClassSupport(OITypeValue.class, new OITypeRenderer(), null, null);
		result.addClassSupport(OIParameterValue.class, new OIParameterRenderer(), null, null);
		result.addClassSupport(OIMethodInvokerValue.class, new OIMethodInvokerRenderer(), null, null);
		result.addClassSupport(UnknownValue.class, defaultRenderer, null, null);

		// Boolean
		result.addClassSupport(Boolean.class, defaultRenderer, null, new OIBooleanCellEditor(true));
		result.addClassSupport(boolean.class, defaultRenderer, null, new OIBooleanCellEditor(false));

		// Numerical values
		result.addClassSupport(Integer.class, defaultRenderer, null, new OINumberEditor(Integer.class));
		result.addClassSupport(int.class, defaultRenderer, null, new OINumberEditor(int.class));
		result.addClassSupport(Byte.class, defaultRenderer, null, new OINumberEditor(Byte.class));
		result.addClassSupport(byte.class, defaultRenderer, null, new OINumberEditor(byte.class));
		result.addClassSupport(Long.class, defaultRenderer, null, new OINumberEditor(Long.class));
		result.addClassSupport(long.class, defaultRenderer, null, new OINumberEditor(long.class));
		result.addClassSupport(Float.class, defaultRenderer, null, new OINumberEditor(Float.class));
		result.addClassSupport(float.class, defaultRenderer, null, new OINumberEditor(float.class));
		result.addClassSupport(Double.class, defaultRenderer, null, new OINumberEditor(Double.class));
		result.addClassSupport(double.class, defaultRenderer, null, new OINumberEditor(double.class));

		// String
		result.addClassSupport(String.class, defaultRenderer, null, new OIStringEditor());

		// char
		result.addClassSupport(char.class, new OICharRenderer(), null, new OICharEditor());
		result.addClassSupport(Character.class, new OICharRenderer(), null, new OICharEditor());

		// enumeration
		result.addClassSupport(Enum.class, defaultRenderer, null, new OIEnumEditor());

		// color
		result.addClassSupport(Color.class, new OIColorRenderer(), null, new OIColorEditor());

		// file
		result.addClassSupport(File.class, defaultRenderer, null, new OIFileEditor());

		return result;
	}
}
