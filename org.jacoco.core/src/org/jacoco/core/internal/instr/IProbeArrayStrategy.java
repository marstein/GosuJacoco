/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Strategies to retrieve the probe array instance for each method within a
 * type. This abstraction is required as we need to follow a different strategy
 * depending on whether the instrumented type is a class or interface.
 */
interface IProbeArrayStrategy {

	/**
	 * Creates code that pushes the probe array instance on the operand stack.
	 * 
	 * @param mv
	 *            visitor to create code
	 * @return maximum stack size required by the generated code
	 */
	int pushInstance(MethodVisitor mv);

	/**
	 * Adds additional class members required by this strategy.
	 * 
	 * @param delegate
	 *            visitor to create fields and classes
	 */
	void addMembers(ClassVisitor delegate);

}
