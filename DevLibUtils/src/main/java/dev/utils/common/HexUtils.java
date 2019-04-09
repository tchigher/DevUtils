package dev.utils.common;

import dev.utils.JCLogUtils;

/**
 * detail: 十六进制处理
 * Created by Ttt
 */
public final class HexUtils {

    private HexUtils() {
    }

    // 日志 TAG
    private static final String TAG = HexUtils.class.getSimpleName();
    // 用于建立十六进制字符的输出的小写字符数组
    public static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    // 用于建立十六进制字符的输出的大写字符数组
    public static final char HEX_DIGITS_UPPER[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 将字节数组转换为十六进制字符数组
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] encodeHex(final byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     * @param data        byte[]
     * @param toLowerCase true : 传换成小写格式 ， false : 传换成大写格式
     * @return 十六进制char[]
     */
    public static char[] encodeHex(final byte[] data, final boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? HEX_DIGITS : HEX_DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     * @param data     byte[]
     * @param hexDigits 用于控制输出的char[]
     * @return 十六进制char[]
     */
    private static char[] encodeHex(final byte[] data, final char[] hexDigits) {
        if (data == null || hexDigits == null) return null;
        try {
            return toHexString(data, hexDigits).toCharArray();
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "encodeHex");
        }
        return null;
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(final byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param data        byte[]
     * @param toLowerCase true : 传换成小写格式 ， false : 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(final byte[] data, final boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? HEX_DIGITS : HEX_DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制String
     */
    private static String encodeHexStr(final byte[] data, final char[] toDigits) {
        if (data == null || toDigits == null) return null;
        try {
            return new String(encodeHex(data, toDigits));
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "encodeHexStr");
        }
        return null;
    }

    /**
     * 将十六进制字符数组转换为字节数组
     * @param data 十六进制char[]
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decodeHex(final char[] data) {
        if (data == null) return null;
        try {
            int len = data.length;
            byte[] out = new byte[len >> 1];
            // two characters form the hex value.
            for (int i = 0, j = 0; j < len; i++) {
                int f = toDigit(data[j], j) << 4;
                j++;
                f = f | toDigit(data[j], j);
                j++;
                out[i] = (byte) (f & 0xFF);
            }
            return out;
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "decodeHex");
        }
        return null;
    }

    /**
     * 将十六进制字符转换成一个整数
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws RuntimeException 当ch不是一个合法的十六进制字符时，抛出运行时异常
     */
    private static int toDigit(final char ch, final int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * 进行十六进制转换
     * @param data
     * @param hexDigits
     * @return
     */
    private static String toHexString(final byte[] data, final char[] hexDigits) {
        if (data == null || hexDigits == null) return null;
        try {
            int len = data.length;
            StringBuilder builder = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                builder.append(hexDigits[(data[i] & 0xf0) >>> 4]);
                builder.append(hexDigits[data[i] & 0x0f]);
            }
            return builder.toString();
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "toHexString");
        }
        return null;
    }

//    public static void main(String[] args) {
//        String srcStr = "待转换字符串";
//        String encodeStr = encodeHexStr(srcStr.getBytes());
//        String decodeStr = new String(decodeHex(encodeStr.toCharArray()));
//        System.out.println("转换前：" + srcStr);
//        System.out.println("转换后：" + encodeStr);
//        System.out.println("还原后：" + decodeStr);
//    }
}