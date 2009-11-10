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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.instr.GeneratorConstants;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This {@link IRuntime} implementation makes the execution data available
 * through a special entry of the type {@link Map} in the
 * {@link System#getProperties()} hash table. The advantage is, that the
 * instrumented classes do not get dependencies to other classes than the JRE
 * library itself.
 * 
 * This runtime may cause problems in environments with security restrictions,
 * in applications that replace the system properties or in applications that
 * fail if non-String values are placed in the system properties.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SystemPropertiesRuntime extends AbstractRuntime {

	private static final String KEYPREFIX = "jacoco-";

	private final String key;

	private final Map<Long, boolean[]> dataAccess = new Map<Long, boolean[]>() {

		public boolean[] get(final Object key) {
			final Long id = (Long) key;
			synchronized (store) {
				final boolean[] data = store.getData(id);
				if (data == null) {
					throw new IllegalStateException(String.format(
							"Unknown class id %x.", id));
				}
				return data;
			}
		}

		public void clear() {
			throw new UnsupportedOperationException();
		}

		public boolean containsKey(final Object key) {
			throw new UnsupportedOperationException();
		}

		public boolean containsValue(final Object value) {
			throw new UnsupportedOperationException();
		}

		public Set<Entry<Long, boolean[]>> entrySet() {
			throw new UnsupportedOperationException();
		}

		public boolean isEmpty() {
			throw new UnsupportedOperationException();
		}

		public Set<Long> keySet() {
			throw new UnsupportedOperationException();
		}

		public boolean[] put(final Long key, final boolean[] value) {
			throw new UnsupportedOperationException();
		}

		public void putAll(final Map<? extends Long, ? extends boolean[]> t) {
			throw new UnsupportedOperationException();
		}

		public boolean[] remove(final Object key) {
			throw new UnsupportedOperationException();
		}

		public Collection<boolean[]> values() {
			throw new UnsupportedOperationException();
		}

		public int size() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Creates a new runtime.
	 */
	public SystemPropertiesRuntime() {
		this.key = KEYPREFIX + hashCode();
	}

	public int generateDataAccessor(final long classid,
			final GeneratorAdapter gen) {

		gen.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
				"getProperties", "()Ljava/util/Properties;");

		// Stack[0]: Ljava/util/Properties;

		gen.push(key);

		// Stack[1]: Ljava/lang/String;
		// Stack[0]: Ljava/util/Properties;

		gen.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties",
				"get", "(Ljava/lang/Object;)Ljava/lang/Object;");

		// Stack[0]: Ljava/lang/Object;

		gen.visitTypeInsn(Opcodes.CHECKCAST, "java/util/Map");

		// Stack[0]: Ljava/util/Map;

		gen.push(classid);

		// Stack[2]: J
		// Stack[1]: .
		// Stack[0]: Ljava/util/Map;

		gen.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");

		// Stack[1]: Ljava/lang/Long;
		// Stack[0]: Ljava/util/Map;

		gen.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get",
				"(Ljava/lang/Object;)Ljava/lang/Object;");

		// Stack[0]: Ljava/lang/Object;

		gen.checkCast(GeneratorConstants.PROBEDATA_TYPE);

		// Stack[0]: [Z

		return 3; // Maximum local stack size is 3
	}

	public void startup() {
		System.getProperties().put(key, dataAccess);
	}

	public void shutdown() {
		System.getProperties().remove(key);
	}

}
