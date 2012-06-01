package gw.jacoco.sourcereport;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.ISO8601DateConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: mstein
 */
public class SourceReportCommandLineArguments {
  @Parameter(names = {"-branch", "-b"}, description = "branch name; e.g. d-pl-merge")
  public String branchName = "";

  @Parameter(names = {"-changelist", "-c"}, description = "change list number; e.g. 443211")
  public String changeList = "";

  @Parameter(names = {"-apps", "-a"}, description = "space separated list of applications (cc bc pc ab) to compare against PL")
  public List<String> apps = new ArrayList<String>();

  @Parameter(names="-runDate", description = "suite run date YYYY/MM/DD", converter = ISO8601DateConverter.class)
  public Date suiteRunDate;

  @Parameter(names = { "-jdbcconnection", "-jdbc" }, description = "JDBC connection string", required = false)
  public String jdbcConnection = "no jdbc connection";

  @Parameter(names = "-debug", description = "Debug mode")
  public boolean debug = false;
}
