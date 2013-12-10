package hugo.weaving.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class Strings {

  public static String toString(final Object o) {
    if (o == null) {
      return "null";
    }

    Class<?> clazz = o.getClass();

    if (o instanceof String) {
      return '"' + o.toString() + '"';
    }

    if (Byte.class == clazz) {
      return byteToString((Byte) o);
    }

    if (clazz.isArray()) {
      Class<?> innerClazz = clazz.getComponentType();
      if (innerClazz.isArray()) {
        return deepArrayToString((Object[]) o);
      }

      return arrayToString(innerClazz, o);
    }
    return o.toString();
  }

  private static String arrayToString(final Class<?> clazz, final Object o) {
    // byte/Byte array
    if (byte.class == clazz) {
      return byteArrayToString((byte[]) o);
    }
    if (Byte.class == clazz) {
      return byteArrayToString((Byte[]) o);
    }

    // other arrays
    if (short.class == clazz) {
      return Arrays.toString((short[]) o);
    }
    if (char.class == clazz) {
      return Arrays.toString((char[]) o);
    }
    if (int.class == clazz) {
      return Arrays.toString((int[]) o);
    }
    if (long.class == clazz) {
      return Arrays.toString((long[]) o);
    }
    if (float.class == clazz) {
      return Arrays.toString((float[]) o);
    }
    if (double.class == clazz) {
      return Arrays.toString((double[]) o);
    }
    if (boolean.class == clazz) {
      return Arrays.toString((boolean[]) o);
    }
    // element is an array of object references
    return Arrays.toString((Object[]) o);
  }

  /** A more human-friendly version of Arrays#toString(byte[]) that uses hex representation. */
  private static String byteArrayToString(final byte[] bytes) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(byteToString(bytes[i]));
    }
    return builder.append(']').toString();
  }

  /**
   * A more human-friendly version of Arrays#toString(Object[]) for Byte arrays
   * that uses hex representation.
   * */
  private static String byteArrayToString(final Byte[] bytes) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(byteToString(bytes[i]));
    }
    return builder.append(']').toString();
  }

  /** A more human-friendly string representation of a Byte. */
  private static String byteToString(final Byte b) {
    return String.format("%02x", b);
  }

  /** A more human-friendly string representation of a byte. */
  private static String byteToString(final byte b) {
    return byteToString(Byte.valueOf(b));
  }

  /**
   * Copy of {@link Arrays#deepToString(Object[])} modified for more human-friendly representation.
   * @param a Array
   * @return human-friendly string representation
   * @see Arrays#deepToString(Object[])
   */
  private static String deepArrayToString(final Object[] a) {
    int bufLen = 20 * a.length;
    if (a.length != 0 && bufLen <= 0)
      bufLen = Integer.MAX_VALUE;
    StringBuilder buf = new StringBuilder(bufLen);
    deepArrayToString(a, buf, new HashSet<Object[]>());
    return buf.toString();
  }

  /**
   * Copy of Arrays#deepToString(Object[], StringBuilder, Set<Object[]>) modified
   * for more human-friendly representation.
   */
  private static void deepArrayToString(final Object[] a, final StringBuilder buf,
                                          final Set<Object[]> dejaVu) {
    if (a == null) {
      buf.append("null");
      return;
    }
    int iMax = a.length - 1;
    if (iMax == -1) {
      buf.append("[]");
      return;
    }

    dejaVu.add(a);
    buf.append('[');
    for (int i = 0; ; i++) {

      Object element = a[i];
      if (element == null) {
        buf.append("null");
      } else {
        Class eClass = element.getClass();

        if (eClass.isArray()) {
          if (eClass == byte[].class)
            buf.append(toString((byte[]) element));
          else if (eClass == short[].class)
            buf.append(toString((short[]) element));
          else if (eClass == int[].class)
            buf.append(toString((int[]) element));
          else if (eClass == long[].class)
            buf.append(toString((long[]) element));
          else if (eClass == char[].class)
            buf.append(toString((char[]) element));
          else if (eClass == float[].class)
            buf.append(toString((float[]) element));
          else if (eClass == double[].class)
            buf.append(toString((double[]) element));
          else if (eClass == boolean[].class)
            buf.append(toString((boolean[]) element));
          else { // element is an array of object references
            if (dejaVu.contains(element))
              buf.append("[...]");
            else
              deepArrayToString((Object[])element, buf, dejaVu);
          }
        } else {  // element is non-null and not an array
          buf.append(toString(element));
        }
      }
      if (i == iMax)
        break;
      buf.append(", ");
    }
    buf.append(']');
    dejaVu.remove(a);
  }

  /**
   * Hidden default constructor, this is a
   * utility class and should not be instantiated.
   */
  private Strings() {
    // do nothing
  }
}
