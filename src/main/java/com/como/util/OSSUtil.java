package com.como.util;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.como.module.common.CommonService;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class OSSUtil {
    private static final String ENDPOINT = PropKit.get("endpoint");
    private static final String ACCESS_KEY_ID = PropKit.get("accessKeyId");
    private static final String ACCESS_KEY_SECRET = PropKit.get("accessKeySecret");
    private static final String BUCKET_NAME = PropKit.get("bucketName");

    /**
     * 上传文件
     *
     * @param path 要保存的文件的路径名，格式为filename.mp4或folder/filename.mp4
     * @param file 要上传的文件
     */
    public static void upload(String path, File file) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        // 上传文件
        ossClient.putObject(BUCKET_NAME, path, file);
        // 关闭client
        ossClient.shutdown();
    }

    /**
     * 上传文件
     *
     * @param path 要保存的文件的路径名，格式为filename.mp4或folder/filename.mp4
     * @param is   要上传的文件流 InputStream
     */
    public static void upload(String path, InputStream is) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        // 上传文件
        ossClient.putObject(BUCKET_NAME, path, is);
        // 关闭client
        ossClient.shutdown();
    }

    /**
     * 删除文件
     *
     * @param path 要删除的文件的路径名，格式为filename.mp4或folder/filename.mp4
     */
    public static void delete(String path) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        // 上传文件
        ossClient.deleteObject(BUCKET_NAME, path);
        // 关闭client
        ossClient.shutdown();
    }

    /**
     * 获取授权的URL
     *
     * @param path 文件的路径名，格式为filename.mp4或folder/filename.mp4
     * @return 授权的URL，文件存在返回地址，不存在则返回null
     */
    public static String getAuthorizedUrl(String path) {
        String authorizedUrl = null;
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        //判断文件是否存在
        if (ossClient.doesObjectExist(BUCKET_NAME, path)) {
            // 设置URL过期时间为1小时
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            // 生成URL
            URL url = ossClient.generatePresignedUrl(BUCKET_NAME, path, expiration);
            authorizedUrl = url.toString();
        }
        // 关闭client
        ossClient.shutdown();
        return authorizedUrl;
    }

    public static Map<String, String> getPolicy(String suffix) {
        String savekey = CommonService.generatorOssSaveKey();
        if (StrKit.notBlank(suffix)) {
            savekey = savekey + StrUtil.C_DOT + suffix;
        }
        String host = "https://" + BUCKET_NAME + "." + ENDPOINT;
        OSSClient client = new OSSClient(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
        Map<String, String> respMap = new LinkedHashMap<>();
        try {
            long expireTime = 6000;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, savekey);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            client.shutdown();

            respMap.put("OSSAccessKeyId", ACCESS_KEY_ID);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            //respMap.put("expire", formatISO8601Date(expiration));
            respMap.put("key", savekey);
            respMap.put("host", host);
            respMap.put("success_action_status", "200");
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
        } catch (Exception e) {
            LogKit.error(e.getMessage(), e);
        }
        return respMap;
    }

}
