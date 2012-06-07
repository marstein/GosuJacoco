package gw.jacoco.sqlreport;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.ISourceFileLocator;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Report visitor that handles coverage information for groups.
 */
class SuiteGroupHandler implements IReportGroupVisitor {

  private final ClassRowWriter writer;

  // Currently groupname and suite name are the same.
  private final String groupName;
  private final Date suiteRunDate;
  private final String suiteName;
  private String branchName;
  private String changelist;

  static private Logger logger = Logger.getLogger(SuiteGroupHandler.class.getName());

  public SuiteGroupHandler(final ClassRowWriter writer, String groupName, String branchName, String changelist, String suiteName, Date suiteRunDate) {
    this.writer = writer;
    this.groupName = groupName;
    this.suiteRunDate = suiteRunDate;
    this.suiteName = suiteName;
    this.branchName = branchName;
    this.changelist = changelist;
  }

  public void visitBundle(final IBundleCoverage bundle, final ISourceFileLocator locator) throws IOException {
    logger.info("Handling bundle " + bundle.getName() + " with " + bundle.getPackages().size() + " packages");
    long t0 = System.nanoTime();
    final String bundleName = appendName(bundle.getName());
    int fileCounter = 0;
    int classCounter = 0;
    for (final IPackageCoverage p : bundle.getPackages()) {
      final String packageName = p.getName();
      for (final IClassCoverage classCoverage : p.getClasses()) {
        classCounter++;
        writer.writeRow(bundleName, branchName, changelist, suiteName, packageName, classCoverage, suiteRunDate != null ? new Date(suiteRunDate.getTime()) : null);
      }
      for (final ISourceFileCoverage sourceCoverage : p.getSourceFiles()) {
        fileCounter++;
        writer.writeSourceRow(bundleName, branchName, changelist, suiteName, packageName, sourceCoverage, suiteRunDate != null ? new Date(suiteRunDate.getTime()) : null);
      }
    }
    logger.info("Bundle " + bundleName + " had " + fileCounter + " files and " + classCounter + " classes in ." + ((System.nanoTime() - t0)/1000) + "s");
  }

  public IReportGroupVisitor visitGroup(String name) throws IOException {
    return new SuiteGroupHandler(writer, appendName(name), branchName, changelist, suiteName, suiteRunDate);
  }


  private String appendName(final String name) {
    return groupName == null ? name : (groupName + "/" + name);
  }

}
