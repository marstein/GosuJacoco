package gw.coverage.dbo;

import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * iBatis services for coverage files.
 */
public interface CoverageService {
  public SqlSession openSession();
  public List<CoveredFile> findAll(SqlSession session);
}
