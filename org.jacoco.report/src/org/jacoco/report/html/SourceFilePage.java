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
package org.jacoco.report.html;

import java.io.IOException;
import java.io.Reader;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;

/**
 * Page showing the content of a source file with numbered and highlighted
 * source lines.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SourceFilePage extends ReportPage {

	private Reader sourceReader;

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 * @param parent
	 * @param outputFolder
	 * @param context
	 */
	public SourceFilePage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder outputFolder,
			final IHTMLReportContext context) {
		super(node, parent, outputFolder, context);
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		throw new IllegalStateException("Source don't have child nodes.");
	}

	@Override
	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		final SourceFileCoverage s = (SourceFileCoverage) getNode();
		sourceReader = sourceFileLocator.getSourceFile(s.getPackageName(), s
				.getName());
		if (sourceReader != null) {
			super.visitEnd(sourceFileLocator);
		}
	}

	@Override
	protected void content(final HTMLElement body,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		final SourceFileCoverage s = (SourceFileCoverage) getNode();
		new SourceHighlighter().render(body, s.getLines(), sourceReader);
		sourceReader.close();
	}

	@Override
	protected String getFileName() {
		return getNode().getName() + ".html";
	}

	@Override
	protected ReportOutputFolder getFolder(final ReportOutputFolder base) {
		return base;
	}

	/**
	 * Checks whether this page has actually been rendered. This might not be
	 * the case if no source file has been found.
	 * 
	 * @return whether the page has been created
	 */
	public boolean exists() {
		return sourceReader != null;
	}

}
