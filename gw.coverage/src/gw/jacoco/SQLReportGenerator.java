/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package gw.jacoco;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Read a coverage data file, analyze it with the class/jar/dumped bytecode files from Gosu and write
 * coverage information to a database.
 * <p/>
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class SQLReportGenerator {

  private final String title;

  private final File executionDataFile;
  private final File projectDirectory;
  private final List<File> classesDirectories = new ArrayList<File>();
  private final List<File> sourceDirectories = new ArrayList<File>();
  private Connection reportConnection;

  private ExecutionDataStore executionDataStore;
  private SessionInfoStore sessionInfoStore;
  private String suiteName;
  private Date suiteRunDate;
  private String branchName;
  private String changelist;

  /**
   * Create a new generator based for the given project.
   *
   * @param projectDirectory
   */
  public SQLReportGenerator(final File projectDirectory) {
    // These have to be passed in as parameters or can be guessed from the files in the projectDirectory.
    this.title = projectDirectory.getName();
    this.projectDirectory = projectDirectory;
    this.executionDataFile = new File(projectDirectory, "jacoco.exec");
    this.classesDirectories.add(new File(projectDirectory, "bin"));
    this.sourceDirectories.add(new File(projectDirectory, "src"));
    this.suiteName = projectDirectory.getName();
    this.suiteRunDate = new Date(projectDirectory.lastModified());
    this.branchName = "branch";
    this.changelist = "cccccc";
  }

  /**
   * Create the report.
   *
   * @throws java.io.IOException
   */
  public void create() throws IOException {

    // Read the jacoco.exec file. Multiple data stores could be merged
    // at this point
    loadExecutionData();

    // Run the structure analyzer on a single class folder to build up
    // the coverage model. The process would be similar if your classes
    // were in a jar file. Typically you would create a bundle for each
    // class folder and each jar you want in your report. If you have
    // more than one bundle you will need to add a grouping node to your
    // report
    final IBundleCoverage bundleCoverage = analyzeStructure();

    createReport(bundleCoverage);

  }

  private void createReport(final IBundleCoverage bundleCoverage) throws IOException {
    String connectString = "jdbc:h2:~/coverage-test-"+this.suiteName;
    try {
      Class.forName("org.h2.Driver");
      this.reportConnection = DriverManager.getConnection(connectString, "sa", "");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not load org.h2.Driver database driver", e);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not get org.h2.Driver database connection to "+connectString, e);
    }

    // Create a concrete report visitor based on some supplied
    // configuration. In this case we use the defaults
    final SQLFormatter sqlFormatter = new SQLFormatter();
    final IReportVisitor visitor = sqlFormatter.createVisitor(reportConnection, branchName, changelist, suiteName, suiteRunDate);

    // Initialize the report with all of the execution and session
    // information. At this point the report doesn't know about the
    // structure of the report being created
    visitor.visitInfo(sessionInfoStore.getInfos(), executionDataStore.getContents());

    for (File sourceDirectory : sourceDirectories) {
      // Populate the report structure with the bundle coverage information.
      // Call visitGroup if you need groups in your report.
      visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(sourceDirectory, "utf-8", 4));
    }
    // Signal end of structure information to allow report to write all
    // information out
    visitor.visitEnd();

    try {
      this.reportConnection.close();
    } catch (SQLException e) {
      throw new IllegalStateException("Could not close database connection",e);
    }
  }

  private void loadExecutionData() throws IOException {
    final FileInputStream fis = new FileInputStream(executionDataFile);
    final ExecutionDataReader executionDataReader = new ExecutionDataReader(fis);
    executionDataStore = new ExecutionDataStore();
    sessionInfoStore = new SessionInfoStore();

    executionDataReader.setExecutionDataVisitor(executionDataStore);
    executionDataReader.setSessionInfoVisitor(sessionInfoStore);

    while (executionDataReader.read()) {
    }

    fis.close();
  }

  private IBundleCoverage analyzeStructure() throws IOException {
    final CoverageBuilder coverageBuilder = new CoverageBuilder();
    final Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);

    for (File classesDirectory : classesDirectories) {
      analyzer.analyzeAll(classesDirectory);
    }
    return coverageBuilder.getBundle(title);
  }

  /**
   * Starts the report generation process
   *
   * @param args generate reports for suite located in directory arg[0]
   * @throws java.io.IOException
   */
  public static void main(final String[] args) throws IOException {
      final SQLReportGenerator generator = new SQLReportGenerator(new File(args[0]));
      generator.create();
  }

}
