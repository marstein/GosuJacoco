package gw.coverage.dbo;

import java.util.List;

/**
 * A file covered by source code coverage, has package and file name.
 * It also contains a list of 'runs' that happened in a branch, changelist, etc
 */
public class CoveredFile {

  private String packageName;
  private String fileName;

  private List<CoverageRun> runList;

  public CoveredFile() {
  }

  public CoveredFile(String packageName, String fileName) {
    this.packageName = packageName;
    this.fileName = fileName;
  }


  public List<CoverageRun> getRunList() {
    return runList;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getFileName() {
    return fileName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setRunList(List<CoverageRun> runList) {
    this.runList = runList;
  }

  @Override
  public String toString() {
    return "CoveredFile{" +
            "packageName='" + packageName + '\'' +
            ", fileName='" + fileName + '\'' +
            ", runList=" + runList +
            '}';
  }
}
