package sk.upjs.jpaz2.inspector;

import java.beans.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

/**
 * The class providing usefull static methods for method invocation.
 */
final class OIInvoker {

	/**
	 * The interface for invocation result handlers.
	 */
	public static interface ResultHandler {
		/**
		 * Handles the result of method invocation. It is called called in the
		 * thread where the method was invoked.
		 * 
		 * @param result
		 *            the result of the invocation
		 * @param thrownException
		 *            the exception thrown during execution of the method or
		 *            during method invocation
		 */
		public void handleResult(Object result, Exception thrownException);
	}

	/**
	 * Executor service providing threads for invoced methods
	 */
	private static ExecutorService executor = Executors.newCachedThreadPool();

	/**
	 * Invokes a setter of the property.
	 * 
	 * @param object
	 *            the object whose property is going to be changed
	 * @param p
	 *            the property descriptor of the property whose value is going
	 *            to change
	 * @param newValue
	 *            the new value of the property
	 * @return an object that allows to wait while the execution is not done
	 */
	public static Future<Void> changePropertyValue(final Object object,
			final PropertyDescriptor p, final Object newValue) {
		return executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				p.getWriteMethod().invoke(object, newValue);
				return null;
			}
		});
	}

	/**
	 * Invokes the method.
	 * 
	 * @param object
	 *            the object whose method is invoked
	 * @param method
	 *            the method to be invoked
	 * @param parameters
	 *            the parameters of the method
	 * @param resultProcessor
	 *            the object that process the returned value of the invocation
	 */
	public static void executeMethod(final Object object, final Method method,
			final Object[] parameters, final ResultHandler resultProcessor) {
		executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				Exception catchedException = null;
				Object result = null;
				try {
					result = method.invoke(object, parameters);
				} catch (Exception e) {
					catchedException = e;
				}

				if (resultProcessor != null)
					resultProcessor.handleResult(result, catchedException);

				return null;
			}
		});
	}
}
