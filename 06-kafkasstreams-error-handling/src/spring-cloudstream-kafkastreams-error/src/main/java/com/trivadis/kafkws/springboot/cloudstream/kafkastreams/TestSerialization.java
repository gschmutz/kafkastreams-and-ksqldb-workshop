package com.trivadis.kafkws.springboot.cloudstream.kafkastreams;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

public class TestSerialization {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String KEY_DELIMITER = "-";
    private static final String HEADERS_NULL_MESSAGE = "Headers may not be null";

    private TestSerialization() {
        // Utility class
    }

    /**
     * Converts bytes to long.
     *
     * @param value the bytes to convert in to a long
     * @return the long build from the given bytes
     */
    public static Long asLong(byte[] value) {
        return value != null ? ByteBuffer.wrap(value).getLong() : null;
    }

    /**
     * Return a {@link Long} representation of the {@code value} stored under a given {@code key} inside the {@link
     * Headers}. In case of a missing entry {@code null} is returned.
     *
     * @param headers the Kafka {@code headers} to pull the {@link Long} value from
     * @param key     the key corresponding to the expected {@link Long} value
     * @return the value as a {@link Long} corresponding to the given {@code key} in the {@code headers}
     */
    public static Long valueAsLong(Headers headers, String key) {
        return asLong(value(headers, key));
    }

    /**
     * Return a {@link Long} representation of the {@code value} stored under a given {@code key} inside the {@link
     * Headers}. In case of a missing entry {@code defaultValue} is returned.
     *
     * @param headers      the Kafka {@code headers} to pull the {@link Long} value from
     * @param key          the key corresponding to the expected {@link Long} value
     * @param defaultValue the default value to return when {@code key} does not exist in the given {@code headers}
     * @return the value as a {@link Long} corresponding to the given {@code key} in the {@code headers}
     */
    public static Long valueAsLong(Headers headers, String key, Long defaultValue) {
        Long value = asLong(value(headers, key));
        return value != null ? value : defaultValue;
    }

    /**
     * Converts the given bytes to {@code int}.
     *
     * @param value the bytes to convert into a {@code int}
     * @return the {@code int} build from the given bytes
     */
    public static Integer asInt(byte[] value) {
        return value != null ? ByteBuffer.wrap(value).getInt() : null;
    }

    /**
     * Return a {@link Integer} representation of the {@code value} stored under a given {@code key} inside the {@link
     * Headers}. In case of a missing entry {@code null} is returned.
     *
     * @param headers the Kafka {@code headers} to pull the {@link Integer} value from
     * @param key     the key corresponding to the expected {@link Integer} value
     * @return the value as a {@link Integer} corresponding to the given {@code key} in the {@code headers}
     */
    public static Integer valueAsInt(Headers headers, String key) {
        return asInt(value(headers, key));
    }

    /**
     * Return a {@link Integer} representation of the {@code value} stored under a given {@code key} inside the {@link
     * Headers}. In case of a missing entry {@code defaultValue} is returned.
     *
     * @param headers      the Kafka {@code headers} to pull the {@link Integer} value from
     * @param key          the key corresponding to the expected {@link Integer} value
     * @param defaultValue the default value to return when {@code key} does not exist in the given {@code headers}
     * @return the value as a {@link Integer} corresponding to the given {@code key} in the {@code headers}
     */
    public static Integer valueAsInt(Headers headers, String key, Integer defaultValue) {
        Integer value = asInt(value(headers, key));
        return value != null ? value : defaultValue;
    }

    public static Short asShort(byte[] value) {
        return value != null ? ByteBuffer.wrap(value).getShort() : null;
    }

    public static Float asFloat(byte[] value) {
        return value != null ? ByteBuffer.wrap(value).getFloat() : null;
    }


    /**
     * Converts bytes to {@link String}.
     *
     * @param value the bytes to convert in to a {@link String}
     * @return the {@link String} build from the given bytes
     */
    public static String asString(byte[] value) {
        return value != null ? new String(value, UTF_8) : null;
    }

    /**
     * Return a {@link String} representation of the {@code value} stored under a given {@code key} inside the {@link
     * Headers}. In case of a missing entry {@code null} is returned.
     *
     * @param headers the Kafka {@code headers} to pull the {@link String} value from
     * @param key     the key corresponding to the expected {@link String} value
     * @return the value as a {@link String} corresponding to the given {@code key} in the {@code headers}
     */
    public static String valueAsString(Headers headers, String key) {
        return asString(value(headers, key));
    }

    /**
     * Return a {@link String} representation of the {@code value} stored under a given {@code key} inside the {@link
     * Headers}. In case of a missing entry the {@code defaultValue} is returned.
     *
     * @param headers      the Kafka {@code headers} to pull the {@link String} value from
     * @param key          the key corresponding to the expected {@link String} value
     * @param defaultValue the default value to return when {@code key} does not exist in the given {@code headers}
     * @return the value as a {@link String} corresponding to the given {@code key} in the {@code headers}
     */
    public static String valueAsString(Headers headers, String key, String defaultValue) {
        return Objects.toString(asString(value(headers, key)), defaultValue);
    }

