package com.como.module.common;

import com.como.common.Ret;
import com.como.interceptor.AuthInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.ExceededSizeException;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.cache.IAccessTokenCache;

import java.util.List;

public class CommonController extends BaseController {
    private com.como.module.common.CommonService service = new CommonService();

    /**
     * 登陆接口
     * 返回sessionId
     * 前端先调用wx.login()获取code
     */
    public void login() {
        String code = getPara("code");
        if (StrKit.isBlank(code)) {
            renderJson(Ret.error(-3));
            return;
        }
        renderJson(service.login(code));
    }

    /**
     * 服务端解密用户信息接口
     * 前端先通过wx.getUserInfo()获取用户信息
     */
    public void info() {
        String signature = getPara("signature");
        String rawData = getPara("rawData");
        String encryptedData = getPara("encryptedData");
        String iv = getPara("iv");
        String sessionId = getPara("sessionId");
        //String sessionId = getHeader("wxa-sessionid");

        // 参数空校验
        if (StrKit.isBlank(sessionId)) {
            renderJson(Ret.error(1000));
            return;
        }

        // 利用 appId 与 accessToken 建立关联，支持多账户
        IAccessTokenCache accessTokenCache = ApiConfigKit.getAccessTokenCache();

        String sessionJson = accessTokenCache.get("wxa:session:" + sessionId);
        if (StrKit.isBlank(sessionJson)) {
            renderJson(Ret.error(1001));
            return;
        }

        ApiResult sessionResult = ApiResult.create(sessionJson);
        // 获取sessionKey
        String sessionKey = sessionResult.get("session_key");
        if (StrKit.isBlank(sessionKey)) {
            renderJson(Ret.error(1002));
            return;
        }

        renderJson(service.info(encryptedData, rawData, signature, iv, sessionKey));
    }

    /**
     * 绑定微信手机号
     */
    @Before(AuthInterceptor.class)
    public void bindMobile() {
        Long userId = getParaToLong("userId");
        userId = userId == null ? getAttr("userId") : userId;
        String iv = getPara("iv");
        String encryptedData = getPara("encryptedData");
        String sessionId = getPara("sessionId");

        // 参数空校验
        if (StrKit.isBlank(sessionId)) {
            renderJson(Ret.error(1000));
            return;
        }

        // 利用 appId 与 accessToken 建立关联，支持多账户
        IAccessTokenCache accessTokenCache = ApiConfigKit.getAccessTokenCache();

        String sessionJson = accessTokenCache.get("wxa:session:" + sessionId);
        if (StrKit.isBlank(sessionJson)) {
            renderJson(Ret.error(1001));
            return;
        }

        ApiResult sessionResult = ApiResult.create(sessionJson);
        // 获取sessionKey
        String sessionKey = sessionResult.get("session_key");
        if (StrKit.isBlank(sessionKey)) {
            renderJson(Ret.error(1002));
            return;
        }

        renderJson(service.bindMobile(userId, sessionKey, iv, encryptedData));
    }

    /**
     * 上传单个文件
     */
    public void uploadSingle() {
        try {
            UploadFile uploadFile = getFile();
            String sessionId = getPara("sessionId");
            boolean saveSuffix = getParaToBoolean("saveSuffix", false);
            renderJson(service.uploadSingle(uploadFile, sessionId, saveSuffix));
        } catch (ExceededSizeException e) {
            renderJson(Ret.error(1005));
        }
    }

    /**
     * 上传多个文件
     */
    public void uploadBatch() {
        try {
            List<UploadFile> uploadFiles = getFiles();
            String sessionId = getPara("sessionId");
            boolean saveSuffix = getParaToBoolean("saveSuffix", false);
            renderJson(service.uploadBatch(uploadFiles, sessionId, saveSuffix));
        } catch (ExceededSizeException e) {
            renderJson(Ret.error(1005));
        }
    }

    /**
     * 获取OSS上传授权（直传）
     */
    @Before(AuthInterceptor.class)
    public void getOSSPolicy() {
        String suffix = getPara("suffix");
        renderJson(service.getOSSPolicy(suffix));
    }


}
