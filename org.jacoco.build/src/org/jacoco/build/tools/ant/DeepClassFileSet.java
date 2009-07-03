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
package org.jacoco.build.tools.ant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Custom Ant type that extracts all class files that are referenced from a
 * given set of root classes. Unlike the original Ant type
 * <code>ClassFileSet</code> this task works recursively.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class DeepClassFileSet implements ResourceCollection {

	private static final String CLASSEXTENSION = ".class";

	private final List<ResourceCollection> delegates = new ArrayList<ResourceCollection>();

	private String rootclass;

	private Collection<Resource> resourceSet;

	// === ResourceCollection ===

	public boolean isFilesystemOnly() {
		return false;
	}

	public Iterator<?> iterator() {
		return getCalculateResources().iterator();
	}

	public int size() {
		return getCalculateResources().size();
	}

	// === Configuration API ===

	/**
	 * Adds the given resource collection to the collection of classes where
	 * dependent class files are extracted from.
	 * 
	 * @param collection
	 *            collection to add
	 */
	public void add(final ResourceCollection collection) {
		delegates.add(collection);
	}

	/**
	 * Sets the required name of the root class where dependency traversal
	 * starts.
	 * 
	 * @param rootclass
	 *            name of the root class in VM notation
	 */
	public void setRootclass(String rootclass) {
		this.rootclass = rootclass;
	}

	// === Internal dependency calculation ===

	private Collection<Resource> getCalculateResources() {
		if (resourceSet != null) {
			return resourceSet;
		}
		final Map<String, Resource> allClasses = createAllClassesMap();
		final Map<String, Resource> selectedClasses = new HashMap<String, Resource>();
		addClass(rootclass, selectedClasses, allClasses);
		return resourceSet = selectedClasses.values();
	}

	private void addClass(final String name,
			final Map<String, Resource> selectedClasses,
			final Map<String, Resource> allClasses) {
		if (selectedClasses.containsKey(name)) {
			// we already have this class:
			return;
		}
		final Resource resource = allClasses.get(name);
		if (resource == null) {
			// this class is out of scope:
			return;
		}
		selectedClasses.put(name, resource);
		for (final String dependency : getDependencies(resource)) {
			addClass(dependency, selectedClasses, allClasses);
		}
	}

	/**
	 * Reads and indexes all class of the underlying collections
	 * 
	 * @return VM class name to resource mapping
	 */
	private Map<String, Resource> createAllClassesMap() {
		final Map<String, Resource> map = new HashMap<String, Resource>();
		for (ResourceCollection c : delegates) {
			final Iterator<?> i = c.iterator();
			while (i.hasNext()) {
				final Resource resource = (Resource) i.next();
				final String name = resource.getName();
				if (resource.isExists() && name.endsWith(CLASSEXTENSION)) {
					String classname = name.substring(0, name.length()
							- CLASSEXTENSION.length());
					// On Windows we get back slashes:
					classname = classname.replace('\\', '/');
					map.put(classname, resource);
				}
			}
		}
		return map;
	}

	/**
	 * Finds all dependencies of the given class file.
	 * 
	 * @param resource
	 *            class file resource
	 * @return VM names of all dependent classes
	 */
	private Collection<String> getDependencies(Resource resource) {
		final Collection<String> dependencies = new HashSet<String>();
		Remapper remapper = new Remapper() {
			@Override
			public String map(String typeName) {
				dependencies.add(typeName);
				return typeName;
			}
		};
		try {
			final ClassReader reader = new ClassReader(resource
					.getInputStream());
			reader.accept(new RemappingClassAdapter(new EmptyVisitor(),
					remapper), 0);
		} catch (IOException e) {
			throw new BuildException(e);
		}
		return dependencies;
	}

}
