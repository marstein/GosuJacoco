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
import java.io.Writer;

import org.jacoco.report.xml.XMLElement;

/**
 * A {@link XMLElement} with utility methods to create XHTML documents. It
 * provides methods of HTML tags to avoid magic strings in the generators.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class HTMLElement extends XMLElement {

	/**
	 * Creates a new element for a HTML document.
	 * 
	 * @param writer
	 *            all output will be written directly to this
	 * @param name
	 *            element name
	 */
	protected HTMLElement(final Writer writer, final String name) {
		super(writer, name);
	}

	@Override
	public HTMLElement element(final String name) throws IOException {
		final HTMLElement element = new HTMLElement(writer, name);
		addChildElement(element);
		return element;
	}

	/**
	 * Creates a 'meta' element.
	 * 
	 * @param httpequivattr
	 *            value of the http-equiv attribute
	 * @param contentattr
	 *            value for the content attribute
	 * @return 'meta' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement meta(final String httpequivattr, final String contentattr)
			throws IOException {
		final HTMLElement meta = element("meta");
		meta.attr("http-equiv", httpequivattr);
		meta.attr("content", contentattr);
		return meta;
	}

	/**
	 * Creates a 'link' element.
	 * 
	 * @param relattr
	 *            value of the rel attribute
	 * @param hrefattr
	 *            value for the href attribute
	 * @param typeattr
	 *            value for the type attribute
	 * @return 'link' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement link(final String relattr, final String hrefattr,
			final String typeattr) throws IOException {
		final HTMLElement link = element("link");
		link.attr("rel", relattr);
		link.attr("href", hrefattr);
		link.attr("type", typeattr);
		return link;
	}

	/**
	 * Creates a 'title' element.
	 * 
	 * @return 'title' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement title() throws IOException {
		return element("title");
	}

	/**
	 * Creates a 'h1' element.
	 * 
	 * @return 'h1' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement h1() throws IOException {
		return element("h1");
	}

	/**
	 * Creates a 'span' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @return 'span' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement span(final String classattr) throws IOException {
		final HTMLElement pre = element("span");
		pre.attr("class", classattr);
		return pre;
	}

	/**
	 * Creates a 'span' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @param idattr
	 *            value of the id attribute
	 * @return 'span' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement span(final String classattr, final String idattr)
			throws IOException {
		final HTMLElement pre = element("span");
		pre.attr("class", classattr);
		pre.attr("id", idattr);
		return pre;
	}

	/**
	 * Creates a 'div' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @return 'div' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement div(final String classattr) throws IOException {
		final HTMLElement pre = element("div");
		pre.attr("class", classattr);
		return pre;
	}

	/**
	 * Creates a 'pre' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @return 'pre' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement pre(final String classattr) throws IOException {
		final HTMLElement pre = element("pre");
		pre.attr("class", classattr);
		return pre;
	}

	/**
	 * Creates a empty 'br' element.
	 * 
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public void br() throws IOException {
		element("br");
	}

	/**
	 * Creates a 'a' element.
	 * 
	 * @param hrefattr
	 *            value of the href attribute
	 * @param classattr
	 *            value of the class attribute
	 * @return 'a' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement a(final String hrefattr, final String classattr)
			throws IOException {
		final HTMLElement a = element("a");
		a.attr("href", hrefattr);
		a.attr("class", classattr);
		return a;
	}

	/**
	 * Creates a 'table' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @return 'table' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement table(final String classattr) throws IOException {
		final HTMLElement table = element("table");
		table.attr("class", classattr);
		return table;
	}

	/**
	 * Creates a 'thead' element.
	 * 
	 * @return 'thead' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement thead() throws IOException {
		return element("thead");
	}

	/**
	 * Creates a 'tfoot' element.
	 * 
	 * @return 'tfoot' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement tfoot() throws IOException {
		return element("tfoot");
	}

	/**
	 * Creates a 'tbody' element.
	 * 
	 * @return 'tbody' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement tbody() throws IOException {
		return element("tbody");
	}

	/**
	 * Creates a 'tr' element.
	 * 
	 * @return 'tr' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement tr() throws IOException {
		return element("tr");
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td() throws IOException {
		return td(null, 1);
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td(final String classattr) throws IOException {
		return td(classattr, 1);
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @param colspanattr
	 *            value of the colspan attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td(final int colspanattr) throws IOException {
		return td(null, colspanattr);
	}

	/**
	 * Creates a 'td' element.
	 * 
	 * @param classattr
	 *            value of the class attribute
	 * @param colspanattr
	 *            value of the colspan attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public HTMLElement td(final String classattr, final int colspanattr)
			throws IOException {
		final HTMLElement pre = element("td");
		if (classattr != null) {
			pre.attr("class", classattr);
		}
		if (colspanattr > 1) {
			pre.attr("colspan", String.valueOf(colspanattr));
		}
		return pre;
	}

	/**
	 * Creates a 'img' element.
	 * 
	 * @param srcattr
	 *            value of the src attribute
	 * @param widthattr
	 *            value of the width attribute
	 * @param heightattr
	 *            value of the height attribute
	 * @param altattr
	 *            value of the alt attribute
	 * @throws IOException
	 *             in case of problems with the writer
	 */
	public void img(final String srcattr, final int widthattr,
			final int heightattr, final String altattr) throws IOException {
		final HTMLElement pre = element("img");
		pre.attr("src", srcattr);
		pre.attr("width", String.valueOf(widthattr));
		pre.attr("height", String.valueOf(heightattr));
		pre.attr("alt", String.valueOf(altattr));
	}

}
