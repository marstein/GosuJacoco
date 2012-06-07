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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CoveredFile that = (CoveredFile) o;

    if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) {
      return false;
    }
    if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null) {
      return false;
    }
    if (runList != null ? !runList.equals(that.runList) : that.runList != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = packageName != null ? packageName.hashCode() : 0;
    result = 31*result + (fileName != null ? fileName.hashCode() : 0);
    return result;
  }
}
