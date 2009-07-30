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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;

/**
 * Page showing coverage information for a class as a table of methods. The
 * methods are linked to the corresponding source file.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassPage extends ReportPage {

	private class MethodItem implements ICoverageTableItem {

		private final MethodCoverage node;

		MethodItem(final MethodCoverage node) {
			this.node = node;
		}

		public String getLabel() {
			return context.getLanguageNames().getMethodName(
					ClassPage.this.getNode().getName(), node.getName(),
					node.getDesc());
		}

		public String getLink(final ReportOutputFolder base) {
			final SourceFilePage sourceFilePage = sourceFiles
					.get(((ClassCoverage) ClassPage.this.getNode())
							.getSourceFileName());
			if (sourceFilePage == null || !sourceFilePage.exists()) {
				return null;
			}
			final String link = sourceFilePage.getLink(base);
			final int first = node.getLines().getFirstLine();
			return first != -1 ? link + "#L" + first : link;
		}

		public ICoverageNode getNode() {
			return node;
		}

	}

	private final List<MethodItem> methods = new ArrayList<MethodItem>();

	private final Map<String, SourceFilePage> sourceFiles;

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 * @param parent
	 * @param sourceFiles
	 * @param outputFolder
	 * @param context
	 */
	public ClassPage(final ICoverageNode node, final ReportPage parent,
			final Map<String, SourceFilePage> sourceFiles,
			final ReportOutputFolder outputFolder,
			final IHTMLReportContext context) {
		super(node, parent, outputFolder, context);
		this.sourceFiles = sourceFiles;
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		methods.add(new MethodItem((MethodCoverage) node));
		return new IReportVisitor() {

			public IReportVisitor visitChild(final ICoverageNode node) {
				throw new IllegalStateException(
						"Methods must not have child nodes.");
			}

			public void visitEnd(final ISourceFileLocator sourceFileLocator) {
			}
		};
	}

	@Override
	protected void content(final HTMLElement body,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		context.getTable(getNode().getElementType()).render(body, methods,
				getNode(), context.getResources(), outputFolder);
	}

	@Override
	protected String getFileName() {
		return getLabel() + ".html";
	}

	@Override
	public String getLabel() {
		return context.getLanguageNames().getClassName(getNode().getName());
	}

	@Override
	protected ReportOutputFolder getFolder(final ReportOutputFolder base) {
		return base;
	}

}
