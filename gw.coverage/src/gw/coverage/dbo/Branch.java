package gw.coverage.dbo;


public class Branch{
  private int id;
  private String branchName;

  public Branch(String b) { branchName = b; }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public String getBranchName() {
    return branchName;
  }
}
