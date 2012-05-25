package gw.jacoco;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.ISO8601DateConverter;
import com.beust.jcommander.internal.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: mstein
 */
public class CommandLineArguments {
  @Parameter(names = {"-suite", "-s"}, description = "suite name; e.g. plv3barebones", required = true)
  public String suiteName;

  @Parameter(names = "-debug", description = "Debug mode")
  public boolean debug = false;

  @Parameter(names = {"-branch", "-b"}, description = "branch name; e.g. d-pl-merge", required = true)
  public String branchName = "";

  @Parameter(names = {"-changelist", "-c"}, description = "change list number; e.g. 443211")
  public String changeList = "";

  @Parameter(names = { "-jdbcconnection", "-jdbc" }, description = "JDBC connection string", required = true)
  public String jdbcConnection = "no jdbc connection";

  @Parameter(names = { "-jdbcdriver", "-jd" }, description = "JDBC driver name, like org.h2.Driver", required = true)
  public String jdbcDrivername = "no jdbc driver";

  @Parameter(names = { "-execfile", "-e"}, description = "Jacoco execution file name", converter = FileConverter.class, required = true)
  public File execFile;

  @Parameter(names = "-classesdir", description =  "class directory, jar file. Can occur multiple times", converter = FileConverter.class, required = true)
  public List<File> classesDirs = new ArrayList<File>();

  @Parameter(names="-runDate", description = "suite run date YYYY/MM/DD", converter = ISO8601DateConverter.class)
  public Date suiteRunDate;

  @Parameter(names = "-createTables", description = "create the tables to write into before running. needs DDL permissions.")
  public Boolean createTables = false;

  @Parameter(description = "Gitmo result directory - just one", converter = FileConverter.class)
  public List<File> directory = new ArrayList<File>();
}