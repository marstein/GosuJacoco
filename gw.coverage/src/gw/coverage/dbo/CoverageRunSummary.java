package gw.coverage.dbo;

import gw.jacoco.sourcereport.CoverageLineSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold non-pl and PL coverage averages and OR-ed coverage line-level bitmaps.
 */
public class CoverageRunSummary {

  // Sums, not individual measurements for one run.

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

  // to calculate averages.
  int runsAdded;

  public String title;

  static public CoverageRunSummary EMPTY_SUMMARY = new CoverageRunSummary("EMPTY SUMMARY");

  private CoverageLineSet coveredLineSet;

  private static Logger logger = LoggerFactory.getLogger("SQLReportGenerator");

  // If we subtract, we store the difference of the averages. Therefore runsAdded has to be one,
  // otherwise we'd divide again. So we store the sum of the runs added here.
  Integer summaryRunsAdded = null;

  public CoverageRunSummary(String title) {
    this.title = title;
    coveredLineSet = new CoverageLineSet();
    runsAdded = 0;
  }

  /**
   * Update the average with another run. OR the line coverage bitmap with what we have.
   *
   * @param coverageRun
   */
  public void addRun(CoverageRun coverageRun) {
    getCoveredLineSet().or(CoverageLineSet.fromByteArray(coverageRun.getLineCoverage()));
    logger.trace(title + ": adding coverageRun.instructionCovered " + instructionCovered + " to " + instructionCovered + " with " + runsAdded + " so far");
    instructionCovered += coverageRun.instructionCovered;
    instructionMissed += coverageRun.instructionMissed;
    branchMissed += coverageRun.branchMissed;
    branchCovered += coverageRun.branchCovered;
    lineMissed += coverageRun.lineMissed;
    lineCovered += coverageRun.lineCovered;
    complexityMissed += coverageRun.complexityMissed;
    complexityCovered += coverageRun.complexityCovered;
    methodMissed += coverageRun.methodMissed;
    methodCovered += coverageRun.methodCovered;
    runsAdded += 1;
  }

  /**
   * Compare this run with another run. Subtract the otherRun and AND NOT the line coverage bitmap.
   *
   * @return a new coverage summary with the subtracted averages and the AND NOT bits.
   */
  public CoverageRunSummary subtract(CoverageRunSummary otherRun, String comparisonTitle) {
    if (this.runsAdded == 0) {
      logger.error("Trying to compare an empty summary " + this.toString() + "with " + otherRun.toString());
      System.err.println("Is there no data in the database for " + title + "? Comparing an empty summary " + toString());
      return CoverageRunSummary.EMPTY_SUMMARY;
    }
    if (otherRun.runsAdded == 0) {
      logger.error("Trying to compare an empty summary " + otherRun.toString() + "with " + this.toString());
      System.err.println("Is there no data in the database for " + otherRun.title + "? Comparing an empty summary " + otherRun.toString());
      return CoverageRunSummary.EMPTY_SUMMARY;
    }

    CoverageRunSummary summary = new CoverageRunSummary(comparisonTitle);
    summary.instructionMissed = getInstructionMissed() - otherRun.getInstructionMissed();
    summary.instructionCovered = getInstructionCovered() - otherRun.getInstructionCovered();
    summary.branchMissed = getBranchMissed() - otherRun.getBranchMissed();
    summary.branchCovered = getBranchCovered() - otherRun.getBranchCovered();
    summary.lineMissed = getLineMissed() - otherRun.getLineMissed();
    summary.lineCovered = getLineCovered() - otherRun.getLineCovered();
    summary.complexityMissed = getComplexityMissed() - otherRun.getComplexityMissed();
    summary.complexityCovered = getComplexityCovered() - otherRun.getComplexityCovered();
    summary.methodMissed = getMethodMissed() - otherRun.getMethodMissed();
    summary.methodCovered = getMethodCovered() - otherRun.getMethodCovered();

    // Store the number of runs in a hacky special way.
    summary.summaryRunsAdded = runsAdded + otherRun.runsAdded;
    summary.runsAdded = 1;
    summary.coveredLineSet = new CoverageLineSet(otherRun.getCoveredLineSet());
    summary.coveredLineSet.andNot(getCoveredLineSet());

    return summary;
  }


