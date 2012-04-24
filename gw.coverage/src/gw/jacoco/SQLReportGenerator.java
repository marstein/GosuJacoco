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

import com.beust.jcommander.JCommander;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.IReportVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Read a coverage data file, analyze it with the class/jar/dumped bytecode files from Gosu and write
 * coverage information to a database.
 * <p/>
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class SQLReportGenerator {

  public static final String JDBC_DRIVER_NAME = "org.h2.Driver";
  private String title;

  private File executionDataFile;
  private File projectDirectory;
  private List<File> classesDirectories = new ArrayList<File>();
  private List<File> sourceDirectories = new ArrayList<File>();
  private Connection reportConnection;
  private String connectString;
  private boolean createTables = false;

  private ExecutionDataStore executionDataStore;
  private SessionInfoStore sessionInfoStore;
  private String suiteName;
  private Date suiteRunDate;
  private String branchName;
  private String changelist;

  private static Logger logger = Logger.getLogger("SQLReportGenerator");

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
    this.suiteName = projectDirectory.getName();
    this.suiteRunDate = new Date(projectDirectory.lastModified());
    this.branchName = "branch";
    this.changelist = "cccccc";
  }

  public SQLReportGenerator withProjectDirectory(File directory) {
    this.projectDirectory = directory;
    return this;
  }

  public SQLReportGenerator withExecutionDataFile(File executionDataFile) {
    this.executionDataFile = executionDataFile;
    return this;
  }

  public SQLReportGenerator withClassesDirectory(File classesDirectory) {
    this.classesDirectories.add(classesDirectory);
    return this;
  }

  public SQLReportGenerator withSuiteRunDate(Date suiteRunDate) {
    this.suiteRunDate = new Date(projectDirectory.lastModified());
    return this;
  }

  public SQLReportGenerator withSuiteName(String suiteName) {
    this.suiteName = suiteName;
    return this;
  }

  public SQLReportGenerator withBranchName(String branchName) {
    this.branchName = branchName;
    return this;
  }

  public SQLReportGenerator withChangelist(String changelist) {
    this.changelist = changelist;
    return this;
  }

  public SQLReportGenerator withJDBCConnection(String jdbcConnectionString) {
    this.connectString = jdbcConnectionString;
    return this;
  }

  public SQLReportGenerator withCreateTables(boolean createTables) {
    this.createTables = createTables;
    return this;
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
    try {
      Class.forName(JDBC_DRIVER_NAME);
      this.reportConnection = DriverManager.getConnection(connectString, "sa", "");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not load org.h2.Driver database driver", e);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not get org.h2.Driver database connection to " + connectString, e);
    }

    try {
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

      if (sourceDirectories.isEmpty()) {
        visitor.visitBundle(bundleCoverage, null);
      }

      // Signal end of structure information to allow report to write all
      // information out
      visitor.visitEnd();
    } finally {
      try {
        this.reportConnection.close();
      } catch (SQLException e) {
        logger.severe("Could not close database connection" + e);
      }
    }
  }

  private void loadExecutionData() throws IOException {
    logger.info("Loading execution data from " + executionDataFile.toString());
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
      logger.info("Analyzing class directory/jar " + classesDirectory.toString());
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
    final CommandLineArguments commandLineArguments = new CommandLineArguments();
    new JCommander(commandLineArguments, args);

    if (commandLineArguments.createTables) {
      createDBTables(commandLineArguments.jdbcConnection);
    }

    if (commandLineArguments.directory.size() != 1) {
      System.err.println("Please specify exec file, bytecode dirs, etc!");
      System.err.println("java -cp jacoco.jar;gw-coverage.jar;h2.jar -createTables -branch e-pr-merge -changelist 444222 -classesdir P:\\eng\\emerald\\pl\\ready\\active\\core\\gitmo\\configenv\\platform\\pl\\classes -execfile jc-coverage-gitmo-PLV3BareBone.exec -suite PLV3BareBone -jdbc jdbc:h2:~/coverage");
      System.exit(2);
    } else {
      final SQLReportGenerator generator = new SQLReportGenerator(commandLineArguments.directory.get(0));
      generator.withBranchName(commandLineArguments.branchName)
              .withChangelist(commandLineArguments.changeList)
              .withExecutionDataFile(commandLineArguments.execFile)
              .withJDBCConnection(commandLineArguments.jdbcConnection)
              .withSuiteName(commandLineArguments.suiteName)
              .withSuiteRunDate(commandLineArguments.suiteRunDate);
      for (File cpElement : commandLineArguments.classesDirs) {
        generator.withClassesDirectory(cpElement);
      }
      generator.create();
    }
  }

  private static void createDBTables(String connectString) {
    Connection reportConnection = null;
    try {
      Class.forName(JDBC_DRIVER_NAME);
      reportConnection = DriverManager.getConnection(connectString, "sa", "");
      Statement createTable = reportConnection.createStatement();
      createTable.executeUpdate("CREATE TABLE COVERAGE (branch varchar(100), changelist varchar(30), suite varchar(100), package varchar(100), class varchar(300), suite_run_date timestamp, INSTRUCTION_MISSED integer, INSTRUCTION_COVERED integer, BRANCH_MISSED integer, BRANCH_COVERED integer, LINE_MISSED integer, LINE_COVERED integer, COMPLEXITY_MISSED integer, COMPLEXITY_COVERED integer, METHOD_MISSED integer, METHOD_COVERED integer)");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not load " + JDBC_DRIVER_NAME + " database driver", e);
    } catch (SQLException e) {
      logger.warning("Error during create table. Continuing... " + e.toString());
    } finally {
      if (reportConnection != null) {
        try {
          reportConnection.close();
        } catch (SQLException e) {
          logger.severe("Cannot close connection after creating table!");
        }
      }
    }
  }
}
