package gw.jacoco.sourcereport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import gw.coverage.dbo.CoverageRunSummary;
import gw.coverage.dbo.CoverageService;
import gw.coverage.dbo.CoverageServiceImpl;
import gw.coverage.dbo.CoveredFile;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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

  // Have to figure out how to override ibatis config.
  private String connectString;

  final static org.slf4j.Logger logger = LoggerFactory.getLogger(SourceReport.class);

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
    for (CoveredFile coveredFile : queryIbatis()) {
      CoverageAnalysis coverageAnalysis = new CoverageAnalysis(coveredFile);
      CoverageRunSummary summary = coverageAnalysis.analyze();
      logger.debug(coverageAnalysis.toString());
      System.out.println(coverageAnalysis.getNonPLRuns().toCSV());
      System.out.println(coverageAnalysis.getThePLRuns().toCSV());
      System.out.println(summary.toCSV());
    }
  }


  private List<CoveredFile> queryIbatis() {
    CoverageService service = new CoverageServiceImpl();
    SqlSession session = service.openSession();
    List<CoveredFile> coveredFileList = service.findAll(session);
    session.close();
    return coveredFileList;
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
}
