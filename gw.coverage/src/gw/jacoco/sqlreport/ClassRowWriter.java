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

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.ILanguageNames;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.logging.Logger;

/**
 * Writes summary coverage data into the database.
 */
class ClassRowWriter {

  private final Connection connection;

  // CREATE TABLE coverage
  private static final CounterEntity[] COUNTERS = {CounterEntity.INSTRUCTION,
          CounterEntity.BRANCH, CounterEntity.LINE,
          CounterEntity.COMPLEXITY, CounterEntity.METHOD};

  private final ILanguageNames languageNames;

  private static Logger logger = Logger.getLogger("gw.jacoco.sqlreport.ClassRowWriter");

  private static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * Creates a new row connection1 that writes class information to the given CSV
   * connection1.
   *
   * @param languageNames converter for Java identifiers
   * @throws java.io.IOException in case of problems with the connection1
   */
  public ClassRowWriter(final Connection connection1, final ILanguageNames languageNames) throws IOException {
    this.languageNames = languageNames;
    this.connection = connection1;
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
    logger.fine("writing class " + className + " to database");
    StringBuilder sql = new StringBuilder().append("INSERT INTO PACKAGE_COVERAGE (");
    sql.append("branch, changelist, ");
    for (final CounterEntity entity : COUNTERS) {
      sql.append(entity.name()).append("_MISSED, ");
      sql.append(entity.name()).append("_COVERED, ");
    }
    sql.append("package, suite, class, suite_run_date) VALUES (");

    // values follow

    sql.append("'").append(branchName).append("', '").append(changelist).append("', ");
    for (final CounterEntity entity : COUNTERS) {
      final ICounter counter = node.getCounter(entity);
      sql.append(counter.getMissedCount()).append(", ");
      sql.append(counter.getCoveredCount()).append(", ");
    }
    sql.append("\'").append(packageName).append("\', ");
    sql.append("\'").append(suiteName).append("\', ");
    sql.append("\'").append(className).append("\', ");
    if (suiteRunDate == null) {
      sql.append("null)");
    } else {
      sql.append("\'").append(sdf.format(suiteRunDate)).append("')");
    }
    logger.finer(sql.toString());
    Statement statement = null;
    try {
      statement = connection.createStatement();
      statement.executeUpdate(sql.toString());
    } catch (SQLException e) {
      throw new IllegalStateException("Error writing to database " + statement, e);
    }
  }

  static String insertSourceSql = insertSourceCoverageStatement();

  /*
  Same as writeRow, only we write a bitmap of line coverage to the database.
   */
  public void writeSourceRow(final String bundleName, final String branchName, final String changelist, final String suiteName, final String packageName, final ISourceFileCoverage sourceCoverage, final Date suiteRunDate) {
    final String fileName = sourceCoverage.getName();
    logger.fine("writing file " + fileName + " to database");

    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(insertSourceSql.toString());
      int parameterCount = 1;
      statement.setString(parameterCount++, branchName);
      statement.setString(parameterCount++, changelist);
      statement.setString(parameterCount++, suiteName);
      statement.setString(parameterCount++, packageName);
      statement.setString(parameterCount++, fileName);
      statement.setBlob(parameterCount++, buildLineCoverageBitmap(connection, sourceCoverage));
      statement.setDate(parameterCount++, suiteRunDate);
      for (final CounterEntity entity3 : COUNTERS) {
        final ICounter counter = sourceCoverage.getCounter(entity3);
        statement.setInt(parameterCount++, counter.getMissedCount());
        statement.setInt(parameterCount++, counter.getCoveredCount());
      }
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new IllegalStateException("Error writing to database " + insertSourceSql, e);
    }
  }

  private static String insertSourceCoverageStatement() {
    StringBuilder sql = new StringBuilder().append("INSERT INTO SOURCE_COVERAGE (branch, changelist, suite, package, filename, line_coverage, suite_run_date, ");
    int counterCount = COUNTERS.length;
    for (final CounterEntity entity : COUNTERS) {
      sql.append(entity.name()).append("_MISSED, ");
      sql.append(entity.name()).append("_COVERED");
      if (counterCount-- > 1) {
        // Don't write the last comma, so we don't end with a comma.
        sql.append(", ");
      }
    }

    // Final field and values follow...
    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ");
    counterCount = COUNTERS.length;
    for (final CounterEntity entity2 : COUNTERS) {
      sql.append("?, ? ");
      if (counterCount-- > 1) {
        // Don't write the last comma, so we don't end with a comma.
        sql.append(", ");
      }
    }
    sql.append(")");
    logger.finer(sql.toString());
    return sql.toString();
  }

  private Blob buildLineCoverageBitmap(Connection connection, ISourceFileCoverage sourceCoverage) throws SQLException {
    BitSet lineCoverage = new BitSet(sourceCoverage.getLineCounter().getTotalCount());
    final int lastLine = sourceCoverage.getLastLine();
    for (int lineNumber = sourceCoverage.getFirstLine(); lineNumber < lastLine; lineNumber++) {
      int status = sourceCoverage.getLine(lineNumber).getStatus();
      lineCoverage.set(lineNumber, status == ICounter.FULLY_COVERED || status == ICounter.PARTLY_COVERED);
    }
    Blob blob = connection.createBlob();
    blob.setBytes(1, bitSetToByteArray(lineCoverage));
    return blob;
  }

  // from http://bespokeblog.wordpress.com/2008/07/25/storing-and-retrieving-java-bitset-in-mysql-database/
  // In Java 7 you can get the bitmap out directly...
  private static byte[] bitSetToByteArray(BitSet bits) {
    byte[] bytes = new byte[bits.length() / 8 + 1];
    for (int i = 0; i < bits.length(); i++) {
      if (bits.get(i)) {
        bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
      }
    }
    return bytes;
  }

}
