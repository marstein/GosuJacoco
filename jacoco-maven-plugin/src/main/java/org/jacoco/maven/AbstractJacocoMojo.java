/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * Base class for JaCoCo Mojos.
 */
public abstract class AbstractJacocoMojo extends AbstractMojo {

	/**
	 * Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Flag used to suppress execution.
	 * 
	 * @parameter expression="${jacoco.skip}" default-value="false"
	 */
	private boolean skip;

	public final void execute() {
		if ("pom".equals(project.getPackaging())) {
			getLog().info(
					"Skipping JaCoCo for project with packaging type 'pom'");
			return;
		}
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			return;
		}
		executeMojo();
	}

	/**
	 * Executes Mojo.
	 */
	protected abstract void executeMojo();

	/**
	 * @return Maven project
	 */
	protected final MavenProject getProject() {
		return project;
	}

}
