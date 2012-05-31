package gw.jacoco.sourcereport;

import java.sql.ResultSet;
import java.util.BitSet;

/**
 * User: mstein
 * <p/>
 * Hold and process query results.
 */
public class SourceCoverageX {

  private ResultSet resultSet;

  public SourceCoverageX(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  public class SourceFileCoverage {
    String branch;
    int BRANCH_COVERED;
    int BRANCH_MISSED;
    String changelist;
    int COMPLEXITY_COVERED;
    int COMPLEXITY_MISSED;
    String filename;
    int INSTRUCTION_COVERED;
    int INSTRUCTION_MISSED;
    BitSet line_coverage;
    int LINE_COVERED;
    int LINE_MISSED;
    int METHOD_COVERED;
    int METHOD_MISSED;
    String packageName;
    int suite;
    int suite_run_date;
  }

}
