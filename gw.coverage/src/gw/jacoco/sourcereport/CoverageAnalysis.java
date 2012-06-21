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

  public CoverageAnalysis(SourceReport sourceReport, CoveredFile coveredFile) {
    this.coveredFile = coveredFile;
  }

  public CoverageRunSummary getNonPLRuns() {
    return nonPLRuns;
  }

  public CoverageRunSummary getThePLRuns() {
    return thePLRuns;
  }

  public CoverageRunSummary analyze() {
    thePLRuns = new CoverageRunSummary("PL");
    nonPLRuns = new CoverageRunSummary("non_PL");
    // Take the non-PL suites and OR the line coverage. Then do the same for the PL classes.
    for (CoverageRun coverageRun : coveredFile.getRunList()) {
      if (coverageRun.getSuite().toLowerCase().startsWith("pl")) {
        thePLRuns.addRun(coverageRun);
      } else {
        nonPLRuns.addRun(coverageRun);
      }
    }
    return thePLRuns.subtract(nonPLRuns, "pl - non_pl");
  }

  @Override
  public String toString() {
    return "CoverageAnalysis{" +
            "\ncoveredFile=" + coveredFile + ", " +
            "\nnonPLCoverage=" + (nonPLRuns == null ? "null" : nonPLRuns.toString()) + ", " +
            "\nPLCoverage=" + (thePLRuns == null ? "null" : thePLRuns.toString()) + ", " +
            "\nPL-nonPL=" + (thePLRuns == null ? "null" : thePLRuns.subtract(nonPLRuns, "comparison pl-nonpl").toString()) +
            "}\n"
            ;
  }
}
