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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.jacoco.core.runtime.AgentOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

/**
 * @phase initialize
 * @goal prepare-agent
 * @requiresProject true
 * @requiresDependencyResolution runtime
 */
public class JaCoCoAutomationMojo extends AbstractMojo {

  /**
   * Name of the property used in maven-osgi-test-plugin.
   */
  private static final String TYCHO_ARG_LINE = "tycho.testArgLine";

  /**
   * Name of the property used in maven-surefire-plugin.
   */
  private static final String SUREFIRE_ARG_LINE = "argLine";

  /**
   * @parameter expression="${project}"
   */
  private MavenProject project;

  /**
   * @component
   * @readonly
   */
  private ArtifactFactory artifactFactory;

  /**
   * @component
   * @readonly
   */
  private ArtifactResolver artifactResolver;

  /**
   * @parameter expression="${localRepository}"
   * @readonly
   */
  private ArtifactRepository localRepository;

  /**
   * Path to the output file for execution data.
   * 
   * @parameter default-value="${project.build.directory}/jacoco.exec"
   */
  private File destfile;

  /**
   * If set to true and the execution data file already exists, coverage data is appended to the existing file.
   * If set to false, an existing execution data file will be replaced.
   * 
   * @parameter default-value="true"
   */
  private boolean append;

  /**
   * A list of class names that should be included in execution analysis.
   * The list entries are separated by a colon (:) and may use wildcard characters (* and ?).
   * Except for performance optimization or technical corner cases this option is normally not required.
   * 
   * @parameter default-value="*"
   */
  private String includes;

  /**
   * A list of class names that should be excluded from execution analysis.
   * 
   * The list entries are separated by a colon (:) and may use wildcard characters (* and ?).
   * Except for performance optimization or technical corner cases this option is normally not required.
   * 
   * @parameter default-value=""
   */
  private String excludes;

  /**
   * A list of class loader names, that should be excluded from execution analysis.
   * The list entries are separated by a colon (:) and may use wildcard characters (* and ?).
   * This option might be required in case of special frameworks that conflict with JaCoCo code instrumentation,
   * in particular class loaders that do not have access to the Java runtime classes.
   * 
   * @parameter @default-value="sun.reflect.DelegatingClassLoader"
   */
  private String exclclassloaders;

  /**
   * A session identifier that is written with the execution data. Without this parameter a random identifier is created by the agent.
   * 
   * @parameter
   */
  private String sessionid;

  /**
   * If set to true coverage data will be written on VM shutdown.
   * 
   * @parameter default-value="true"
   */
  private boolean dumpOnExit;

  /**
   * Output method to use for writing coverage data. Valid options are:
   * <ul>
   * <li>file: At VM termination execution data is written to the file specified in the tofile attribute.</li>
   * <li>tcpserver: The agent listens for incoming connections on the TCP port specified by the address and port attribute. Execution data is written to this TCP connection.</li>
   * <li>tcpclient: At startup the agent connects to the TCP port specified by the address and port attribute. Execution data is written to this TCP connection.</li>
   * </ul>
   * 
   * @parameter default-value="file"
   */
  private String output;

  /**
   * IP address or hostname to bind to when the output method is tcpserver or connect to when the output method is tcpclient.
   * In tcpserver mode the value "*" causes the agent to accept connections on any local address.
   * 
   * @parameter
   */
  private String address;

  /**
   * Port to bind to when the output method is tcpserver or connect to when the output method is tcpclient.
   * In tcpserver mode the port must be available, which means that if multiple JaCoCo agents should run on the same machine, different ports have to be specified.
   * 
   * @parameter default-value="6300"
   */
  private int port;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!isEclipseTestPluginPackaging() && !isJarPackaging()) {
      return;
    }

    String vmArgument = createAgentOptions().getVMArgument(getAgentJarFile());
    if (isEclipseTestPluginPackaging()) {
      setCommandLineForTestPlugin(TYCHO_ARG_LINE, vmArgument);
    } else {
      setCommandLineForTestPlugin(SUREFIRE_ARG_LINE, vmArgument);
    }
  }

  private File getAgentJarFile() throws MojoExecutionException {
    Artifact jacocoAgentArtifact;
    try {
      jacocoAgentArtifact = artifactFactory.createArtifactWithClassifier("org.jacoco", "org.jacoco.agent.rt", getAgentVersion(), "jar", "all");
      artifactResolver.resolve(jacocoAgentArtifact, Collections.EMPTY_LIST, localRepository);
    } catch (ArtifactResolutionException e) {
      throw new MojoExecutionException("Unable to find JaCoCo agent", e);
    } catch (ArtifactNotFoundException e) {
      throw new MojoExecutionException("Unable to find JaCoCo agent", e);
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to find JaCoCo agent", e);
    }
    return jacocoAgentArtifact.getFile();
  }

  private static String getAgentVersion() throws IOException {
    Properties properties = new Properties();
    InputStream stream = JaCoCoAutomationMojo.class.getResourceAsStream("/META-INF/maven/org.jacoco/jacoco-maven-plugin/pom.properties");
    try {
      properties.load(stream);
    } finally {
      stream.close();
    }
    return properties.getProperty("version");
  }

  private AgentOptions createAgentOptions() throws MojoExecutionException {
    AgentOptions agentOptions = new AgentOptions();
    String destPath = destfile.getAbsolutePath();
    try {
      destPath = CommandLineUtils.quote(destPath);
    } catch (CommandLineException e) {
      throw new MojoExecutionException("Failure to use path", e);
    }
    agentOptions.setDestfile(destPath);
    agentOptions.setAppend(append);
    if (includes != null) {
      agentOptions.setIncludes(includes);
    }
    if (excludes != null) {
      agentOptions.setExcludes(excludes);
    }
    if (exclclassloaders != null) {
      agentOptions.setExclClassloader(exclclassloaders);
    }
    if (sessionid != null) {
      agentOptions.setSessionId(sessionid);
    }
    agentOptions.setDumpOnExit(dumpOnExit);
    agentOptions.setOutput(output);
    if (address != null) {
      agentOptions.setAddress(address);
    }
    agentOptions.setPort(port);
    return agentOptions;
  }

  private boolean isEclipseTestPluginPackaging() {
    return "eclipse-test-plugin".equals(project.getPackaging());
  }

  private boolean isJarPackaging() {
    return "jar".equals(project.getPackaging());
  }

  private void setCommandLineForTestPlugin(String propertyName, String argLine) {
    Properties projectProperties = project.getProperties();
    String oldValue = projectProperties.getProperty(propertyName);
    String newValue = oldValue == null ? argLine : argLine + ' ' + oldValue;
    getLog().info(propertyName + " set to " + argLine);
    projectProperties.put(propertyName, newValue);
  }

}