    /**
     * Return the {@code value} stored under a given {@code key} inside the {@link Headers}. In case of missing entry
     * {@code null} is returned.
     *
     * @param headers the Kafka {@code headers} to pull the value from
     * @param key     the key corresponding to the expected value
     * @return the value corresponding to the given {@code key} in the {@code headers}
     */
    @SuppressWarnings("squid:S2259") // Null check performed by `Assert.isTrue`
    public static byte[] value(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        return header != null ? header.value() : null;
    }

    /**
     * Converts primitive arithmetic types to byte array.
     *
     * @param value the {@link Number} to convert into a byte array
     * @return the byte array converted from the given {@code value}
     */
    public static byte[] toBytes(Number value) {
        if (value instanceof Short) {
            return toBytes((Short) value);
        } else if (value instanceof Integer) {
            return toBytes((Integer) value);
        } else if (value instanceof Long) {
            return toBytes((Long) value);
        } else if (value instanceof Float) {
            return toBytes((Float) value);
        } else if (value instanceof Double) {
            return toBytes((Double) value);
        }
        throw new IllegalArgumentException("Cannot convert [" + value + "] to bytes");
    }

    private static byte[] toBytes(Short value) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(value);
        return buffer.array();
    }

    private static byte[] toBytes(Integer value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }

    private static byte[] toBytes(Long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }

    private static byte[] toBytes(Float value) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(value);
        return buffer.array();
    }

    private static byte[] toBytes(Double value) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(value);
        return buffer.array();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        //Long l = Instant.now().toEpochMilli();
        Long l = 10L;
        Short shortValue = 1;
        byte[] blong = toBytes(l);
        byte[] binteger = toBytes(11);
        byte[] bshort = toBytes(shortValue);
        byte[] bfloat = toBytes(11.1f);
        //byte[] bbool = toBytes(true);
        byte[] bstring = new String("Hello World").getBytes(StandardCharsets.UTF_8);
        byte[] bstring16 = new String("Hello World from Worblaufen").getBytes(StandardCharsets.UTF_16);
        int ir = asInt(binteger);
        long lr = asLong(blong);
        float fr = asFloat(bfloat);

        String sr = asString(bstring);



        byte[] vft = bstring16;

        System.out.println(isValidUTF8(vft));
        System.out.println(isValidUTF16(vft));

        if (isValidUTF8(vft)) {
            System.out.println("String: " + new String(vft));
        } else if (isValidUTF16(vft)) {
            System.out.println("String: " + new String(vft, StandardCharsets.UTF_16));
        } else {

                try {
                    System.out.println("Long: " + asLong(vft));
                } catch (Exception el) {
                    try {
                        System.out.println("Integer: " + asInt(vft));
                    } catch (Exception ei) {
                        System.out.println("Short: " + asShort(vft));
                    }
                }

        }

        //System.out.println("StringUtils:" + StringUtils.newStringUtf8(bs));
    }


    static boolean isValidUTF8(byte[] utf8) throws UnsupportedEncodingException
    {
        Pattern p = Pattern.compile("\\A(\n" +
                "  [\\x09\\x0A\\x0D\\x20-\\x7E]             # ASCII\\n" +
                "| [\\xC2-\\xDF][\\x80-\\xBF]               # non-overlong 2-byte\n" +
                "|  \\xE0[\\xA0-\\xBF][\\x80-\\xBF]         # excluding overlongs\n" +
                "| [\\xE1-\\xEC\\xEE\\xEF][\\x80-\\xBF]{2}  # straight 3-byte\n" +
                "|  \\xED[\\x80-\\x9F][\\x80-\\xBF]         # excluding surrogates\n" +
                "|  \\xF0[\\x90-\\xBF][\\x80-\\xBF]{2}      # planes 1-3\n" +
                "| [\\xF1-\\xF3][\\x80-\\xBF]{3}            # planes 4-15\n" +
                "|  \\xF4[\\x80-\\x8F][\\x80-\\xBF]{2}      # plane 16\n" +
                ")*\\z", Pattern.COMMENTS);

        String phonyString = new String(utf8, "ISO-8859-1");
        return p.matcher(phonyString).matches();
    }

    public static boolean isValidUTF16(byte[] buffer) {
        return isValidUTF16(buffer, false);
    }

    public static boolean isValidUTF16(byte[] buffer, boolean le) {
        if (buffer.length < 2) {
            return false;
        }
        for (int i = 0; i < buffer.length / 2; i++) {
            boolean extraByte = false;
            int c = read16bit(buffer, i, le);

            if (c >= 0xD800 && c < 0xDC00) {
                // it's a higher surrogate (10 bits)
                extraByte = true;
                i++;
            } else if ((c >= 0xDC00 && c < 0xE000) || c == 0) {
                return false;
            }
            // else it is a simple 2 byte encoding (code points in BMP), and it's valid

            if (extraByte && i < buffer.length / 2) {
                c = read16bit(buffer, i, le);
                if (c < 0xDC00 || c >= 0xE000) {
                    // invalid lower surrogate (10 bits)
                    return false;
                }
            }
        }
        return true;
    }

    private static int read16bit(byte[] buffer, int i, boolean le) {
        return le ? (buffer[i / 2] & 0xff) | ((buffer[i / 2 + 1] & 0xff) << 8)
                : ((buffer[i / 2] & 0xff) << 8) | (buffer[i / 2 + 1] & 0xff);
    }
}
