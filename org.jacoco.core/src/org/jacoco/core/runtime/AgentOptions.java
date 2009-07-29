/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.runtime;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to create and parse options for the runtime agent. Options are
 * represented as a string in the following format:
 * 
 * <pre>
 *   key1=value1,key2=value2,key3=value3
 * </pre>
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class AgentOptions {

	/**
	 * Specifies the output file for execution data. Default is
	 * <code>jacoco.exec</code> in the working directory.
	 */
	public static final String FILE = "file";

	/**
	 * Specifies whether execution data should be appended to the output file.
	 * Default is <code>true</code>.
	 */
	public static final String MERGE = "merge";

	/**
	 * Wildcard expression for class loaders names for classes that should be
	 * excluded from code coverage. This means all classes loaded by a class
	 * loader which full qualified name matches this expression will be ignored
	 * for code coverage regardless of all other filtering settings. Default is
	 * <code>sun.reflect.DelegatingClassLoader</code>.
	 * 
	 * @see WildcardMatcher
	 */
	public static final String EXCLCLASSLOADER = "exclclassloader";

	private static final Collection<String> VALID_OPTIONS = Arrays.asList(FILE,
			MERGE, EXCLCLASSLOADER);

	private final Map<String, String> options;

	/**
	 * New instance with all values set to default.
	 */
	public AgentOptions() {
		this.options = new HashMap<String, String>();
	}

	/**
	 * New instance parsed from the given option string.
	 * 
	 * @param optionstr
	 *            string to parse or <code>null</code>
	 */
	public AgentOptions(final String optionstr) {
		this();
		if (optionstr != null && optionstr.length() > 0) {
			for (final String entry : optionstr.split(",")) {
				final int pos = entry.indexOf('=');
				if (pos == -1) {
					throw new IllegalArgumentException(
							"Invalid agent option syntax \"" + optionstr
									+ "\".");
				}
				final String key = entry.substring(0, pos);
				if (!VALID_OPTIONS.contains(key)) {
					throw new IllegalArgumentException(
							"Unknown agent option \"" + key + "\".");
				}
				options.put(key, entry.substring(pos + 1));
			}
		}
	}

	/**
	 * Returns the output file location.
	 * 
	 * @return output file location
	 */
	public String getFile() {
		final String file = options.get(FILE);
		return file == null ? "jacoco.exec" : file;
	}

	/**
	 * Sets the output file location.
	 * 
	 * @param file
	 *            output file location
	 */
	public void setFile(final String file) {
		options.put(FILE, file);
	}

	/**
	 * Returns whether the output should be merged with an existing file.
	 * 
	 * @return <code>true</code>, when the output should be merged
	 */
	public boolean getMerge() {
		final String merge = options.get(MERGE);
		return merge == null ? true : Boolean.parseBoolean(merge);
	}

	/**
	 * Sets whether the output should be merged with an existing file.
	 * 
	 * @param flag
	 *            <code>true</code>, when the output should be merged
	 */
	public void setMerge(final boolean flag) {
		options.put(MERGE, String.valueOf(flag));
	}

	/**
	 * Returns the wildcard expression for excluded class loaders.
	 * 
	 * @return expression for excluded class loaders
	 */
	public String getExclClassloader() {
		final String file = options.get(EXCLCLASSLOADER);
		return file == null ? "sun.reflect.DelegatingClassLoader" : file;
	}

	/**
	 * Sets the wildcard expression for excluded class loaders.
	 * 
	 * @param expression
	 *            expression for excluded class loaders
	 */
	public void setExclClassloader(final String expression) {
		options.put(EXCLCLASSLOADER, expression);
	}

	/**
	 * Creates a string representation that can be passed to the agent via the
	 * command line. Might be the empty string, if no options are set.
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final String key : VALID_OPTIONS) {
			final String value = options.get(key);
			if (value != null) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(key).append('=').append(value);
			}
		}
		return sb.toString();
	}

}
