package gw.coverage.dbo;

import gw.jacoco.sourcereport.CoverageLineSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold non-pl and PL coverage averages and OR-ed coverage line-level bitmaps.
 */
public class CoverageRunSummary extends CoverageRun {

  // to calculate averages.
  int runsAdded = 1;

  public CoveredFile coveredFile;
  public String title;

  static public CoverageRunSummary EMPTY_SUMMARY = new CoverageRunSummary(new CoveredFile("no package", "no file"), "EMPTY SUMMARY");

  public CoverageLineSet getCoveredLineSet() {
    return coveredLineSet;
  }

  private CoverageLineSet coveredLineSet;

  private static Logger logger = LoggerFactory.getLogger("SQLReportGenerator");

  public CoverageRunSummary(CoveredFile coveredFile, String title) {
    this.coveredFile = coveredFile;
    this.title = title;
    this.instructionMissed = 0;
    this.instructionCovered = 0;
    this.branchMissed = 0;
    this.branchCovered = 0;
    this.lineMissed = 0;
    this.lineCovered = 0;
    this.complexityMissed = 0;
    this.complexityCovered = 0;
    this.methodMissed = 0;
    this.methodCovered = 0;
    this.coveredLineSet = new CoverageLineSet();
  }

  /**
   * Update the average with another run. OR the line coverage bitmap with what we have.
   *
   * @param coverageRun
   */
  public void addRun(CoverageRun coverageRun) {
    getCoveredLineSet().or(CoverageLineSet.fromByteArray(coverageRun.getLineCoverage()));

    instructionCovered += coverageRun.instructionCovered/runsAdded;
    instructionMissed += coverageRun.instructionMissed/runsAdded;
    branchMissed += coverageRun.branchMissed/runsAdded;
    branchCovered += coverageRun.branchCovered/runsAdded;
    lineMissed += coverageRun.lineMissed/runsAdded;
    lineCovered += coverageRun.lineCovered/runsAdded;
    complexityMissed += coverageRun.complexityMissed/runsAdded;
    complexityCovered += coverageRun.complexityCovered/runsAdded;
    methodMissed += coverageRun.methodMissed/runsAdded;
    methodCovered += coverageRun.methodCovered/runsAdded;
    runsAdded += 1;
  }

  /**
   * Compare this run with another run. Subtract the otherRun and AND NOT the line coverage bitmap.
   *
   * @return a new coverage summary with the subtracted averages and the AND NOT bits.
   */
  public CoverageRunSummary subtract(CoverageRunSummary otherRun, String comparisonTitle) {
    if (this.runsAdded == 1) {
      logger.error("Trying to compare an empty summary " + this.toString() + "with " + otherRun.toString());
      System.err.println("Is there no data in the database for " + title + "? Comparing an empty summary " + toString());
      return CoverageRunSummary.EMPTY_SUMMARY;
    }
    if (otherRun.runsAdded == 1) {
      logger.error("Trying to compare an empty summary " + otherRun.toString() + "with " + this.toString());
      System.err.println("Is there no data in the database for " + otherRun.title + "? Comparing an empty summary " + otherRun.toString());
      return CoverageRunSummary.EMPTY_SUMMARY;
    }
    CoverageRunSummary summary = new CoverageRunSummary(this.coveredFile, comparisonTitle);
    summary.instructionMissed = instructionMissed - otherRun.instructionMissed;
    summary.instructionCovered = instructionCovered - otherRun.instructionCovered;
    summary.branchMissed = branchMissed - otherRun.branchMissed;
    summary.branchCovered = branchCovered - otherRun.branchCovered;
    summary.lineMissed = lineMissed - otherRun.lineMissed;
    summary.lineCovered = lineCovered - otherRun.lineCovered;
    summary.complexityMissed = complexityMissed - otherRun.complexityMissed;
    summary.complexityCovered = complexityCovered - otherRun.complexityCovered;
    summary.methodMissed = methodMissed - otherRun.methodMissed;
    summary.methodCovered = methodCovered - otherRun.methodCovered;
    summary.runsAdded = runsAdded + otherRun.runsAdded;
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
            "line coverage difference CoveredLineSet cardinality";
  }


  public String toCSV() {
    return title + ", " +
            coveredFile.getPackageName() + ", " +
            coveredFile.getFileName() + ", " +
            lineMissed + ", " +
            lineCovered + ", " +
            instructionMissed + ", " +
            instructionCovered + ", " +
            branchMissed + ", " +
            branchCovered + ", " +
            complexityMissed + ", " +
            complexityCovered + ", " +
            methodMissed + ", " +
            methodCovered + ", " +
            getCoveredLineSet().cardinality();
  }

  @Override
  public String toString() {
    return "CoverageRunSummary{" +
            "instructionMissed=" + instructionMissed +
            ", instructionCovered=" + instructionCovered +
            ", branchMissed=" + branchMissed +
            ", branchCovered=" + branchCovered +
            ", lineMissed=" + lineMissed +
            ", lineCovered=" + lineCovered +
            ", complexityMissed=" + complexityMissed +
            ", complexityCovered=" + complexityCovered +
            ", methodMissed=" + methodMissed +
            ", methodCovered=" + methodCovered +
            ", runsAdded=" + runsAdded +
            ", coveredLineSet=\n" + coveredLineSet +
            "}\n";
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
    if (coveredFile != null ? !coveredFile.equals(summary.coveredFile) : summary.coveredFile != null) {
      return false;
    }
    if (title != null ? !title.equals(summary.title) : summary.title != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = runsAdded;
    result = 31*result + (coveredFile != null ? coveredFile.hashCode() : 0);
    result = 31*result + (title != null ? title.hashCode() : 0);
    return result;
  }
}
