package gw.jacoco.sourcereport;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.BitSet;

/**
 * Make BitSet work better with byte arrays and blobs and improve toString for our purposes.
 */
public class CoverageLineSet extends BitSet {
  public CoverageLineSet(int size) {
    super(size);
  }

  public CoverageLineSet() {
  }


  // from http://bespokeblog.wordpress.com/2008/07/25/storing-and-retrieving-java-bitset-in-mysql-database/
  // and http://www.experts-exchange.com/Programming/Misc/Q_20403619.html
  // In Java 7 you can get the bitmap out directly...
  public static byte[] bitSetToByteArray(BitSet bitSet) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(bitSet.size());
    try {
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(bitSet);
    }
    catch (Exception ex) {
      throw new IllegalStateException("Unexpected error converting BitSet to bytes", ex);
    }
    return baos.toByteArray();

/*    byte[] bytes = new byte[bits.length()/8 + 1];
    for (int i = 0; i < bits.length(); i++) {
      if (bits.get(i)) {
        bytes[bytes.length - i/8 - 1] |= 1 << (i%8);
      }
    }
    return bytes;*/
  }

  public static BitSet fromByteArray(byte[] bytes) {
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
    return bits;
  }

  public String toString(){
    StringBuilder result = new StringBuilder("'");
    for (int i=0; i<size(); i++) result.append(get(i)?'x':' ');
    result.append("'");
    return result.toString();
  }
}
