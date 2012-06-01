package gw.jacoco.sourcereport;

import gw.coverage.dbo.CoverageRun;
import gw.coverage.dbo.CoveredFile;
import gw.jacoco.sqlreport.ClassRowWriter;

import java.util.BitSet;

/**
 * Analyze the runs against a file.
 * Each file contains coverage information (lines, instructions, etc covered),
 * and coverage information about each line.
 */
public class CoverageAnalysis {

  private CoveredFile coveredFile;

  private BitSet nonPLLineCoverage = new BitSet();
  private BitSet thePLLineCoverage = new BitSet();

  public CoverageAnalysis(CoveredFile coveredFile){
    this.coveredFile = coveredFile;
  }

  public void analyze(){
    // Take the non-PL suites and OR the line coverage. Then do the same for the PL classes.
    for (CoverageRun coverageRun: coveredFile.getRunList()) {
      if (coverageRun.getSuite().toLowerCase().startsWith("pl")) {
        thePLLineCoverage.or(ClassRowWriter.fromByteArray(coverageRun.getLineCoverage()));
      } else {
        nonPLLineCoverage.or(ClassRowWriter.fromByteArray(coverageRun.getLineCoverage()));
      }
    }
  }

  @Override
  public String toString() {
    BitSet linesNotCoveredByPLTests = new BitSet(nonPLLineCoverage.size());
    linesNotCoveredByPLTests.or(nonPLLineCoverage); // set to nonPLLineCoverage
    linesNotCoveredByPLTests.andNot(thePLLineCoverage);
    return "CoverageAnalysis{" +
            "coveredFile=" + coveredFile +
            "}\n" +
            "non-PL:\t"+ nonPLLineCoverage.toString() +
            "\nPL:\t"+ thePLLineCoverage.toString() +
            "\nLines not covered by PL tests: "+linesNotCoveredByPLTests.cardinality()+" lines: "+linesNotCoveredByPLTests.toString()
            ;
  }
}
