package gw.jacoco.sourcereport;

import org.junit.Before;
import org.junit.Test;

/**
 * test bitset stuff
 */
public class CoverageLineSetTest {

  private CoverageLineSet plcoverage;
  private CoverageLineSet nonplcoverage;

  @Before
  public void setup() {
    // 011100011 PL
    plcoverage = CoverageLineSet.fromByteArray(new byte[]{(byte) 0xe3});
    // 011110111 non-PL
    nonplcoverage = CoverageLineSet.fromByteArray(new byte[]{(byte) 0xf7});
    // 000010100 = 0x14 lines covered by non-pl AND NOT by pl
  }

  @Test
  public void testAndNot() {
    org.junit.Assert.assertEquals(7, nonplcoverage.cardinality());
    nonplcoverage.andNot(plcoverage);
    org.junit.Assert.assertEquals(2, nonplcoverage.cardinality());
  }
}

