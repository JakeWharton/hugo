package hugo.weaving.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

final class Strings {
  static String toString(Object obj) {
    if (obj == null) {
      return "null";
    }
    if (obj instanceof CharSequence) {
      return '"' + obj.toString() + '"';
    }

    Class<?> cls = obj.getClass();
    if (Byte.class == cls) {
      return byteToString((Byte) obj);
    }

    if (cls.isArray()) {
      return arrayToString(cls.getComponentType(), obj);
    }
    return obj.toString();
  }

  private static String arrayToString(Class<?> cls, Object obj) {
    if (byte.class == cls) {
      return byteArrayToString((byte[]) obj);
    }
    if (short.class == cls) {
      return Arrays.toString((short[]) obj);
    }
    if (char.class == cls) {
      return Arrays.toString((char[]) obj);
    }
    if (int.class == cls) {
      return Arrays.toString((int[]) obj);
    }
    if (long.class == cls) {
      return Arrays.toString((long[]) obj);
    }
    if (float.class == cls) {
      return Arrays.toString((float[]) obj);
    }
    if (double.class == cls) {
      return Arrays.toString((double[]) obj);
    }
    if (boolean.class == cls) {
      return Arrays.toString((boolean[]) obj);
    }
    return arrayToString((Object[]) obj);
  }

  /** A more human-friendly version of Arrays#toString(byte[]) that uses hex representation. */
  private static String byteArrayToString(byte[] bytes) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(byteToString(bytes[i]));
    }
    return builder.append(']').toString();
  }

  private static String byteToString(Byte b) {
    if (b == null) {
      return "null";
    }
    return "0x" + String.format("%02x", b).toUpperCase(Locale.US);
  }

  private static String arrayToString(Object[] array) {
    StringBuilder buf = new StringBuilder();
    arrayToString(array, buf, new HashSet<Object[]>());
    return buf.toString();
  }

  private static void arrayToString(Object[] array, StringBuilder builder, Set<Object[]> seen) {
    if (array == null) {
      builder.append("null");
      return;
    }

    seen.add(array);
    builder.append('[');
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }

      Object element = array[i];
      if (element == null) {
        builder.append("null");
      } else {
        Class elementClass = element.getClass();
        if (elementClass.isArray() && elementClass.getComponentType() == Object.class) {
            Object[] arrayElement = (Object[]) element;
            if (seen.contains(arrayElement)) {
              builder.append("[...]");
            } else {
              arrayToString(arrayElement, builder, seen);
            }
        } else {
          builder.append(toString(element));
        }
      }
    }
    builder.append(']');
    seen.remove(array);
  }

  private Strings() {
    throw new AssertionError("No instances.");
  }
}
