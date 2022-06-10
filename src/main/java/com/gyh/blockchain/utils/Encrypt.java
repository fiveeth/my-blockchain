package com.gyh.blockchain.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @description: hash工具类
 * @author: gyh
 * @date: 2022/06/10
 */
public class Encrypt {
    /**
     * 传入字符串，返回SHA-256加密字符串
     *
     * @param strText
     * @return
     */
    public String getSHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入字符串，返回SHA-512加密字符串
     *
     * @param strText
     * @return
     */
    public String getSHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * 传入字符串，返回MD5加密字符串
     *
     * @param strText
     * @return
     */
    public String getMD5(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * 字符串SHA加密
     *
     * @param strText
     * @param strType
     * @return
     */
    private String SHA(final String strText, final String strType) {
        //返回值
        String strResult = null;
        //是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                //SHA加密开始
                //创建加密对象，传入加密类型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                //传入要加密的字符串
                messageDigest.update(strText.getBytes());
                //得到byte数组
                byte[] byteBuffer = messageDigest.digest();
                //將byte数组转换string类型
                StringBuffer strHexString = new StringBuffer();
                //遍历byte数组
                for (int i = 0; i < byteBuffer.length; i++) {
                    //转换成16进制并存储在字符串中
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                //得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }
}
