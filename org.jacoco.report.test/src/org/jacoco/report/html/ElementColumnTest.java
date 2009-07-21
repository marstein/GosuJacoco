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

import static org.junit.Assert.assertEquals;

import org.jacoco.core.analysis.CounterImpl;
import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.MemoryReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link ElementColumn}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ElementColumnTest {

	private MemoryReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	private HTMLDocument doc;

	private HTMLElement tr;

	private HTMLSupport support;

	@Before
	public void setup() throws Exception {
		output = new MemoryReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
		doc = new HTMLDocument(root.createFile("Test.html"));
		doc.head().title();
		tr = doc.body().table("somestyle").tr();
		support = new HTMLSupport();
	}

	@Test
	public void testHeader() throws Exception {
		new ElementColumn().header(tr, resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Element", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testFooter() throws Exception {
		new ElementColumn().footer(tr, createTotal("test", 0), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Total", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
	}

	@Test
	public void testItemWithoutLink() throws Exception {
		new ElementColumn().item(tr, createItem("Abc", null), resources, root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Abc", support.findStr(doc,
				"/html/body/table/tr/td/text()"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/table/tr/td/@class"));
	}

	@Test
	public void testItemWithLink() throws Exception {
		new ElementColumn().item(tr, createItem("Def", "def.html"), resources,
				root);
		doc.close();
		final Document doc = support.parse(output.getFile("Test.html"));
		assertEquals("Def", support.findStr(doc,
				"/html/body/table/tr/td/a/text()"));
		assertEquals("def.html", support.findStr(doc,
				"/html/body/table/tr/td/a/@href"));
		assertEquals("el_group", support.findStr(doc,
				"/html/body/table/tr/td/a/@class"));
	}

	private ICoverageTableItem createItem(final String name, final String link) {
		final ICoverageNode node = new CoverageNodeImpl(ElementType.GROUP,
				name, false);
		return new ICoverageTableItem() {
			public String getLabel() {
				return name;
			}

			public String getLink(ReportOutputFolder base) {
				return link;
			}

			public ICoverageNode getNode() {
				return node;
			}
		};
	}

	private ICoverageNode createTotal(final String name, final int count) {
		return new CoverageNodeImpl(ElementType.GROUP, name, false) {
			{
				this.classCounter = CounterImpl.getInstance(count, false);
			}
		};
	}

}
