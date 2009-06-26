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

import java.util.Collection;

/**
 * Base implementation for coverage data nodes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageDataNodeImpl implements ICoverageDataNode {

	private final ElementType elementType;

	private final String name;

	/** Counter for blocks. */
	protected CounterImpl blockCounter;

	/** Counter for instructions. */
	protected CounterImpl instructionCounter;

	/** Counter for lines, if this element does not have lines. */
	protected CounterImpl lineCounter;

	/** Counter for methods. */
	protected CounterImpl methodCounter;

	/** Counter for classes. */
	protected CounterImpl classCounter;

	/** Line information if this element has lines. */
	protected final LinesImpl lines;

	/**
	 * Creates a new coverage data node.
	 * 
	 * @param elementType
	 *            type of the element represented by this instance
	 * @param name
	 *            name of this node
	 * @param hasLines
	 *            <code>true</code> id this element has source lines
	 */
	public CoverageDataNodeImpl(final ElementType elementType,
			final String name, final boolean hasLines) {
		this.elementType = elementType;
		this.name = name;
		this.blockCounter = CounterImpl.COUNTER_0_0;
		this.instructionCounter = CounterImpl.COUNTER_0_0;
		this.methodCounter = CounterImpl.COUNTER_0_0;
		this.classCounter = CounterImpl.COUNTER_0_0;
		this.lineCounter = hasLines ? null : CounterImpl.COUNTER_0_0;
		this.lines = hasLines ? new LinesImpl() : null;
	}

	/**
	 * Increments the counters by the values given by another element.
	 * 
	 * @param child
	 *            counters to add
	 */
	public void increment(final ICoverageDataNode child) {
		blockCounter = blockCounter.increment(child.getBlockCounter());
		instructionCounter = instructionCounter.increment(child
				.getInstructionCounter());
		methodCounter = methodCounter.increment(child.getMethodCounter());
		classCounter = classCounter.increment(child.getClassCounter());
		if (lines == null) {
			lineCounter = lineCounter.increment(child.getLineCounter());
		} else {
			lines.increment(child.getLines());
		}
	}

	/**
	 * Increments the counters by the values given by the collection of
	 * elements.
	 * 
	 * @param children
	 *            list of nodes, which counters will be added to this node
	 */
	public void increment(final Collection<? extends ICoverageDataNode> children) {
		for (final ICoverageDataNode child : children) {
			increment(child);
		}
	}

	// === ICoverageDataNode ===

	public ElementType getElementType() {
		return elementType;
	}

	public String getName() {
		return name;
	}

	public ICounter getInstructionCounter() {
		return instructionCounter;
	}

	public ICounter getBlockCounter() {
		return blockCounter;
	}

	public ICounter getLineCounter() {
		return lines != null ? lines : lineCounter;
	}

	public ICounter getMethodCounter() {
		return methodCounter;
	}

	public ICounter getClassCounter() {
		return classCounter;
	}

	public ICounter getCounter(final CounterEntity entity) {
		switch (entity) {
		case INSTRUCTION:
			return getInstructionCounter();
		case BLOCK:
			return getBlockCounter();
		case LINE:
			return getLineCounter();
		case METHOD:
			return getMethodCounter();
		case CLASS:
			return getClassCounter();
		}
		throw new IllegalArgumentException("Unknown entity " + entity);
	}

	public ILines getLines() {
		return lines;
	}

}
