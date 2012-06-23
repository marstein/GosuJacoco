package gw.coverage.dbo;

import gw.jacoco.sourcereport.CoverageLineSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold non-pl and PL coverage averages and OR-ed coverage line-level bitmaps.
 */
public class CoverageRunSummary extends CoverageRun {

  // to calculate averages.
  int runsAdded;

  //  public CoveredFile coveredFile;
  public String title;

  static public CoverageRunSummary EMPTY_SUMMARY = new CoverageRunSummary("EMPTY SUMMARY");

  public CoverageLineSet getCoveredLineSet() {
    return coveredLineSet;
  }

  private CoverageLineSet coveredLineSet;

  private static Logger logger = LoggerFactory.getLogger("SQLReportGenerator");

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

    // value = value*n/(n + 1) + newvalue/(n + 1)

    int runsAddedPlus = runsAdded + 1;
    logger.trace(title + ": adding coverageRun.instructionCovered " + instructionCovered + " to " + instructionCovered + " with " + runsAdded + " so far");
    instructionCovered = (instructionCovered*runsAdded + coverageRun.instructionCovered)/runsAddedPlus;
    instructionMissed = (instructionMissed*runsAdded + coverageRun.instructionMissed)/runsAddedPlus;
    branchMissed = (branchMissed*runsAdded + coverageRun.branchMissed)/runsAddedPlus;
    branchCovered = (branchCovered*runsAdded + coverageRun.branchCovered)/runsAddedPlus;
    lineMissed = (lineMissed*runsAdded + coverageRun.lineMissed)/runsAddedPlus;
    lineCovered = (lineCovered*runsAdded + coverageRun.lineCovered)/runsAddedPlus;
    complexityMissed = (complexityMissed*runsAdded + coverageRun.complexityMissed)/runsAddedPlus;
    complexityCovered = (complexityCovered*runsAdded + coverageRun.complexityCovered)/runsAddedPlus;
    methodMissed = (methodMissed*runsAdded + coverageRun.methodMissed)/runsAddedPlus;
    methodCovered = (methodCovered*runsAdded + coverageRun.methodCovered)/runsAddedPlus;
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
//    CoverageRunSummary summary = new CoverageRunSummary(this.coveredFile, comparisonTitle);
    CoverageRunSummary summary = new CoverageRunSummary(comparisonTitle);
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
            "runs" + ", " +
            "line coverage difference cardinality";
  }


  public String toCSV(CoveredFile coveredFile) {
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
            runsAdded + ", " +
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
            ", coveredLineSet=\n" + (coveredLineSet == null ? "null" : coveredLineSet.toString()) +
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
}
