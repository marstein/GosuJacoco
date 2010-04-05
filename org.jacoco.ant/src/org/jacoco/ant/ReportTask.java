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
 * $Id: $
 *******************************************************************************/
package org.jacoco.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
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
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.FileSingleReportOutput;
import org.jacoco.report.IMultiReportOutput;
import org.jacoco.report.IReportFormatter;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiFormatter;
import org.jacoco.report.ZipMultiReportOutput;
import org.jacoco.report.csv.CsvFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Task for coverage report generation. Experimental implementation that needs
 * refinement.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ReportTask extends Task {

	/**
	 * The source files are specified in a resource collection with additional
	 * attributes.
	 */
	public static class SourceFilesElement extends Union {

		String encoding;

		/**
		 * Defines the optional source file encoding. If not set the platform
		 * default is used.
		 * 
		 * @param encoding
		 *            source file encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

	}

	/**
	 * Container element for class file groups.
	 */
	public static class GroupElement {

		private final List<GroupElement> children = new ArrayList<GroupElement>();

		private final Union classfiles = new Union();

		private final SourceFilesElement sourcefiles = new SourceFilesElement();

		private String name;

		/**
		 * Sets the name of the group.
		 * 
		 * @param name
		 *            name of the group
		 */
		public void setName(final String name) {
			this.name = name;
		}

		/**
		 * Creates a new child group.
		 * 
		 * @return new child group
		 */
		public GroupElement createGroup() {
			final GroupElement group = new GroupElement();
			children.add(group);
			return group;
		}

		/**
		 * Returns the nested resource collection for class files.
		 * 
		 * @return resource collection for class files
		 */
		public Union createClassfiles() {
			return classfiles;
		}

		/**
		 * Returns the nested resource collection for source files.
		 * 
		 * @return resource collection for source files
		 */
		public SourceFilesElement createSourcefiles() {
			return sourcefiles;
		}

	}

	/**
	 * Interface for child elements that define formatters.
	 */
	private interface IFormatterElement {

		IReportFormatter createFormatter() throws IOException;

		void finish() throws IOException;

	}

	/**
	 * Formatter Element for HTML reports.
	 */
	public static class HTMLFormatterElement implements IFormatterElement {

		private File destdir;

		private File destfile;

		private String footer = "";

		private String encoding = "UTF-8";

		private ZipOutputStream zipOutput;

		/**
		 * Sets the output directory for the report.
		 * 
		 * @param destdir
		 *            output directory
		 */
		public void setDestdir(final File destdir) {
			this.destdir = destdir;
		}

		/**
		 * Sets the Zip output file for the report.
		 * 
		 * @param destfile
		 *            Zip output file
		 */
		public void setDestfile(final File destfile) {
			this.destfile = destfile;
		}

		/**
		 * Sets an optional footer text that will be displayed on every report
		 * page.
		 * 
		 * @param text
		 *            footer text
		 */
		public void setFooter(final String text) {
			this.footer = text;
		}

		/**
		 * Sets the output encoding for generated HTML files. Default is UTF-8.
		 * 
		 * @param encoding
		 *            output encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		public IReportFormatter createFormatter() throws IOException {
			final IMultiReportOutput output;
			if (destfile != null) {
				if (destdir != null) {
					throw new BuildException(
							"Either destination directory or file must be supplied, not both");
				}
				zipOutput = new ZipOutputStream(new FileOutputStream(destfile));
				output = new ZipMultiReportOutput(zipOutput);

			} else {
				if (destdir == null) {
					throw new BuildException(
							"Destination directory or file must be supplied for html report");
				}
				output = new FileMultiReportOutput(destdir);
			}
			final HTMLFormatter formatter = new HTMLFormatter();
			formatter.setReportOutput(output);
			formatter.setFooterText(footer);
			formatter.setOutputEncoding(encoding);
			return formatter;
		}

		public void finish() throws IOException {
			if (zipOutput != null) {
				zipOutput.close();
			}
		}

	}

	/**
	 * Formatter Element for CSV reports.
	 */
	public static class CsvFormatterElement implements IFormatterElement {

		private File destfile;

		private String encoding = "UTF-8";

		/**
		 * Sets the output file for the report.
		 * 
		 * @param destfile
		 *            output file
		 */
		public void setDestfile(final File destfile) {
			this.destfile = destfile;
		}

		public IReportFormatter createFormatter() {
			if (destfile == null) {
				throw new BuildException(
						"Destination file must be supplied for csv report");
			}
			final CsvFormatter formatter = new CsvFormatter();
			formatter.setReportOutput(new FileSingleReportOutput(destfile));
			formatter.setOutputEncoding(encoding);
			return formatter;
		}

		/**
		 * Sets the output encoding for generated XML file. Default is UTF-8.
		 * 
		 * @param encoding
		 *            output encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		public void finish() {
		}

	}

	/**
	 * Formatter Element for XML reports.
	 */
	public static class XMLFormatterElement implements IFormatterElement {

		private File destfile;

		private String encoding = "UTF-8";

		/**
		 * Sets the output file for the report.
		 * 
		 * @param destfile
		 *            output file
		 */
		public void setDestfile(final File destfile) {
			this.destfile = destfile;
		}

		/**
		 * Sets the output encoding for generated XML file. Default is UTF-8.
		 * 
		 * @param encoding
		 *            output encoding
		 */
		public void setEncoding(final String encoding) {
			this.encoding = encoding;
		}

		public IReportFormatter createFormatter() {
			if (destfile == null) {
				throw new BuildException(
						"Destination file must be supplied for xml report");
			}
			final XMLFormatter formatter = new XMLFormatter();
			formatter.setReportOutput(new FileSingleReportOutput(destfile));
			formatter.setOutputEncoding(encoding);
			return formatter;
		}

		public void finish() {
		}

	}

	private final Union executiondataElement = new Union();

	private final GroupElement structure = new GroupElement();

	private final List<IFormatterElement> formatters = new ArrayList<IFormatterElement>();

	/**
	 * Returns the nested resource collection for execution data files.
	 * 
	 * @return resource collection for execution files
	 */
	public Union createExecutiondata() {
		return executiondataElement;
	}

	/**
	 * Returns the root group element that defines the report structure.
	 * 
	 * @return root group element
	 */
	public GroupElement createStructure() {
		return structure;
	}

	/**
	 * Creates a new HTML report element.
	 * 
	 * @return HTML report element
	 */
	public HTMLFormatterElement createHtml() {
		final HTMLFormatterElement element = new HTMLFormatterElement();
		formatters.add(element);
		return element;
	}

	/**
	 * Creates a new CSV report element.
	 * 
	 * @return CSV report element
	 */
	public CsvFormatterElement createCsv() {
		final CsvFormatterElement element = new CsvFormatterElement();
		formatters.add(element);
		return element;
	}

	/**
	 * Creates a new XML report element.
	 * 
	 * @return CSV report element
	 */
	public XMLFormatterElement createXml() {
		final XMLFormatterElement element = new XMLFormatterElement();
		formatters.add(element);
		return element;
	}

	@Override
	public void execute() throws BuildException {
		final ExecutionDataStore executionData = loadExecutionData();
		try {
			final IReportFormatter formatter = createFormatter();
			createReport(structure, formatter, executionData);
			finishFormatters();
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
				in = new BufferedInputStream(resource.getInputStream());
				final ExecutionDataReader reader = new ExecutionDataReader(in);
				reader.setExecutionDataVisitor(data);
				reader.read();
			} catch (final IOException e) {
				throw new BuildException("Unable to read execution data file "
						+ resource.getName(), e);
			} finally {
				FileUtils.close(in);
			}
		}
		return data;
	}

	private IReportFormatter createFormatter() throws IOException {
		final MultiFormatter multi = new MultiFormatter();
		for (final IFormatterElement f : formatters) {
			multi.add(f.createFormatter());
		}
		return multi;
	}

	private void finishFormatters() throws IOException {
		for (final IFormatterElement f : formatters) {
			f.finish();
		}
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
		if (group.name == null) {
			throw new BuildException("Group name must be supplied");
		}
		if (group.children.size() > 0) {
			return new CoverageNodeImpl(ElementType.GROUP, group.name, false);
		} else {
			final CoverageBuilder builder = new CoverageBuilder(executionData);
			final Analyzer analyzer = new Analyzer(builder);
			for (final Iterator<?> i = group.classfiles.iterator(); i.hasNext();) {
				final Resource resource = (Resource) i.next();
				if (resource.isDirectory() && resource instanceof FileResource) {
					analyzer.analyzeAll(((FileResource) resource).getFile());
				} else {
					final InputStream in = resource.getInputStream();
					analyzer.analyzeAll(in);
					in.close();
				}
			}
			return builder.getBundle(group.name);
		}
	}

	private static class SourceFileCollection implements ISourceFileLocator {

		private final String encoding;

		private final Map<String, Resource> resources = new HashMap<String, Resource>();

		SourceFileCollection(final SourceFilesElement sourceFiles) {
			encoding = sourceFiles.encoding;
			for (final Iterator<?> i = sourceFiles.iterator(); i.hasNext();) {
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
			if (encoding == null) {
				return new InputStreamReader(r.getInputStream());
			} else {
				return new InputStreamReader(r.getInputStream(), encoding);
			}
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
