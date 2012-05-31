package gw.jacoco.sourcereport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * User: mstein
 * <p/>
 * Pl-coverage -jdbc … -branch branch -changelist changelist-nr -product CC,BC
 * The tool would load the coverage data for the branches, changelists and products specified on the command line.
 * Then it needs to group them into files, and do a bitwise OR for the non-PL and PL suites.
 * Then it has to do an (non-PL AND NOT PL) comparison with the results, to get the lines covered by non-PL but not by PL.
 * Finally the 1 bits have to be counted to arrive at the "Lines covered by non-PL, not covered by PL" result column.
 * Having the output as CSV might be good for importing into Excel.
 */
public class SourceReport {
  private List<String> apps;
  private Date suiteRunDate;
  private String branchName;
  private String changelist;

  // jdbc database
  private String connectString;
  private String jdbcDrivername;
  private Connection reportConnection;

  //mybatis database
  private SqlSessionFactory sessionFactory;

  public static void main(final String[] args) throws IOException {
    final SourceReportCommandLineArguments commandLineArguments = new SourceReportCommandLineArguments();
    JCommander commander = null;
    try {
      commander = new JCommander(commandLineArguments, args);
    } catch (ParameterException e) {
      System.err.println("Command line parameter error! " + e.toString());
      new JCommander(commandLineArguments).usage();
      System.err.println("Please specify exec file, bytecode dirs, etc!");
      System.err.println("java -cp jacoco.jar;gw-coverage.jar;h2.jar -createTables -branch e-pr-merge -changelist 444222 -classesdir P:\\eng\\emerald\\pl\\ready\\active\\core\\gitmo\\configenv\\platform\\pl\\classes -execfile jc-coverage-gitmo-PLV3BareBone.exec -suite PLV3BareBone -jdbc jdbc:h2:~/coverage");
      System.exit(2);
    }

    final SourceReport report = new SourceReport();
    report.withBranchName(commandLineArguments.branchName)
            .withChangelist(commandLineArguments.changeList)
            .withJDBCConnection(commandLineArguments.jdbcConnection)
            .withSuiteRunDate(commandLineArguments.suiteRunDate)
            .withApps(commandLineArguments.apps);
    report.create();
  }

  // using the parameters gathered query the database for matching file records.
  private void create() {
    try {
      query();
    } catch (SQLException e) {
      throw new IllegalStateException("Error processing data!", e);
    }
  }


  private void queryIbatis(){
    try {
      Reader rdr = Resources.getResourceAsReader("SourceCoverageMapper.xml");
      sessionFactory = new SqlSessionFactoryBuilder().build(rdr);
      rdr.close();

      SqlSession session = sessionFactory.openSession();
        session.selectMap("", "");
    } catch (IOException e) {
      throw new IllegalStateException("database problem",e);
    }
  }


  private ResultSet query() throws SQLException {
    // Open the database connection.
    try {
      Class.forName(jdbcDrivername);
      this.reportConnection = DriverManager.getConnection(connectString);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not load " + jdbcDrivername + " database driver", e);
    } catch (SQLException e) {
      throw new IllegalStateException("Could not get " + jdbcDrivername + " database connection to " + connectString, e);
    }
    // Query for relevant files coverage data.
    String sql = "select " +
            "BRANCH_COVERED,\n" +
            "BRANCH_MISSED,\n" +
            "COMPLEXITY_COVERED,\n" +
            "COMPLEXITY_MISSED,\n" +
            "INSTRUCTION_COVERED,\n" +
            "INSTRUCTION_MISSED,\n" +
            "LINE_COVERED,\n" +
            "LINE_MISSED,\n" +
            "METHOD_COVERED,\n" +
            "METHOD_MISSED,\n" +
            "line_coverage,\n" +
            "changelist,\n" +
            "branch,\n" +
            "filename,\n" +
            "package,\n" +
            "suite,\n" +
            "suite_run_date datetime " +
            "from SOURCE_COVERAGE where true ";

    if (apps != null) {
      for (ListIterator<String> i = apps.listIterator(1); ; ) {
        sql += " AND substring(suite, 1, 2) = '" + i.next() + "'";
      }
    }

    if(changelist!=null){
      sql += " and changelist='"+changelist+"'";
    }

    if(branchName!=null){
      sql += " and branch='"+branchName+"'";
    }

    if (suiteRunDate!=null){
      sql += " and suite_run_date=?";
    }

    sql += " ORDER BY package, filename";

    PreparedStatement query = null;
    ResultSet result = null;
    try {
      query = reportConnection.prepareStatement(sql);

      if (suiteRunDate!=null){
        query.setDate(1, new java.sql.Date(suiteRunDate.getTime()));
      }

      result = query.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      throw e;
    }
    return result; // should return something that is easier to read...
  }


  public SourceReport() {
  }

  public SourceReport withApps(List<String> someApps) {
    this.apps = someApps;
    return this;
  }

  public SourceReport withSuiteRunDate(Date suiteRunDate) {
    this.suiteRunDate = suiteRunDate;
    return this;
  }

  public SourceReport withBranchName(String branchName) {
    this.branchName = branchName;
    return this;
  }

  public SourceReport withChangelist(String changelist) {
    this.changelist = changelist;
    return this;
  }

  public SourceReport withJDBCConnection(String jdbcConnectionString) {
    this.connectString = jdbcConnectionString;
    return this;
  }

  public SourceReport withJDBCDrivername(String jdbcDrivername) {
    this.jdbcDrivername = jdbcDrivername;
    return this;
  }
}
