package gw.jacoco.sourcereport;

import gw.coverage.dbo.CoverageRun;
import gw.coverage.dbo.CoverageRunSummary;
import gw.coverage.dbo.CoveredFile;

/**
 * Analyze the runs against a file.
 * Each file contains coverage information (lines, instructions, etc covered),
 * and coverage information about each line.
 */
public class CoverageAnalysis {

  private CoveredFile coveredFile;

  private CoverageRunSummary nonPLRuns;

  private CoverageRunSummary thePLRuns;

  // The parameters for this report, like branch name, etc.
  private SourceReport sourceReport;

  public CoverageAnalysis(SourceReport sourceReport, CoveredFile coveredFile) {
    this.coveredFile = coveredFile;
    this.sourceReport = sourceReport;
    nonPLRuns = new CoverageRunSummary(coveredFile, "non_PL");
    thePLRuns = new CoverageRunSummary(coveredFile, "PL");
  }

  public CoverageRunSummary getNonPLRuns() {
    return nonPLRuns;
  }

  public CoverageRunSummary getThePLRuns() {
    return thePLRuns;
  }

  public CoverageRunSummary analyze() {
    // Take the non-PL suites and OR the line coverage. Then do the same for the PL classes.
    for (CoverageRun coverageRun : coveredFile.getRunList()) {
      if (coverageRun.getSuite().toLowerCase().startsWith("pl")) {
        thePLRuns.addRun(coverageRun);
      } else {
        nonPLRuns.addRun(coverageRun);
      }
    }
    return nonPLRuns.compareWith(thePLRuns, "pl - non_pl");
  }

  @Override
  public String toString() {
    return "CoverageAnalysis{" +
            "coveredFile=" + coveredFile + ", " +
            "nonPLCoverage=" + nonPLRuns.toString() + ", " +
            "PLCoverage=" + thePLRuns.toString() + ", " +
            "\nPL-nonPL=" + thePLRuns.compareWith(nonPLRuns, "comparison pl-nonpl").toString() +
            "}\n"
            ;
  }
}
