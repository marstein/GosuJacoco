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
package org.jacoco.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.analysis.BundleCoverage;
import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.PackageCoverage;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.instr.Analyzer;
import org.jacoco.report.FileReportOutput;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiFormatter;
import org.jacoco.report.html.HTMLFormatter;

/**
 * Task for coverage report generation. Experimental implementation that needs
 * refinement.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ReportTask extends Task {

	/**
	 * Container element for class file groups.
	 */
	public static class GroupElement {

		final List<GroupElement> children = new ArrayList<GroupElement>();

		final Union classfiles = new Union();

		final Union sourcefiles = new Union();

		String name;

		public void setName(final String name) {
			this.name = name;
		}

		public GroupElement createGroup() {
			final GroupElement group = new GroupElement();
			children.add(group);
			return group;
		}

		public Union createClassfiles() {
			return classfiles;
		}

		public Union createSourcefiles() {
			return sourcefiles;
		}

	}

	/**
	 * Interface for child elements that define formatters.
	 */
	private interface IFormatterElement {

		IReportFormatter createFormatter();

	}

	public static class HTMLFormatterElement implements IFormatterElement {

		private File todir;

		public void setTodir(final File todir) {
			this.todir = todir;
		}

		public IReportFormatter createFormatter() {
			final HTMLFormatter formatter = new HTMLFormatter();
			formatter.setReportOutput(new FileReportOutput(todir));
			return formatter;
		}

	}

	private final Union executiondataElement = new Union();

	private final GroupElement structure = new GroupElement();

	private final List<IFormatterElement> formatters = new ArrayList<IFormatterElement>();

	public Union createExecutiondata() {
		return executiondataElement;
	}

	public GroupElement createStructure() {
		return structure;
	}

	public HTMLFormatterElement createHtml() {
		final HTMLFormatterElement element = new HTMLFormatterElement();
		formatters.add(element);
		return element;
	}

	@Override
	public void execute() throws BuildException {
		final ExecutionDataStore executionData = loadExecutionData();
		final IReportFormatter formatter = createFormatter();
		try {
			createReport(structure, formatter, executionData);
		} catch (final IOException e) {
			throw new BuildException("Error while creating report.", e);
		}
	}

	private ExecutionDataStore loadExecutionData() {
		final ExecutionDataStore data = new ExecutionDataStore();
		for (final Iterator<?> i = executiondataElement.iterator(); i.hasNext();) {
			final Resource resource = (Resource) i.next();
			InputStream in = null;
			try {
				in = resource.getInputStream();
				new ExecutionDataReader(in).accept(data);
				in.close();
			} catch (final IOException e) {
				throw new BuildException("Unable to read execution data file "
						+ resource.getName(), e);
			} finally {
				FileUtils.close(in);
			}
		}
		return data;
	}

	private IReportFormatter createFormatter() {
		final MultiFormatter multi = new MultiFormatter();
		for (final IFormatterElement f : formatters) {
			multi.add(f.createFormatter());
		}
		return multi;
	}

	private void createReport(final GroupElement group,
			final IReportFormatter formatter,
			final ExecutionDataStore executionData) throws IOException {
		final CoverageNodeImpl node = createNode(group, executionData);
		final IReportVisitor visitor = formatter.createReportVisitor(node);
		final SourceFileCollection sourceFileLocator = new SourceFileCollection(
				group.sourcefiles);
		if (node instanceof BundleCoverage) {
			visitBundle(visitor, (BundleCoverage) node, sourceFileLocator);
		} else {
			for (final GroupElement g : group.children) {
				createReport(g, visitor, node, executionData);
			}
		}
		visitor.visitEnd(sourceFileLocator);
	}

	private void createReport(final GroupElement group,
			final IReportVisitor parentVisitor,
			final CoverageNodeImpl parentNode,
			final ExecutionDataStore executionData) throws IOException {
		final CoverageNodeImpl node = createNode(group, executionData);
		final IReportVisitor visitor = parentVisitor.visitChild(node);
		final SourceFileCollection sourceFileLocator = new SourceFileCollection(
				group.sourcefiles);
		if (node instanceof BundleCoverage) {
			visitBundle(visitor, (BundleCoverage) node, sourceFileLocator);
		} else {
			for (final GroupElement g : group.children) {
				createReport(g, visitor, node, executionData);
			}
		}
		parentNode.increment(node);
		visitor.visitEnd(sourceFileLocator);
	}

	private CoverageNodeImpl createNode(final GroupElement group,
			final ExecutionDataStore executionData) throws IOException {
		if (group.children.size() > 0) {
			return new CoverageNodeImpl(ElementType.GROUP, group.name, false);
		} else {
			final CoverageBuilder builder = new CoverageBuilder(executionData);
			final Analyzer analyzer = new Analyzer(builder);
			for (final Iterator<?> i = group.classfiles.iterator(); i.hasNext();) {
				final Resource resource = (Resource) i.next();
				if (resource.isDirectory() && resource instanceof FileResource) {
					analyzer.analyzeAll(((FileResource) resource).getFile());
					continue;
				}
				if (resource.getName().toLowerCase().endsWith(".jar")) {
					final InputStream in = resource.getInputStream();
					analyzer.analyzeJAR(in);
					in.close();
					continue;
				}
				if (resource.getName().toLowerCase().endsWith(".class")) {
					final InputStream in = resource.getInputStream();
					analyzer.analyze(in);
					in.close();
				}
			}
			return builder.getBundle(group.name);
		}
	}

	private static class SourceFileCollection implements ISourceFileLocator {

		private final Map<String, Resource> resources = new HashMap<String, Resource>();

		SourceFileCollection(final ResourceCollection collection) {
			for (final Iterator<?> i = collection.iterator(); i.hasNext();) {
				final Resource r = (Resource) i.next();
				resources.put(r.getName().replace(File.separatorChar, '/'), r);
			}
		}

		public Reader getSourceFile(final String packageName,
				final String fileName) throws IOException {
			final Resource r = resources.get(packageName + '/' + fileName);
			if (r == null) {
				return null;
			}
			// TODO: specify encoding
			return new InputStreamReader(r.getInputStream());
		}
	}

	private static void visitBundle(final IReportVisitor visitor,
			final BundleCoverage bundledata,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		for (final PackageCoverage p : bundledata.getPackages()) {
			visitPackage(visitor.visitChild(p), p, sourceFileLocator);
		}
	}

	private static void visitPackage(final IReportVisitor visitor,
			final PackageCoverage packagedata,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		visitLeafs(visitor, packagedata.getSourceFiles(), sourceFileLocator);
		for (final ClassCoverage c : packagedata.getClasses()) {
			visitClass(visitor.visitChild(c), c, sourceFileLocator);
		}
		visitor.visitEnd(sourceFileLocator);
	}

	private static void visitClass(final IReportVisitor visitor,
			final ClassCoverage classdata,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		visitLeafs(visitor, classdata.getMethods(), sourceFileLocator);
		visitor.visitEnd(sourceFileLocator);
	}

	private static void visitLeafs(final IReportVisitor visitor,
			final Collection<? extends ICoverageNode> leafs,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		for (final ICoverageNode l : leafs) {
			final IReportVisitor child = visitor.visitChild(l);
			child.visitEnd(sourceFileLocator);
		}
	}

}
