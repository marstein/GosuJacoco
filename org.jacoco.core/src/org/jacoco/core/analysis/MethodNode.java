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
package org.jacoco.core.analysis;

/**
 * Coverage data of a single method.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MethodNode extends CoverageDataNodeImpl {

	private final String desc;

	private final String signature;

	/**
	 * Creates a method coverage data object with the given parameters.
	 * 
	 * @param name
	 *            name of the method
	 * @param desc
	 *            parameter description
	 * @param signature
	 *            generic signature or <code>null</code>
	 */
	public MethodNode(final String name, final String desc,
			final String signature) {
		super(ElementType.METHOD, name, true);
		this.desc = desc;
		this.signature = signature;
		this.methodCounter = CounterImpl.getInstance(false);
	}

	/**
	 * Adds the given block to this method.
	 * 
	 * @param instructions
	 *            number of instructions of this block
	 * @param lines
	 *            lines of this block
	 * @param covered
	 *            <code>true</code>, if this block is covered
	 */
	public void addBlock(final int instructions, final int[] lines,
			final boolean covered) {
		this.lines.increment(lines, covered);
		this.blockCounter = this.blockCounter.increment(CounterImpl
				.getInstance(covered));
		this.instructionCounter = this.instructionCounter.increment(CounterImpl
				.getInstance(instructions, covered));
		if (covered && this.methodCounter.getCoveredCount() == 0) {
			this.methodCounter = CounterImpl.getInstance(true);
		}
	}

	/**
	 * Returns the parameter description of the method.
	 * 
	 * @return parameter description
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Returns the generic signature of the method if defined.
	 * 
	 * @return generic signature or <code>null</code>
	 */
	public String getSignature() {
		return signature;
	}

}
