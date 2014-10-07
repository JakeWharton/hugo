package hugo.weaving.internal;

import java.math.BigInteger;
import junit.framework.TestCase;

public final class StringsTest extends TestCase {
  public void testNull() {
    assertEquals("null", Strings.toString(null));
  }

  public void testString() {
    assertEquals("\"String\"", Strings.toString("String"));
  }

  public void testUnprintableCharacters() {
    assertEquals("\"Str\\ning\"", Strings.toString("Str\ning"));
    assertEquals("\"\\n\\r\\t\\f\\b\\u202C\"", Strings.toString("\n\r\t\f\b\u202C"));
  }

  public void testObjects() {
    assertEquals("1", Strings.toString(new BigInteger("1")));
  }

  public void testByte() {
    byte primitive = (byte) 0xAB;
    assertEquals("0xAB", Strings.toString(primitive));
    Byte boxed = primitive;
    assertEquals("0xAB", Strings.toString(boxed));
  }

  public void testByteArrays() {
    byte[] primitive = { (byte) 0xAB, (byte) 0xBC, (byte) 0xCD, (byte) 0xDE, (byte) 0xEF };
    assertEquals("[0xAB, 0xBC, 0xCD, 0xDE, 0xEF]", Strings.toString(primitive));
    Byte[] boxed = { (byte) 0xAB, (byte) 0xBC, null, (byte) 0xDE, (byte) 0xEF };
    assertEquals("[0xAB, 0xBC, null, 0xDE, 0xEF]", Strings.toString(boxed));
  }

  public void testShortArrays() {
    short[] primitive = { 1, 2, 3, 4, 5 };
    assertEquals("[1, 2, 3, 4, 5]", Strings.toString(primitive));
    Short[] boxed = { 1, 2, null, 4, 5 };
    assertEquals("[1, 2, null, 4, 5]", Strings.toString(boxed));
  }

  public void testCharArrays() {
    char[] primitive = { 'a', 'b', 'c', 'd', 'e' };
    assertEquals("[a, b, c, d, e]", Strings.toString(primitive));
    Character[] boxed = { 'a', 'b', null, 'd', 'e' };
    assertEquals("[a, b, null, d, e]", Strings.toString(boxed));
  }

  public void testIntArrays() {
    int[] primitive = { 1, 2, 3, 4, 5 };
    assertEquals("[1, 2, 3, 4, 5]", Strings.toString(primitive));
    Integer[] boxed = { 1, 2, null, 4, 5 };
    assertEquals("[1, 2, null, 4, 5]", Strings.toString(boxed));
  }

  public void testLongArrays() {
    long[] primitive = { 1, 2, 3, 4, 5 };
    assertEquals("[1, 2, 3, 4, 5]", Strings.toString(primitive));
    Long[] boxed = { 1L, 2L, null, 4L, 5L };
    assertEquals("[1, 2, null, 4, 5]", Strings.toString(boxed));
  }

  public void testFloatArrays() {
    float[] primitive = { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f };
    assertEquals("[1.1, 2.2, 3.3, 4.4, 5.5]", Strings.toString(primitive));
    Float[] boxed = { 1.1f, 2.2f, null, 4.4f, 5.5f };
    assertEquals("[1.1, 2.2, null, 4.4, 5.5]", Strings.toString(boxed));
  }

  public void testDoubleArrays() {
    double[] primitive = { 1.1d, 2.2d, 3.3d, 4.4d, 5.5d };
    assertEquals("[1.1, 2.2, 3.3, 4.4, 5.5]", Strings.toString(primitive));
    Double[] boxed = { 1.1d, 2.2d, null, 4.4d, 5.5d };
    assertEquals("[1.1, 2.2, null, 4.4, 5.5]", Strings.toString(boxed));
  }

  public void testBooleanArrays() {
    boolean[] primitive = { true, false, true, false, true };
    assertEquals("[true, false, true, false, true]", Strings.toString(primitive));
    Boolean[] boxed = { true, false, null, false, true };
    assertEquals("[true, false, null, false, true]", Strings.toString(boxed));
  }

  public void testObjectArray() {
    Object[] array = { 1, true, "String", 1.1f, null, new BigInteger("1") };
    assertEquals("[1, true, \"String\", 1.1, null, 1]", Strings.toString(array));
  }

  public void testDeepObjectArray() {
    Object[] array = { 1, true, "String", new Object[] { 1.1f, "Nested" } };
    assertEquals("[1, true, \"String\", [1.1, \"Nested\"]]", Strings.toString(array));
  }

  public void testRecursiveObjectArray() {
    Object[] array = { 1, 2, 3, null };
    array[3] = array;
    assertEquals("[1, 2, 3, [...]]", Strings.toString(array));
  }
}
