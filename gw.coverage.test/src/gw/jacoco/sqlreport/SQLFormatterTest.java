package gw.jacoco.sqlreport;

import gw.jacoco.help.ReportStructureTestDriver;
import org.apache.ibatis.session.SqlSession;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportVisitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for {@link gw.jacoco.sqlreport.SQLFormatter}.
 */
public class SQLFormatterTest {

  private static final String HEADER = "GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED";

  private ReportStructureTestDriver driver;

  private SQLFormatter formatter;

  private IReportVisitor visitor;

  private Connection connection;
  private SqlSession sqlSession;

  static org.slf4j.Logger logger = LoggerFactory.getLogger("sql formatter test");

  @Before
  public void setup() throws Exception {
    String connectString = "jdbc:h2:~/sqlformattertest.db;MODE=MSSQLServer;TRACE_LEVEL_FILE=2";
    SQLReportGenerator.createDBTables(connectString);
    connection = DriverManager.getConnection(connectString);
    sqlSession = SQLReportGenerator.initializeIBatis().openSession(connection);

    driver = new ReportStructureTestDriver();
    formatter = new SQLFormatter();
    visitor = formatter.createVisitor(sqlSession, "branch", "changelist", "test suite", new SimpleDateFormat().parse("07/10/12 4:5 PM, PDT"));
  }

  @After
  public void teardown() throws SQLException {
    Statement drop = connection.createStatement();
    drop.executeUpdate("drop table package_coverage");
    drop.executeUpdate("drop table source_coverage");
    drop.executeUpdate("drop table branch");
    drop.executeUpdate("drop table changelist");
    drop.executeUpdate("drop table suite");
    drop.executeUpdate("drop table class");
    drop.executeUpdate("drop table filename");
    drop.executeUpdate("drop table package");

    //connection.close();
    sqlSession.commit();
    sqlSession.close();
  }

  @Test
  public void testStructureWithGroup() throws IOException {
    driver.sendGroup(visitor);
    final List<String> lines = getLines();
    assertEquals("INSTRUCTION_MISSED=10 INSTRUCTION_COVERED=15 BRANCH_MISSED=1 BRANCH_COVERED=2 LINE_MISSED=0 LINE_COVERED=3 COMPLEXITY_MISSED=1 COMPLEXITY_COVERED=2 METHOD_MISSED=0 METHOD_COVERED=1 SUITE=test suite PACKAGE=org/jacoco/example CLASS=FooClass SUITE_RUN_DATE=2012-07-10 16:05:00.0 BRANCH=branch CHANGELIST=changelist ",
            lines.get(0));
  }

  @Test
  public void testStructureWithNestedGroups() throws IOException {
    driver.sendNestedGroups(visitor);
    final List<String> lines = getLines();
    assertEquals("INSTRUCTION_MISSED=10 INSTRUCTION_COVERED=15 BRANCH_MISSED=1 BRANCH_COVERED=2 LINE_MISSED=0 LINE_COVERED=3 COMPLEXITY_MISSED=1 COMPLEXITY_COVERED=2 METHOD_MISSED=0 METHOD_COVERED=1 SUITE=test suite PACKAGE=org/jacoco/example CLASS=FooClass SUITE_RUN_DATE=2012-07-10 16:05:00.0 BRANCH=branch CHANGELIST=changelist ",
            lines.get(0));
    assertEquals("INSTRUCTION_MISSED=10 INSTRUCTION_COVERED=15 BRANCH_MISSED=1 BRANCH_COVERED=2 LINE_MISSED=0 LINE_COVERED=3 COMPLEXITY_MISSED=1 COMPLEXITY_COVERED=2 METHOD_MISSED=0 METHOD_COVERED=1 SUITE=test suite PACKAGE=org/jacoco/example CLASS=FooClass SUITE_RUN_DATE=2012-07-10 16:05:00.0 BRANCH=branch CHANGELIST=changelist ",
            lines.get(1));
  }

