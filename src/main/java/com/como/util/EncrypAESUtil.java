package com.como.util;

import com.como.common.Config;
import com.jfinal.kit.StrKit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class EncrypAESUtil {
    public static final String SERECTKEY = Config.p.get("serectKey");

    public static void main(String[] args) {
        String de = EncrypAESUtil.decrypt("2753f42a6448fa9c1786f1bff3263877");
        System.out.println(de);
        String en = EncrypAESUtil.encrypt(100059L);
        System.out.println(en);
    }

    /**
     * 加密
     *
     * @param num 方便加密id
     */
    public static String encrypt(long num) {
        return encrypt("" + num);
    }

    /**
     * 加密
     *
     * @param content 需要加密的内容
     */
    public static String encrypt(String content) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(SERECTKEY.getBytes());
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes("utf-8");
            // 初始化
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(byteContent);
            return parseByte2HexStr(result);
        } catch (Exception e) {
            System.out.println("加密失败！");
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @return Long
     */
    public static Long decryptToLong(String content) {
        String decryptStr = decrypt(content);
        if (StrKit.isBlank(decryptStr))
            return null;
        return Long.parseLong(decryptStr);
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     */
    public static String decrypt(String content) {
        try {
            byte[] bytes = parseHexStr2Byte(content);
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(SERECTKEY.getBytes());
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] resultBytes = cipher.doFinal(bytes);
            return new String(resultBytes);
        } catch (Exception e) {
            System.out.println("解密失败！");
        }
        return null;
    }

    /**
     * 将二进制转换成十六进制
     *
     * @param buf
     * @return
     */
    private static String parseByte2HexStr(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 将十六进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}
