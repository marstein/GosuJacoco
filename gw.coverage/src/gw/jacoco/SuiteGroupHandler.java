package gw.jacoco;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.ISourceFileLocator;

import java.io.IOException;
import java.util.Date;

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


  public SuiteGroupHandler(final ClassRowWriter writer, String groupName, String branchName, String changelist, String suiteName, Date suiteRunDate) {
    this.writer = writer;
    this.groupName = groupName;
    this.suiteRunDate = suiteRunDate;
    this.suiteName = suiteName;
    this.branchName = branchName;
    this.changelist = changelist;
	}

	public void visitBundle(final IBundleCoverage bundle, final ISourceFileLocator locator) throws IOException {
		final String name = appendName(bundle.getName());
		for (final IPackageCoverage p : bundle.getPackages()) {
			final String packageName = p.getName();
			for (final IClassCoverage classCoverage : p.getClasses()) {
				writer.writeRow(name, branchName, changelist, suiteName, packageName, classCoverage, suiteRunDate != null ? new java.sql.Date(suiteRunDate.getTime()) : null);
			}
		}
	}

  public IReportGroupVisitor visitGroup(String name) throws IOException {
    return new SuiteGroupHandler(writer, appendName(name), "", "", "", null);
  }

  public IReportGroupVisitor visitGroup(final String name, String suiteName, Date suiteRunDate) throws IOException {
		return new SuiteGroupHandler(writer, appendName(name), branchName, changelist, suiteName, suiteRunDate);
	}

	private String appendName(final String name) {
		return groupName == null ? name : (groupName + "/" + name);
	}

}
