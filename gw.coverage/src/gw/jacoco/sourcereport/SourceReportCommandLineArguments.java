package gw.jacoco.sourcereport;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.ISO8601DateConverter;

import java.sql.Date;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: mstein
 */
public class SourceReportCommandLineArguments {
  @Parameter(names = {"-branch", "-b"}, description = "branch name; e.g. d-pl-merge")
  public String branchName = null;

  @Parameter(names = {"-changelist", "-c"}, description = "change list number; e.g. 443211")
  public String changeList = null;

  @Parameter(names = {"-apps", "-a"}, description = "space separated list of applications (lowercase cc bc pc ab) to compare against PL. Will always load PL suites.")
  public List<String> apps = null;

  @Parameter(names = {"-file", "-f"}, description = "file name pattern for database LIKE statement (i.e. %) to select certain files")
  public String filePattern = null;

  @Parameter(names = "-runDate", description = "suite run date YYYY/MM/DD", converter = ISO8601DateConverter.class)
  public Date suiteRunDate = null;

  @Parameter(names = {"-jdbcconnection", "-jdbc"}, description = "JDBC connection string", required = false)
  public String jdbcConnection = "no jdbc connection";

  @Parameter(names = "-debug", description = "Debug mode")
  public boolean debug = false;
}
