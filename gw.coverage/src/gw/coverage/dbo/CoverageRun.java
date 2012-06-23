package gw.coverage.dbo;

import java.util.Date;

/**
 * A run of a test. Records coverage information.
 */
public class CoverageRun {

  private String branch;

  private String changelist;

  private String suite;

  private Date suiteRunDate;

  int instructionMissed;

  int instructionCovered;

  int branchMissed;

  int branchCovered;

  int lineMissed;

  int lineCovered;

  int complexityMissed;

  int complexityCovered;

  int methodMissed;

  int methodCovered;

  private byte[] lineCoverage;

  public CoverageRun() {
  }

  public CoverageRun(String branch, String changelist, String suite, Date suiteRunDate,
                     int instructionMissed, int instructionCovered, int branchMissed, int branchCovered, int lineMissed,
                     int lineCovered, int complexityMissed, int complexityCovered, int methodMissed, int methodCovered,
                     Object lineCoverage) {
    this.branch = branch;
    this.changelist = changelist;
    this.suite = suite;
    this.suiteRunDate = suiteRunDate;
    this.instructionMissed = instructionMissed;
    this.instructionCovered = instructionCovered;
    this.branchMissed = branchMissed;
    this.branchCovered = branchCovered;
    this.lineMissed = lineMissed;
    this.lineCovered = lineCovered;
    this.complexityMissed = complexityMissed;
    this.complexityCovered = complexityCovered;
    this.methodMissed = methodMissed;
    this.methodCovered = methodCovered;
    this.lineCoverage = (byte[]) lineCoverage;
  }

  public String getBranch() {
    return branch;
  }

  public String getChangelist() {
    return changelist;
  }

  public String getSuite() {
    return suite;
  }

  public Date getSuiteRunDate() {
    return suiteRunDate;
  }

  public int getInstructionMissed() {
    return instructionMissed;
  }

  public int getInstructionCovered() {
    return instructionCovered;
  }

  public int getBranchMissed() {
    return branchMissed;
  }

  public int getBranchCovered() {
    return branchCovered;
  }

  public int getLineMissed() {
    return lineMissed;
  }

  public int getLineCovered() {
    return lineCovered;
  }

  public int getComplexityMissed() {
    return complexityMissed;
  }

  public int getComplexityCovered() {
    return complexityCovered;
  }

  public int getMethodMissed() {
    return methodMissed;
  }

  public int getMethodCovered() {
    return methodCovered;
  }

  public byte[] getLineCoverage() {
    return lineCoverage;
  }

  public void setBranch(String branch) {
    this.branch = branch.intern();
  }

  public void setChangelist(String changelist) {
    this.changelist = changelist.intern();
  }

  public void setSuite(String suite) {
    this.suite = suite.intern();
  }

  public void setSuiteRunDate(Date suiteRunDate) {
    this.suiteRunDate = suiteRunDate;
  }

  public void setInstructionMissed(int instructionMissed) {
    this.instructionMissed = instructionMissed;
  }

  public void setInstructionCovered(int instructionCovered) {
    this.instructionCovered = instructionCovered;
  }

  public void setBranchMissed(int branchMissed) {
    this.branchMissed = branchMissed;
  }

  public void setBranchCovered(int branchCovered) {
    this.branchCovered = branchCovered;
  }

  public void setLineMissed(int lineMissed) {
    this.lineMissed = lineMissed;
  }

  public void setLineCovered(int lineCovered) {
    this.lineCovered = lineCovered;
  }

  public void setComplexityMissed(int complexityMissed) {
    this.complexityMissed = complexityMissed;
  }

  public void setComplexityCovered(int complexityCovered) {
    this.complexityCovered = complexityCovered;
  }

  public void setMethodMissed(int methodMissed) {
    this.methodMissed = methodMissed;
  }

  public void setMethodCovered(int methodCovered) {
    this.methodCovered = methodCovered;
  }

  public void setLineCoverage(byte[] lineCoverage) {
    this.lineCoverage = lineCoverage;
  }

  @Override
  public String toString() {
    return "CoverageRun{" +
            "branch='" + branch + '\'' +
            ", changelist='" + changelist + '\'' +
            ", suite='" + suite + '\'' +
            ", suiteRunDate=" + suiteRunDate +
            ", instructionMissed=" + instructionMissed +
            ", instructionCovered=" + instructionCovered +
            ", branchMissed=" + branchMissed +
            ", branchCovered=" + branchCovered +
            ", lineMissed=" + lineMissed +
            ", lineCovered=" + lineCovered +
            ", complexityMissed=" + complexityMissed +
            ", complexityCovered=" + complexityCovered +
            ", methodMissed=" + methodMissed +
            ", methodCovered=" + methodCovered +
            ", lineCoverage=" + lineCoverage +
            "}\n";
  }
}
