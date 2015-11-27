/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Original Source: StringUtils class from Apache Commons Lang library.
 * Modifications copyright (C) 2015 Serdar Basegmez
 * 
 */
package org.openntf.base.logback.utils;

public class StringUtils {

	public static boolean isEmpty(final CharSequence s) {
		return s==null || s.length()==0;
	}

	public static boolean isNotEmpty(final CharSequence s) {
		return !isEmpty(s);
	}

	public static String defaultString(final String str, final String defaultStr) {
	    return str == null ? defaultStr : str;
	}

	public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
	    return isEmpty(str) ? defaultStr : str;
	}

	public static boolean equalsIgnoreCase(final CharSequence str1, CharSequence str2) {
	    if (str1 == null || str2 == null) {
	        return str1 == str2;
	    } else if (str1 == str2) {
	        return true;
	    } else if (str1.length() != str2.length()) {
	        return false;
	    } else {
	        return regionMatches(str1, true, 0, str2, 0, str1.length());
	    }
	}

	static boolean regionMatches(final CharSequence cs, final boolean ignoreCase, final int thisStart,
	        final CharSequence substring, final int start, final int length)    {
	    if (cs instanceof String && substring instanceof String) {
	        return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
	    } else {
	        int index1 = thisStart;
	        int index2 = start;
	        int tmpLen = length;
	
	        while (tmpLen-- > 0) {
	            char c1 = cs.charAt(index1++);
	            char c2 = substring.charAt(index2++);
	
	            if (c1 == c2) {
	                continue;
	            }
	
	            if (!ignoreCase) {
	                return false;
	            }
	
	            // The same check as in String.regionMatches():
	            if (Character.toUpperCase(c1) != Character.toUpperCase(c2)
	                    && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
	                return false;
	            }
	        }
	
	        return true;
	    }
	}

}
