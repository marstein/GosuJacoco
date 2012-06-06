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

import gw.coverage.dbo.Branch;
import gw.coverage.dbo.CoverageMapper;
import gw.jacoco.sourcereport.CoverageLineSet;
import org.apache.ibatis.session.SqlSession;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.ILanguageNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.BitSet;

/**
 * Writes summary coverage data into the database.
 */
public class ClassRowWriter {

  private final CoverageMapper mapper;

  // CREATE TABLE coverage
  private static final CounterEntity[] COUNTERS = {CounterEntity.INSTRUCTION,
          CounterEntity.BRANCH, CounterEntity.LINE,
          CounterEntity.COMPLEXITY, CounterEntity.METHOD};

  private final ILanguageNames languageNames;

  private static Logger logger = LoggerFactory.getLogger("gw.jacoco.sqlreport.ClassRowWriter");

  private static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * Creates a new row connection1 that writes class information to the given CSV
   * connection1.
   *
   *
   *
   * @param languageNames converter for Java identifiers
   * @throws java.io.IOException in case of problems with the connection1
   */
  public ClassRowWriter(final CoverageMapper session, final ILanguageNames languageNames) throws IOException {
    this.languageNames = languageNames;
    this.mapper = session;
  }

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

  /**
   * Writes the class summary information as a row.
   *
   * @param name        name of the group
   * @param branchName  the perforce branch name
   * @param packageName vm name of the package
   * @param node        class coverage data    @throws java.io.IOException in case of problems with the writer
   */
  public void writeRow(String name, String branchName, final String changelist, final String suiteName, final String packageName, final IClassCoverage node, final Date suiteRunDate) throws IOException {
    final String className = languageNames.getClassName(node.getName(), node.getSignature(), node.getSuperName(), node.getInterfaceNames());
    logger.debug("writing class " + className + " to database");

    // Find the dimension IDs.
    Integer branch_id;
    if (null == (branch_id = mapper.selectBranch(branchName))) {
      mapper.insertBranch(branchName);
      branch_id = mapper.selectBranch(branchName);
    }
    Integer suite_id=null;
    if (null == (suite_id = mapper.selectSuite(suiteName))) {
       mapper.insertSuite(suiteName);
      suite_id = mapper.selectSuite(suiteName);
    }
    Integer changelist_id=null;
    if (null == (changelist_id = mapper.selectChangelist(changelist))) {
      mapper.insertChangelist(changelist);
      changelist_id = mapper.selectChangelist(changelist);
    }
    Integer package_id=null;
    if (null == (package_id = mapper.selectPackage(packageName))) {
      mapper.insertPackage(packageName);
      package_id = mapper.selectPackage(packageName);
    }
    Integer class_id=null;
    if (null == (class_id = mapper.selectClass(className))) {
      mapper.insertClass(className);
      class_id = mapper.selectClass(className);
    }
    mapper.insertPackageCoverage(branch_id, changelist_id, suite_id, package_id, class_id, suiteRunDate,
            node.getCounter(CounterEntity.INSTRUCTION).getCoveredCount(), node.getCounter(CounterEntity.INSTRUCTION).getMissedCount(),
            node.getCounter(CounterEntity.BRANCH).getCoveredCount(), node.getCounter(CounterEntity.BRANCH).getMissedCount(),
            node.getCounter(CounterEntity.LINE).getCoveredCount(), node.getCounter(CounterEntity.LINE).getMissedCount(),
            node.getCounter(CounterEntity.COMPLEXITY).getCoveredCount(), node.getCounter(CounterEntity.COMPLEXITY).getMissedCount(),
            node.getCounter(CounterEntity.METHOD).getCoveredCount(), node.getCounter(CounterEntity.METHOD).getMissedCount());
  }

  /*
  Same as writeRow, only we write a bitmap of line coverage to the database.
   */
  public void writeSourceRow(final String bundleName, final String branchName, final String changelist, final String suiteName, final String packageName, final ISourceFileCoverage sourceCoverage, final Date suiteRunDate) {
    final String fileName = sourceCoverage.getName();
    logger.debug("writing file " + fileName + " to database");

    // Find the dimension IDs.
    Integer branch_id;
    if (null == (branch_id = mapper.selectBranch(branchName))) {
      mapper.insertBranch(branchName);
      branch_id = mapper.selectBranch(branchName);
    }
    Integer suite_id=null;
    if (null == (suite_id = mapper.selectSuite(suiteName))) {
      mapper.insertSuite(suiteName);
      suite_id = mapper.selectSuite(suiteName);
    }
    Integer changelist_id=null;
    if (null == (changelist_id = mapper.selectChangelist(changelist))) {
      mapper.insertChangelist(changelist);
      changelist_id = mapper.selectChangelist(changelist);
    }
    Integer package_id=null;
    if (null == (package_id = mapper.selectPackage(packageName))) {
      mapper.insertPackage(packageName);
      package_id = mapper.selectPackage(packageName);
    }
    Integer filename_id=null;
    if (null == (filename_id = mapper.selectFilename(fileName))) {
      mapper.insertFilename(fileName);
      filename_id = mapper.selectFilename(fileName);
    }

    mapper.insertSourceCoverage(branch_id.intValue(), changelist_id.intValue(), suite_id.intValue(), package_id.intValue(), filename_id.intValue(), buildLineCoverageBytes(sourceCoverage), suiteRunDate,
            sourceCoverage.getCounter(CounterEntity.INSTRUCTION).getCoveredCount(), sourceCoverage.getCounter(CounterEntity.INSTRUCTION).getMissedCount(),
            sourceCoverage.getCounter(CounterEntity.BRANCH).getCoveredCount(), sourceCoverage.getCounter(CounterEntity.BRANCH).getMissedCount(),
            sourceCoverage.getCounter(CounterEntity.LINE).getCoveredCount(), sourceCoverage.getCounter(CounterEntity.LINE).getMissedCount(),
            sourceCoverage.getCounter(CounterEntity.COMPLEXITY).getCoveredCount(), sourceCoverage.getCounter(CounterEntity.COMPLEXITY).getMissedCount(),
            sourceCoverage.getCounter(CounterEntity.METHOD).getCoveredCount(), sourceCoverage.getCounter(CounterEntity.METHOD).getMissedCount());
  }

  private byte[] buildLineCoverageBytes(ISourceFileCoverage sourceCoverage) {
    BitSet lineCoverage = new BitSet(sourceCoverage.getLineCounter().getTotalCount());
    final int lastLine = sourceCoverage.getLastLine();
    for (int lineNumber = sourceCoverage.getFirstLine(); lineNumber < lastLine; lineNumber++) {
      int status = sourceCoverage.getLine(lineNumber).getStatus();
      lineCoverage.set(lineNumber, status == ICounter.FULLY_COVERED || status == ICounter.PARTLY_COVERED);
    }
    return CoverageLineSet.bitSetToByteArray(lineCoverage);
  }
}
