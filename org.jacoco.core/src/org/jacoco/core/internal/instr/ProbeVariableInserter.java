/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Simplified version of ASM's
 * {@link org.objectweb.asm.commons.LocalVariablesSorter} that inserts a local
 * variable at the very beginning of the method. This avoids maintaining mapping
 * tables and prevents ASM bug #314563.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
class ProbeVariableInserter extends MethodAdapter {

	/** Position of the inserted variable. */
	protected final int variable;

	/** Index the inserted variable. */
	private final int variableIdx;

	private boolean firstFrame = true;

	/**
	 * Creates a new {@link ProbeVariableInserter}.
	 * 
	 * @param access
	 *            access flags of the adapted method.
	 * @param desc
	 *            the method's descriptor
	 * @param mv
	 *            the method visitor to which this adapter delegates calls
	 */
	ProbeVariableInserter(final int access, final String desc,
			final MethodVisitor mv) {
		super(mv);
		int idx = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
		int pos = idx;
		for (final Type t : Type.getArgumentTypes(desc)) {
			idx++;
			pos += t.getSize();
		}
		variableIdx = idx;
		variable = pos;
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		mv.visitVarInsn(opcode, map(var));
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		mv.visitIincInsn(map(var), increment);
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
		mv.visitMaxs(maxStack, maxLocals + 1);
	}

	@Override
	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		mv.visitLocalVariable(name, desc, signature, start, end, map(index));
	}

	private int map(final int var) {
		if (var < variable) {
			return var;
		} else {
			return var + 1;
		}
	}

	@Override
	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {

		if (type != Opcodes.F_NEW) { // uncompressed frame
			throw new IllegalStateException(
					"ClassReader.accept() should be called with EXPAND_FRAMES flag");
		}

		if (firstFrame) {
			// The first frame is generated by ASM and represents the implicit
			// frame derived from the method signature only. This frame must not
			// yet be modified.
			mv.visitFrame(type, nLocal, local, nStack, stack);
			firstFrame = false;
			return;
		}

		final Object[] newLocal = new Object[nLocal + 1];
		for (int i = 0; i <= local.length; i++) {
			if (i < variableIdx) {
				newLocal[i] = local[i];
				continue;
			}
			if (i > variableIdx) {
				newLocal[i] = local[i - 1];
				continue;
			}
			newLocal[i] = InstrSupport.DATAFIELD_DESC;
		}
		mv.visitFrame(type, nLocal + 1, newLocal, nStack, stack);
	}

}
