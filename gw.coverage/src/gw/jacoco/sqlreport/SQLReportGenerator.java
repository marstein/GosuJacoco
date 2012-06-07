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
package gw.jacoco.sqlreport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.IReportVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
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
  private String title;

  private File executionDataFile;
  private File projectDirectory;
  private List<File> classesDirectories = new ArrayList<File>();
  private List<File> sourceDirectories = new ArrayList<File>();
  private String connectString;

  private ExecutionDataStore executionDataStore;
  private SessionInfoStore sessionInfoStore;
  private String suiteName;
  private Date suiteRunDate;
  private String branchName;
  private String changelist;

  private static Logger logger = LoggerFactory.getLogger("SQLReportGenerator");

  /**
   * Create a new generator based for the given project.
   *
   * @param projectDirectory the directory where the results reside the .exec file resides
   */
  public SQLReportGenerator(final File projectDirectory) {
    // These have to be passed in as parameters or can be guessed from the files in the projectDirectory. Default settings below.
    this.title = projectDirectory.getName();
    this.projectDirectory = projectDirectory;
    this.executionDataFile = new File(projectDirectory, "jacoco.exec");
    this.suiteName = projectDirectory.getName();
    this.suiteRunDate = new Date(projectDirectory.lastModified());
    this.branchName = "branch";
    this.changelist = "cccccc";
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
    sessionFactory = initializeIBatis();
    SqlSession session = null;

    try {
      // Create a concrete report visitor based on some supplied
      // configuration. In this case we use the defaults
      final SQLFormatter sqlFormatter = new SQLFormatter();
      session = sessionFactory.openSession(TransactionIsolationLevel.READ_UNCOMMITTED);
      final IReportVisitor visitor = sqlFormatter.createVisitor(session, branchName, changelist, suiteName, suiteRunDate);

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
      if (session != null) {
        session.commit();
        session.close();
      }
    }
  }

  private SqlSessionFactory sessionFactory;

  protected static SqlSessionFactory initializeIBatis() {
    SqlSessionFactory factory;
    Reader resourceAsReader;
    try {
      resourceAsReader = Resources.getResourceAsReader("sourceCoverageMap/xml/dbo/ibatisconfig.xml");
      factory = new SqlSessionFactoryBuilder().build(resourceAsReader);
      resourceAsReader.close();
    } catch (IOException e) {
      throw new IllegalStateException("could not initialize ibatis", e);
    }
    return factory;
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
    JCommander commander = null;
    try {
      commander = new JCommander(commandLineArguments, args);
    } catch (ParameterException e) {
      System.err.println("Command line parameter error! " + e.toString());
      new JCommander(commandLineArguments).usage();
      System.exit(2);
    }

    if (commandLineArguments.createTables) {
      createDBTables(commandLineArguments.jdbcConnection);
    }

    if (commandLineArguments.directory.size() != 1) {
      commander.usage();
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

  /*
  Create the database tables for MS SQL Server. Uses vbinary. The table is used in ClassRowWriter.writeRow().
  It would improve the size of the written table if the dimensions (branch, suite, package, etc) were in separate
  tables that point into a fact table containing the measurements.
   */
  static void createDBTables(String connectString) {
    SqlSessionFactory sessionFactory = initializeIBatis();
    SqlSession session;
    if (connectString != null) {
      try {
        session = sessionFactory.openSession(DriverManager.getConnection(connectString));
      } catch (SQLException e) {
        throw new IllegalStateException("ERROR opening DB connection!", e);
      }
    } else {
      session = sessionFactory.openSession();
    }
    try {
      // Dimension tables: branch, suite, changelist, package, filename, class
      makeTable(session, "gw.coverage.dbo.CoverageMapper.createBranch");
      makeTable(session, "createSuite");
      makeTable(session, "createChangelist");
      makeTable(session, "createPackage");
      makeTable(session, "createFilename");
      makeTable(session, "createClass");

      // PACKAGE_COVERAGE
      makeTable(session, "createPackageCoverage");
      // SOURCE_COVERAGE
      makeTable(session, "createSourceCoverage");
    } finally {
      session.commit();
      session.close();
    }
  }


  protected static void makeTable(SqlSession session, String sql) {
    try {
      session.update(sql);
    } catch (PersistenceException e) {
      logger.warn("Ignoring error during create table. Statement=" + sql + "\nContinuing... " + e.toString());
    }
  }
}
