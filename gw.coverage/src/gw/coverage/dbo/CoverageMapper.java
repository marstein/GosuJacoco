package gw.coverage.dbo;

import gw.jacoco.sourcereport.SourceReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;

import java.sql.Date;
import java.util.List;

/**
 * iBatis services for coverage files.
 */
public interface CoverageMapper {
  public List<CoveredFile> findAllCoveredFiles(@Param("branch") String branch, @Param("changelist") String changelist,
                                               @Param("apps") List<String> apps, @Param("file") String file,
                                               @Param("runDate")Date runDate);
}
