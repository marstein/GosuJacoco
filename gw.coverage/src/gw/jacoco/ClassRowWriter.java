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

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.ILanguageNames;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
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

  private static Logger logger = Logger.getLogger("ClassRowWriter");

  private static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * Creates a new row connection1 that writes class information to the given CSV
   * connection1.
   *
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
   *
   * @param name  name of the group
   * @param branchName
   * @param packageName vm name of the package
   * @param node        class coverage data    @throws java.io.IOException in case of problems with the writer
   */
  public void writeRow(String name, String branchName, final String changelist, final String suiteName, final String packageName, final IClassCoverage node, final Date suiteRunDate) throws IOException {
    final String className = languageNames.getClassName(node.getName(), node.getSignature(), node.getSuperName(), node.getInterfaceNames());
    logger.fine("writing class "+className+" to database");
    StringBuilder sql = new StringBuilder().append("INSERT INTO COVERAGE (");
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
    if(suiteRunDate == null) {
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
      throw new IllegalStateException("Error writing to database "+statement, e);
    }
  }
}
