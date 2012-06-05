package gw.coverage.dbo;

import gw.jacoco.sourcereport.CoverageLineSet;

/**
 * Hold non-pl and PL coverage averages and OR-ed coverage line-level bitmaps.
 */
public class CoverageRunSummary extends CoverageRun {

  // to calculate averages.
  int runsAdded = 1;

  public CoveredFile coveredFile;
  public String title;

  public CoverageLineSet getCoveredLineSet() {
    return coveredLineSet;
  }

  private CoverageLineSet coveredLineSet;

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
  }

  /**
   * Compare this run with another run. Subtract the otherRun and AND NOT the line coverage bitmap.
   *
   * @return a new coverage summary with the subtracted averages and the AND NOT bits.
   */
  public CoverageRunSummary compareWith(CoverageRunSummary otherRun, String comparisonTitle) {
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
    CoverageLineSet linesNotCoveredByPLTests = new CoverageLineSet(getCoveredLineSet().size());
    linesNotCoveredByPLTests.or(getCoveredLineSet()); // set to nonPLLineCoverage
    linesNotCoveredByPLTests.andNot(otherRun.getCoveredLineSet());
    summary.coveredLineSet = linesNotCoveredByPLTests;

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
            "CoverageRun=" + super.toString()+
            "runsAdded=" + runsAdded +
            ", coveredLineSet=\n" + coveredLineSet +
            "}";
  }
}
