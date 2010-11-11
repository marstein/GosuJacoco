/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A {@link MethodVisitor} with additional methods to get probe insertion
 * information.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public interface IMethodProbesVisitor extends MethodVisitor {

	/**
	 * Visits an unconditional probe that should be inserted at the current
	 * position.
	 * 
	 * @param probeId
	 *            id of the probe to insert
	 */
	public void visitProbe(int probeId);

	/**
	 * Visits a jump instruction. A probe with the given id should be inserted
	 * in a way that it is executed only when the jump to the given label is
	 * executed.
	 * 
	 * @param opcode
	 *            the opcode of the type instruction to be visited. This opcode
	 *            is either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
	 *            IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,
	 *            IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
	 * @param label
	 *            the operand of the instruction to be visited. This operand is
	 *            a label that designates the instruction to which the jump
	 *            instruction may jump.
	 * @param probeId
	 *            id of the probe
	 * @see MethodVisitor#visitJumpInsn(int, Label)
	 */
	void visitJumpInsnWithProbe(int opcode, Label label, int probeId);

	/**
	 * Visits a zero operand instruction with a probe. This event is used only
	 * for instructions that terminates the method. Therefore the probe must be
	 * inserted before the actual instruction.
	 * 
	 * @param opcode
	 *            the opcode of the instruction to be visited. This opcode is
	 *            either IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN or
	 *            ATHROW.
	 * @param probeId
	 *            id of the probe
	 * @see MethodVisitor#visitInsn(int)
	 */
	void visitInsnWithProbe(int opcode, int probeId);

	/**
	 * Visits a TABLESWITCH instruction with optional probes for each target
	 * label. Implementations can be optimized based on the fact that the same
	 * target labels will always have the same probe id within a call to this
	 * method. The probe id for each label can be obtained with
	 * {@link LabelInfo#getProbeId(Label)}.
	 * 
	 * @param min
	 *            the minimum key value.
	 * @param max
	 *            the maximum key value.
	 * @param dflt
	 *            beginning of the default handler block.
	 * @param labels
	 *            beginnings of the handler blocks. <code>labels[i]</code> is
	 *            the beginning of the handler block for the
	 *            <code>min + i</code> key.
	 * @see MethodVisitor#visitTableSwitchInsn(int, int, Label, Label[])
	 */
	public void visitTableSwitchInsnWithProbes(int min, int max, Label dflt,
			Label[] labels);

	/**
	 * Visits a LOOKUPSWITCH instruction with optional probes for each target
	 * label. Implementations can be optimized based on the fact that the same
	 * target labels will always have the same probe id within a call to this
	 * method. The probe id for each label can be obtained with
	 * {@link LabelInfo#getProbeId(Label)}.
	 * 
	 * @param dflt
	 *            beginning of the default handler block.
	 * @param keys
	 *            the values of the keys.
	 * @param labels
	 *            beginnings of the handler blocks. <code>labels[i]</code> is
	 *            the beginning of the handler block for the
	 *            <code>keys[i]</code> key.
	 * @see MethodVisitor#visitLookupSwitchInsn(Label, int[], Label[])
	 */
	public void visitLookupSwitchInsnWithProbes(Label dflt, int[] keys,
			Label[] labels);

}
