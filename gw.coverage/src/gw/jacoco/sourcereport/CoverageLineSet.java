package gw.jacoco.sourcereport;

import java.util.BitSet;

/**
 * Make BitSet work better with byte arrays and blobs and improve toString for our purposes.
 */
public class CoverageLineSet {
  private BitSet bitSet;

  public CoverageLineSet(int size) {
    bitSet = new BitSet(size);
  }

  public CoverageLineSet(BitSet bitSet1) {
    bitSet = (BitSet) bitSet1.clone();
  }


  public CoverageLineSet(CoverageLineSet set1) {
    bitSet = (BitSet) set1.bitSet.clone();
  }

  public CoverageLineSet() {
    bitSet = new BitSet();
  }

  // from http://bespokeblog.wordpress.com/2008/07/25/storing-and-retrieving-java-bitset-in-mysql-database/
  // and http://www.experts-exchange.com/Programming/Misc/Q_20403619.html
  // In Java 7 you can get the bitmap out directly...
  public static byte[] bitSetToByteArray(BitSet bitSet) {
/*
    ByteArrayOutputStream baos = new ByteArrayOutputStream(bitSet.size());
    try {
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(bitSet);
    }
    catch (Exception ex) {
      throw new IllegalStateException("Unexpected error converting BitSet to bytes", ex);
    }
    return baos.toByteArray();
*/

    byte[] bytes = new byte[bitSet.length()/8 + 1];
    for (int i = 0; i < bitSet.length(); i++) {
      if (bitSet.get(i)) {
        bytes[bytes.length - i/8 - 1] |= 1 << (i%8);
      }
    }
    return bytes;
  }

  public static CoverageLineSet fromByteArray(byte[] bytes) {
/*
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    BitSet result;
    try {
      ObjectInputStream ois = new ObjectInputStream(bais);
      result = (BitSet)ois.readObject();
    }
    catch (Exception ex) {
      throw new IllegalStateException("Unexpected error converting bytes to BitSet", ex);
    }
    return result;
*/

    BitSet bits = new BitSet();
    for (int i = 0; i < bytes.length*8; i++) {
      if ((bytes[bytes.length - i/8 - 1] & (1 << (i%8))) != 0) {
        bits.set(i);
      }
    }
    return new CoverageLineSet(bits);
  }

  public String toString() {
    StringBuilder result = new StringBuilder("bits='");
    for (int i = 0; i < size(); i++) {
      result.append(get(i) ? 'x' : ' ');
    }
    result.append("',");
    result.append("size=").append(size()).append(", ");
    result.append("cardinality=").append(cardinality());
    return result.toString();
  }


  public boolean get(int i) {
    return bitSet.get(i);
  }

  public void set(int i, boolean b) {
    bitSet.set(i, b);
  }

  public int cardinality() {
    return bitSet.cardinality();
  }

  public int size() {
    return bitSet.size();
  }

  public void or(CoverageLineSet bitSet1) {
    bitSet.or(bitSet1.bitSet);
  }

  public void andNot(CoverageLineSet bitSet1) {
    bitSet.andNot(bitSet1.bitSet);
  }
}
