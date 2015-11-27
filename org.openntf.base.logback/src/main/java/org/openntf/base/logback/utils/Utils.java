/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.base.logback.utils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import ch.qos.logback.classic.Level;
import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class Utils {

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");	
	
	public static boolean isInteger(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        
        try {
        	Integer.parseInt(value);
        	return true;
        } catch (Throwable t) {
        	return false;
        }
    }

    /**
	 * recycles a domino object instance
	 * 
	 * @param obj to recycle 
	 */
	public static void recycleObject(Object obj) {
		if (obj != null) {
			if (obj instanceof Base) {
				Base dominoObject = (Base) obj;
				try {
					dominoObject.recycle();
				} catch (Exception e) {
				}
			} else if (obj instanceof Collection) {
				Iterator<?> i = ((Collection<?>) obj).iterator();
				while (i.hasNext()) {
					Object colObj = i.next();
					recycleObject(colObj);
				}
			} else if (obj.getClass().isArray()) {
				try {
					Object[] objs = (Object[]) obj;
					for (Object ao : objs) {
						recycleObject(ao);
					}
				} catch (Throwable t) {
					// who cares?
				}
			}
		}
	}

	/**
	 * recycles multiple domino objects
	 *		
	 * @param objs
	 */
	public static void recycleObjects(Object... objs) {
		for ( Object obj : objs ) 
			recycleObject(obj);
	}

	/**
	 * Save a single date value into a document. 
	 * 
	 * @param doc Document for the field.
	 * @param fieldName Field Name for the field
	 * @param value Values accepted are DateTime, Calendar or Date
	 * @throws NotesException
	 */
	public static void saveDateField(Document doc, String fieldName, Object value) throws NotesException {
		
		DateTime someDate=null;
		Session session=getSession(doc);
		
		try {
			
			if(value instanceof DateTime) {
				doc.replaceItemValue(fieldName, value);
			} else if(value instanceof Calendar) {
				someDate=session.createDateTime((Calendar)value);
				doc.replaceItemValue(fieldName, someDate);
			} else if(value instanceof Date) {
				someDate=session.createDateTime((Date)value);
				doc.replaceItemValue(fieldName, someDate);
			}

		} finally {
			recycleObjects(someDate);
		}		
		
	}

	/**
	 * Extracts a Session from given Document.
	 * 	
	 * @param doc
	 * @return null if not found anything.
	 */
	public static Session getSession(Document doc) {
		Session session=null;
		Database parentDb=null;		
		View parentView=null;		
		
		try {
			if(doc.getParentDatabase()!=null) {
				parentDb=doc.getParentDatabase();
			} else if(doc.getParentView()!=null) {
				parentView=doc.getParentView();
				parentDb=parentView.getParent();
			}
			
			if(parentDb!=null) {
				session=parentDb.getParent();
			}

		} catch (NotesException ne) {
			// Cannot extract a session...
			throw new IllegalAccessError("Unable to obtain a Session object!"); 
		} finally {
			// recycleObjects(parentView, parentDb); May not be a good idea?
		}
		
		return session;
	}

	/**
	 * @return the clientVersion
	 */
	public static Vector<String> getClientVersion(Session session) {
		Vector<String> _clientVersion = new Vector<String>();
		try {
			String cver = session.getNotesVersion();
			if (cver != null) {
				if (cver.indexOf("|") > 0) {
					_clientVersion.addElement(cver.substring(0, cver.indexOf("|")));
					_clientVersion.addElement(cver.substring(cver.indexOf("|") + 1));
				} else {
					_clientVersion.addElement(cver);
				}
			}
		} catch (Exception e) {
		}
		return _clientVersion;
	}

	public static String getAccessLevel(Database database) {
		if(null==database) return "";
	
		try {
			switch (database.getCurrentAccessLevel()) {
			case 0:
				return "0: No Access";
			case 1:
				return "1: Depositor";
			case 2:
				return "2: Reader";
			case 3:
				return "3: Author";
			case 4:
				return "4: Editor";
			case 5:
				return "5: Designer";
			case 6:
				return "6: Manager";
			default:
				break;
			}
		} catch (NotesException e) {
		}
	
		return "N/A";
	}

	@SuppressWarnings("unchecked")
	public static Vector<String> getUserRoles(Session session) {
		try {
			return session.evaluate("@UserRoles");
		} catch (Exception e) {}
	
		return new Vector<String>();
	}

	public static String toSafeFolder(String path) {
		if(StringUtils.isEmpty(path)) return "";
		return path.endsWith( FILE_SEPARATOR ) ? path : ( path + FILE_SEPARATOR ); 
	}

	public static boolean isBoolean(String value) {
		return StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "false");
	}

	public static boolean isLogLevel(String value) {
		return Level.toLevel(value, null) != null;
	}
	
}