  public static String toCSVTitle() {
    return "title, " +
            "PackageName, " +
            "FileName, " +
            "lineMissed, " +
            "lineCovered, " +
            "instructionMissed, " +
            "instructionCovered, " +
            "branchMissed, " +
            "branchCovered, " +
            "complexityMissed, " +
            "complexityCovered, " +
            "methodMissed, " +
            "methodCovered, " +
            "runs" + ", " +
            "line coverage difference cardinality";
  }


  public String toCSV(CoveredFile coveredFile) {
    return title + ", " +
            coveredFile.getPackageName() + ", " +
            coveredFile.getFileName() + ", " +
            getLineMissed() + ", " +
            getLineCovered() + ", " +
            getInstructionMissed() + ", " +
            getInstructionCovered() + ", " +
            getBranchMissed() + ", " +
            getBranchCovered() + ", " +
            getComplexityMissed() + ", " +
            getComplexityCovered() + ", " +
            getMethodMissed() + ", " +
            getMethodCovered() + ", " +
            (summaryRunsAdded != null ? summaryRunsAdded : runsAdded) + ", " +
            getCoveredLineSet().cardinality();
  }

  @Override
  public String toString() {
    if (runsAdded == 0) {
      return "CoverageRunSummary{" +
              "instructionMissed=n/a" +
              ", instructionCovered=n/a" +
              ", branchMissed=n/a" +
              ", branchCovered=n/a" +
              ", lineMissed=n/a" +
              ", lineCovered=n/a" +
              ", complexityMissed=n/a" +
              ", complexityCovered=n/a" +
              ", methodMissed=n/a" +
              ", methodCovered=n/a" +
              ", runsAdded=" + (summaryRunsAdded != null ? summaryRunsAdded : runsAdded) +
              ", summary=" + (summaryRunsAdded != null ? "yes" : "no") +
              ", coveredLineSet=\n" + (coveredLineSet == null ? "null" : coveredLineSet.toString()) +
              "}\n";
    } else {
      return "CoverageRunSummary{" +
              "instructionMissed=" + getInstructionMissed() +
              ", instructionCovered=" + getInstructionCovered() +
              ", branchMissed=" + getBranchMissed() +
              ", branchCovered=" + getBranchCovered() +
              ", lineMissed=" + getLineMissed() +
              ", lineCovered=" + getLineCovered() +
              ", complexityMissed=" + getComplexityMissed() +
              ", complexityCovered=" + getComplexityCovered() +
              ", methodMissed=" + getMethodMissed() +
              ", methodCovered=" + getMethodCovered() +
              ", runsAdded=" + (summaryRunsAdded != null ? summaryRunsAdded : runsAdded) +
              ", summary=" + (summaryRunsAdded != null ? "yes" : "no") +
              ", coveredLineSet=\n" + (coveredLineSet == null ? "null" : coveredLineSet.toString()) +
              "}\n";
    }
  }

  public boolean isEmpty() {
    return equals(EMPTY_SUMMARY);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CoverageRunSummary summary = (CoverageRunSummary) o;

    if (runsAdded != summary.runsAdded) {
      return false;
    }
    if (coveredLineSet != null ? !coveredLineSet.equals(summary.coveredLineSet) : summary.coveredLineSet != null) {
      return false;
    }
    if (!title.equals(summary.title)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = runsAdded;
    result = 31*result + title.hashCode();
    result = 31*result + (coveredLineSet != null ? coveredLineSet.hashCode() : 0);
    return result;
  }


  public CoverageLineSet getCoveredLineSet() {
    return coveredLineSet;
  }

  // Get Averages!
  public int getInstructionMissed() {
    return instructionMissed/runsAdded;
  }

  public int getInstructionCovered() {
    return instructionCovered/runsAdded;
  }

  public int getBranchMissed() {
    return branchMissed/runsAdded;
  }

  public int getBranchCovered() {
    return branchCovered/runsAdded;
  }

  public int getLineMissed() {
    return lineMissed/runsAdded;
  }

  public int getLineCovered() {
    return lineCovered/runsAdded;
  }

  public int getComplexityMissed() {
    return complexityMissed/runsAdded;
  }

  public int getComplexityCovered() {
    return complexityCovered/runsAdded;
  }

  public int getMethodMissed() {
    return methodMissed/runsAdded;
  }

  public int getMethodCovered() {
    return methodCovered/runsAdded;
  }
}
