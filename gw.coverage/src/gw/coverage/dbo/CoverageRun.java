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

  Integer instructionMissed;

  Integer instructionCovered;

  Integer branchMissed;

  Integer branchCovered;

  Integer lineMissed;

  Integer lineCovered;

  Integer complexityMissed;

  Integer complexityCovered;

  Integer methodMissed;

  Integer methodCovered;

  private byte[] lineCoverage;

  public CoverageRun() {
  }

  public CoverageRun(String branch, String changelist, String suite, Date suiteRunDate,
                     Integer instructionMissed, Integer instructionCovered, Integer branchMissed, Integer branchCovered, Integer lineMissed,
                     Integer lineCovered, Integer complexityMissed, Integer complexityCovered, Integer methodMissed, Integer methodCovered,
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

  public Integer getInstructionMissed() {
    return instructionMissed;
  }

  public Integer getInstructionCovered() {
    return instructionCovered;
  }

  public Integer getBranchMissed() {
    return branchMissed;
  }

  public Integer getBranchCovered() {
    return branchCovered;
  }

  public Integer getLineMissed() {
    return lineMissed;
  }

  public Integer getLineCovered() {
    return lineCovered;
  }

  public Integer getComplexityMissed() {
    return complexityMissed;
  }

  public Integer getComplexityCovered() {
    return complexityCovered;
  }

  public Integer getMethodMissed() {
    return methodMissed;
  }

  public Integer getMethodCovered() {
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

  public void setInstructionMissed(Integer instructionMissed) {
    this.instructionMissed = instructionMissed;
  }

  public void setInstructionCovered(Integer instructionCovered) {
    this.instructionCovered = instructionCovered;
  }

  public void setBranchMissed(Integer branchMissed) {
    this.branchMissed = branchMissed;
  }

  public void setBranchCovered(Integer branchCovered) {
    this.branchCovered = branchCovered;
  }

  public void setLineMissed(Integer lineMissed) {
    this.lineMissed = lineMissed;
  }

  public void setLineCovered(Integer lineCovered) {
    this.lineCovered = lineCovered;
  }

  public void setComplexityMissed(Integer complexityMissed) {
    this.complexityMissed = complexityMissed;
  }

  public void setComplexityCovered(Integer complexityCovered) {
    this.complexityCovered = complexityCovered;
  }

  public void setMethodMissed(Integer methodMissed) {
    this.methodMissed = methodMissed;
  }

  public void setMethodCovered(Integer methodCovered) {
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
