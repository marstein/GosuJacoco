package gw.coverage.dbo;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * iBatis services for coverage files.
 */
public interface CoverageMapper {

  public List<CoveredFile> findAllCoveredFiles(@Param("branch") String branch, @Param("changelist") String changelist,
                                               @Param("apps") List<String> apps, @Param("file") String file,
                                               @Param("runDate") Date runDate);

  public Integer insertBranch(@Param("branchName") String branchName);

  public Integer selectBranch(@Param("branchName") String branchName);

  public Integer insertSuite(@Param("suite") String suite);

  public Integer selectSuite(@Param("suite") String suite);

  public Integer insertChangelist(@Param("changelist") String changelist);

  public Integer selectChangelist(@Param("changelist") String changelist);

  public Integer insertPackage(@Param("package") String packageName);

  public Integer selectPackage(@Param("package") String packageName);

  public Integer insertFilename(@Param("filename") String filename);

  public Integer selectFilename(@Param("filename") String filename);

  public Integer insertClass(@Param("class") String className);

  public Integer selectClass(@Param("class") String className);

  public int insertSourceCoverage(@Param("branch_id") int branch_id, @Param("changelist_id") int changelist_id,
                                  @Param("suite_id") int suite_id, @Param("package_id") int package_id, @Param("filename_id") int filename_id,
                                  @Param("line_coverage") byte[] line_coverage, @Param("suite_run_date") Date suite_run_date,
                                  @Param("INSTRUCTION_MISSED") int instruction_missed, @Param("INSTRUCTION_COVERED") int instruction_covered,
                                  @Param("BRANCH_MISSED") int branch_missed, @Param("BRANCH_COVERED") int branch_covered,
                                  @Param("LINE_MISSED") int line_missed, @Param("LINE_COVERED") int line_covered, @Param("COMPLEXITY_MISSED") int complexity_missed,
                                  @Param("COMPLEXITY_COVERED") int complexity_covered, @Param("METHOD_MISSED") int method_missed,
                                  @Param("METHOD_COVERED") int method_covered);

  public int insertPackageCoverage(@Param("branch_id") int branch_id, @Param("changelist_id") int changelist_id,
                                   @Param("suite_id") int suite_id, @Param("package_id") int package_id, @Param("class_id") int class_id,
                                   @Param("suite_run_date") Date suite_run_date,
                                   @Param("INSTRUCTION_MISSED") int instruction_missed, @Param("INSTRUCTION_COVERED") int instruction_covered,
                                   @Param("BRANCH_MISSED") int branch_missed, @Param("BRANCH_COVERED") int branch_covered,
                                   @Param("LINE_MISSED") int line_missed, @Param("LINE_COVERED") int line_covered, @Param("COMPLEXITY_MISSED") int complexity_missed,
                                   @Param("COMPLEXITY_COVERED") int complexity_covered, @Param("METHOD_MISSED") int method_missed,
                                   @Param("METHOD_COVERED") int method_covered);
}
