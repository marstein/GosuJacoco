package gw.jacoco.sqlreport;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.JavaNames;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Report formatter that will create a single CSV file. By default the filename
 * used will be the name of the session.
 */
public class SQLFormatter {
  private ILanguageNames languageNames = new JavaNames();

  private String outputEncoding = "UTF-8";

  /**
   * Sets the implementation for language name display. Java language names
   * are defined by default.
   *
   * @param languageNames converter for language specific names
   */
  public void setLanguageNames(final ILanguageNames languageNames) {
    this.languageNames = languageNames;
  }

  /**
   * Returns the language names call-back used in this report.
   *
   * @return language names
   */
  public ILanguageNames getLanguageNames() {
    return languageNames;
  }

  /**
   * Sets the encoding used for generated CSV document. Default is UTF-8.
   *
   * @param outputEncoding CSV output encoding
   */
  public void setOutputEncoding(final String outputEncoding) {
    this.outputEncoding = outputEncoding;
  }

  /**
   * Creates a new visitor to write a report to the given stream.
   *
   * @param connection output SQL connection to write the report to
   * @return visitor to emit the report data to
   * @throws java.io.IOException in case of problems with the output stream
   */
  public IReportVisitor createVisitor(final Connection connection, final String branchName, final String changelist, String suiteName, Date suiteRunDate)
          throws IOException {
    final ClassRowWriter rowWriter = new ClassRowWriter(connection, languageNames);

    class Visitor extends SuiteGroupHandler implements IReportVisitor {

      Visitor(String branchName, String changelist, String suiteName, Date suiteRunDate) {
        super(rowWriter, suiteName, branchName, changelist, suiteName, suiteRunDate);
      }

      public void visitInfo(final List<SessionInfo> sessionInfos, final Collection<ExecutionData> executionData)
              throws IOException {
        // Info not used for SQL report
      }

      public void visitEnd() throws IOException {
      }
    }
    return new Visitor(branchName, changelist, suiteName, suiteRunDate);
  }
}
