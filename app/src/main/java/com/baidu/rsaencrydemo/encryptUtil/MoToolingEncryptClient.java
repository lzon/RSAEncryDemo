package com.baidu.rsaencrydemo.encryptUtil;

import android.os.Build;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baidu.rsaencrydemo.constant.NetConstant;
import com.baidu.rsaencrydemo.util.XPrintLogUtils;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class MoToolingEncryptClient {
    private static String TAG = MoToolingEncryptClient.class.getName();

    private static boolean debug = true;

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;
    private static final String CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String ALGORITHM = "RSA";
    private static final String PROVIDER = "BC";

    /**
     Post 请求数据加密
     1. 先将token转成data
     2. 将时间戳转成data
     3. 加盐 MoTooling转成data
     4. 按顺序将上述3个data 转成一个md5字符串 以备后台验证
     5. 构建dict : @"md5":md5字符串 ,@"token":用户token，@“timestamp”：时间戳字符串
     6. 将dict 结合后台公钥转成base64字符串--》base64Str
     7. 构建Prama @{@"paramsData":base64Str},作为参数体递交后台
     */
    /**
     * 将请求参数数据包加密成字符串
     *
     * @param data
     * @param token
     * @param timestamp
     * @return
     */
    public static String encryptionData(Map<String, Object> data, String token, Long timestamp) {
        //请求MoTooling企业站点-->参数
        Map<String, Object> objMap = new HashMap<>();
        //参数
        objMap.put("data", data);
        //时间戳
        objMap.put("timestamp", timestamp);
        //token
        objMap.put("token", token);
        StringBuffer buffer = new StringBuffer(3);
        //将 token ,timsetamp,"Motooling"
        buffer.append(token).append(timestamp).append(NetConstant.MoToolingSalt);
        //MD5加密
        objMap.put("md5", MD5.getMd5Str(buffer.toString()));
        //公钥加密  //  objMpa:(md5 ,timestamp ,token ,data)
        return encryptionResult(objMap, NetConstant.REQ_PUBLIC_KEY);
    }

    /**
     * 将服务端返回params进行Base64解密一下
     * @param resualtPar
     * @return
     */
    public static String decryptionData(String resualtPar) {
        try {
            return decryptAndroid(resualtPar, NetConstant.RES_PRIVATE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            XPrintLogUtils.e("decryptionData:" + e.getMessage());
        }
        return null;
    }

    /**
     * 响应对象加密----公钥加密
     * @param obj 需要加密对象
     * @return
     */
    public static String encryptionResult(Object obj, String publicKey) {
        try {
//            Map<String, Object> map = new HashMap<>();
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("uid","1");
//            map.put("data", map1);
//            map.put("md5","1b7f37a39d5cb4d350dbea25be0bba72");
//            map.put("timestamp","1552455938");
//            map.put("token","d28c7064-6f1d-4dc9-8bac-a4a18942d2c8"); 23dbb183-e69a-48ad-9592-9e9ddb4ddae7
//            String data = JSON.toJSONString(map, SerializerFeature.DisableCircularReferenceDetect);
            String data = JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
            if (debug)
                XPrintLogUtils.e("encryp: 请求数据---->:" + data);
            return encryptAndroid(data, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
            XPrintLogUtils.e("----> " + e.getMessage());
            return null;
        }

    }
    /**
     * 响应对象加密----公钥加密
     * @param obj 需要加密对象
     * @return
     */
    public static String encryptionResult(String obj, String publicKey) {
        try {
            String data = obj;
            if (debug)
                XPrintLogUtils.e("encryp: 请求数据---->:" + data);
            return encryptAndroid(data, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
            XPrintLogUtils.e("----> " + e.getMessage());
            return null;
        }

    }


    /**
     * Android --->    RSA加密
     * @param content   待加密文本
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception
     */
    public static String encryptAndroid(String content, String publicKey) throws Exception {
        byte[] keyBytes = Base64Util.decodeString(publicKey);
        X509EncodedKeySpec pkcs8KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            keyFactory = KeyFactory.getInstance(ALGORITHM);
        } else {
            keyFactory = KeyFactory.getInstance("RSA", "BC");
        }
        Key privateK = keyFactory.generatePublic(pkcs8KeySpec);
        //java默认"RSA"="RSA/ECB/PKCS1Padding"
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        byte[] data = content.getBytes();
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return new String(Base64.encode(encryptedData, Base64.NO_WRAP));
    }


    /**
     * Android----> RSA解密
     *
     * @param content    密文
     * @param privateKey 私钥
     * @return 明文
     * @throws Exception
     */
    public static String decryptAndroid(String content, String privateKey) throws Exception {
        XPrintLogUtils.e("解密原文："+content);
        byte[] keyBytes = Base64Util.decodeString(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        byte[] encryptedData = Base64.decode(content, Base64.NO_WRAP);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }

}