  @Test
  public void testStructureWithBundleOnly() throws IOException {
    driver.sendBundle(visitor);
    final List<String> lines = getLines();
    assertEquals("INSTRUCTION_MISSED=10 INSTRUCTION_COVERED=15 BRANCH_MISSED=1 BRANCH_COVERED=2 LINE_MISSED=0 LINE_COVERED=3 COMPLEXITY_MISSED=1 COMPLEXITY_COVERED=2 METHOD_MISSED=0 METHOD_COVERED=1 SUITE=test suite PACKAGE=org/jacoco/example CLASS=FooClass SUITE_RUN_DATE=2012-07-10 16:05:00.0 BRANCH=branch CHANGELIST=changelist ",
            lines.get(0));
  }

  @Test
  public void testSetEncoding() throws Exception {
    formatter.setOutputEncoding("UTF-16");
    visitor = formatter.createVisitor(sqlSession, "branch", "changelist", "test suite", new SimpleDateFormat().parse("11/11/12 4:5 PM, PDT"));
    driver.sendBundle(visitor);
    final List<String> lines = getLines("UTF-16");
    assertEquals("INSTRUCTION_MISSED=10 INSTRUCTION_COVERED=15 BRANCH_MISSED=1 BRANCH_COVERED=2 LINE_MISSED=0 LINE_COVERED=3 COMPLEXITY_MISSED=1 COMPLEXITY_COVERED=2 METHOD_MISSED=0 METHOD_COVERED=1 SUITE=test suite PACKAGE=org/jacoco/example CLASS=FooClass SUITE_RUN_DATE=2012-11-11 16:05:00.0 BRANCH=branch CHANGELIST=changelist ", lines.get(0));
  }

  @Test
  public void testGetLanguageNames() throws Exception {
    ILanguageNames names = new ILanguageNames() {
      public String getPackageName(String vmname) {
        return null;
      }

      public String getQualifiedClassName(String vmname) {
        return null;
      }

      public String getClassName(String vmname, String vmsignature, String vmsuperclass, String[] vminterfaces) {
        return null;
      }

      public String getMethodName(String vmclassname, String vmmethodname, String vmdesc, String vmsignature) {
        return null;
      }
    };
    formatter.setLanguageNames(names);
    assertSame(names, formatter.getLanguageNames());
  }

  private List<String> getLines() throws IOException {
    return getLines("UTF-8");
  }

  private List<String> getLines(String encoding) throws IOException {
    final List<String> lines = new ArrayList<String>();
    try {
      Statement select = connection.createStatement();
      ResultSet result = select.executeQuery("SELECT INSTRUCTION_MISSED, INSTRUCTION_COVERED, BRANCH_MISSED, " +
              "BRANCH_COVERED, LINE_MISSED, LINE_COVERED, COMPLEXITY_MISSED, COMPLEXITY_COVERED, METHOD_MISSED, METHOD_COVERED, s.suite, p.package, cl.class, suite_run_date, b.branch, ch.changelist \n" +
              "FROM PACKAGE_COVERAGE  sc " +
              "join package p on sc.package_id = p.id " +
              "join changelist ch on sc.changelist_id = ch.id  " +
              "join class cl on sc.class_id=cl.id " +
              "join branch b on sc.branch_id = b.id " +
              "join suite s on sc.suite_id = s.id\n");
      while (result.next()) {
        StringBuffer line = new StringBuffer();
        for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
          logger.info("result.getMetaData().getColumnName(" + i + ")=" + result.getMetaData().getColumnName(i));
          String value = result.getObject(i) == null ? "null" : result.getObject(i).toString();
//          logger.info("result.getObject(i)="+value);
          line.append(result.getMetaData().getColumnName(i)).append("=").append(value).append(" ");
        }
        lines.add(line.toString());
      }
    } catch (Exception e) {
      throw new IllegalStateException("select broke ", e);
    }
    return lines;
  }
}
