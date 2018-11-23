package org.openntf.base.logback.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lotus.domino.Session;

// Leave this unused import, Need to record the dependency  

/**
 * Helper class to detect whether the current thread is running within an XPages
 * application
 * 
 * @author Karsten Lehmann
 */
public class XPagesDetector {

	/**
	 * Method to check whether we are in an XPages application
	 * 
	 * @return true, if XPages app
	 */
	public static boolean isXPagesContext() {
		try {
			Class<?> ctxClazz = Class
					.forName("com.ibm.domino.xsp.module.nsf.NotesContext");
			Method m = ctxClazz
					.getMethod("getCurrentUnchecked", (Class[]) null);
			Object ctxInstance = m.invoke((Object) null, (Object[]) null);
			if (ctxInstance != null) {
				if (getXspContextSession() != null)
					return true;
			}
			return false;
		} catch (NoClassDefFoundError e) {
			return false;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (SecurityException e) {
			return false;
		} catch (NoSuchMethodException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (InvocationTargetException e) {
			return false;
		}
	}

	public static Session getXspContextSession() {
		return getXspContextSession(false);
	}

	/**
	 * Returns the Session of the current XPages request
	 * 
	 * @return session, null in case of errors
	 */
	public static Session getXspContextSession(boolean asAdmin) {
		try {
			Class<?> ctxClazz = Class
					.forName("com.ibm.domino.xsp.module.nsf.NotesContext");
			Method m = ctxClazz.getMethod("getCurrent", (Class[]) null);
			Object ctxInstance = m.invoke((Object) null, (Object[]) null);
			Method m2;
			if (asAdmin)
				m2 = ctxInstance.getClass().getMethod(
						"getSessionAsSignerFullAdmin", (Class[]) null);
			else
				m2 = ctxInstance.getClass().getMethod("getCurrentSession",
						(Class[]) null);

			Session session = (Session) m2.invoke(ctxInstance, (Object[]) null);
			return session;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

	/**
	 * Returns the Session of the current XPages request
	 * 
	 * @return session, null in case of errors
	 */
//	public static DirectoryUser getXspContextDirectoryUser() {
//		try {
//
//			Class<?> ctxClazz = Class
//					.forName("javax.faces.context.FacesContext");
//			Method m = ctxClazz.getMethod("getCurrentInstance", (Class[]) null);
//			Object fcInstance = m.invoke((Object) null, (Object[]) null);
//
//			Class<?> xcClazz = Class
//					.forName("com.ibm.xsp.designer.context.ServletXSPContext");
//
//			Class<?>[] xcArg = new Class[1];
//			xcArg[0] = ctxClazz;
//
//			Method m2 = xcClazz.getMethod("getXSPContext", xcArg);
//			// 1st param null for static
//			Object xc = m2.invoke(null, fcInstance);
//
//			Method m3 = xcClazz.getMethod("getUser", (Class[]) null);
//
//			DirectoryUser diruser = (DirectoryUser) m3.invoke(xc,
//					(Object[]) null);
//			return diruser;
//
//		} catch (ClassNotFoundException e) {
//			return null;
//		} catch (SecurityException e) {
//			return null;
//		} catch (NoSuchMethodException e) {
//			return null;
//		} catch (IllegalArgumentException e) {
//			return null;
//		} catch (IllegalAccessException e) {
//			return null;
//		} catch (InvocationTargetException e) {
//			return null;
//		}
//	}
}
