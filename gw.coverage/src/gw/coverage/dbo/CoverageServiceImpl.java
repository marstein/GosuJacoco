package gw.coverage.dbo;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 */
public class CoverageServiceImpl implements CoverageService {

  private SqlSessionFactory sessionFactory;


  public CoverageServiceImpl() {
    Reader resourceAsReader = null;
    try {
      resourceAsReader = Resources.getResourceAsReader("sourceCoverageMap/xml/dbo/ibatisconfig.xml");
      sessionFactory = new SqlSessionFactoryBuilder().build(resourceAsReader);
      resourceAsReader.close();
    } catch (IOException e) {
      throw new IllegalStateException("could not initialize ibatis", e);
    }
  }

 public List<CoveredFile> findAll(SqlSession session) {
    List<CoveredFile> cf = (List<CoveredFile>) session.selectList(CoveredFile.class.getName()+".selectRunListsForFileNested", "Address%");
    return cf;
  }

  public SqlSession openSession() {
    return sessionFactory.openSession();
  }
}
