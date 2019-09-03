package com.como.module.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.como.common.Const;
import com.como.common.Ret;
import com.como.model.EduUser;
import com.como.util.EncrypAESUtil;
import com.como.util.OSSUtil;
import com.jfinal.aop.Duang;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.ApiResult;
import com.jfinal.weixin.sdk.cache.IAccessTokenCache;
import com.jfinal.wxaapp.api.WxaUserApi;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by commando on 2019/1/10.
 */
public class CommonService {
    protected WxaUserApi wxaUserApi = Duang.duang(WxaUserApi.class);

    public Ret login(String code) {
        // 获取SessionKey
        ApiResult apiResult = wxaUserApi.getSessionKey(code);

        // 返回{"session_key":"nzoqhc3OnwHzeTxJs+inbQ==","expires_in":2592000,"openid":"oVBkZ0aYgDMDIywRdgPW8-joxXc4"},新版中不对外暴露过期时间
        if (!apiResult.isSucceed()) {
            LogKit.error("login error:" + apiResult.getJson());
            return Ret.error(-4);
        }

        String openId = apiResult.get("openid");
        EduUser user = EduUser.dao.findByOpenId(openId);
        if (user == null) {
            addUser(apiResult);
        }

        // 利用 appId 与 accessToken 建立关联，支持多账户
        IAccessTokenCache accessTokenCache = ApiConfigKit.getAccessTokenCache();
        //String sessionId = StrKit.getRandomUUID();
        user = EduUser.dao.findByOpenId(openId);
        String sessionId = EncrypAESUtil.encrypt(user.getId());
        accessTokenCache.set("wxa:session:" + sessionId, apiResult.getJson());
        return Ret.ok().set("sessionId", sessionId);
    }

    public Ret info(String encryptedData, String rawData, String signature, String iv, String sessionKey) {
        // 用户信息校验
        boolean check = wxaUserApi.checkUserInfo(sessionKey, rawData, signature);
        if (!check) {
            return Ret.error(1003);
        }
        // 服务端解密用户信息
        ApiResult apiResult = wxaUserApi.getUserInfo(sessionKey, encryptedData, iv);
        if (!apiResult.isSucceed()) {
            return Ret.error(1004);
        }
        // 如果开发者拥有多个移动应用、网站应用、和公众帐号（包括小程序），可通过unionid来区分用户的唯一性
        // 同一用户，对同一个微信开放平台下的不同应用，unionid是相同的。

        String openId = apiResult.get("openId");
        EduUser user = EduUser.dao.findByOpenId(openId);
        if (user != null) {
            LogKit.info("apiRsult:" + apiResult.toString());
            updateUser(apiResult);
        }

        return Ret.ok();
    }

    private void addUser(ApiResult apiResult) {
        String openId = apiResult.get("openid");

        EduUser user = new EduUser();
        user.setOpenId(openId)
                .setRegTime(new Date());
        user.save();
    }

    private void updateUser(ApiResult apiResult) {
        String unionId = apiResult.get("unionId");
        String openId = apiResult.get("openId"); //I大写
        int gender = apiResult.getInt("gender");
        String nickName = apiResult.get("nickName");
        String language = apiResult.get("language");
        String city = apiResult.get("city");
        String province = apiResult.get("province");
        String country = apiResult.get("country");
        String avatarUrl = apiResult.get("avatarUrl");

        EduUser user = EduUser.dao.findByOpenId(openId);
        LogKit.info("openId:" + openId);

        //第一次信息由微信写入，用户修改后，不再由微信更新性别和昵称
        if (user.getGender() == null) {
            user.setGender(gender);
        }
        if (user.getNickName() == null) {
            user.setNickName(nickName);
        }
        if (user.getRealName() == null) {
            user.setRealName(nickName);
        }

        user.setCity(city)
                .setLanguage(language)
                .setCity(city)
                .setProvince(province)
                .setCountry(country)
                .setAvatar(avatarUrl)
                .update();
    }

    public Ret bindMobile(long userId, String sessionKey, String iv, String encryptedData) {
        // 服务端解密用户信息
        ApiResult apiResult = wxaUserApi.getUserInfo(sessionKey, encryptedData, iv);

        if (!apiResult.isSucceed()) {
            return Ret.error(1004);
        }

        String phoneNumber = apiResult.get("phoneNumber");

        EduUser user = EduUser.dao.findById(userId);
        if (user != null) {
            LogKit.info("apiRsult:" + apiResult.toString());
            user.setMobile(phoneNumber)
                    .update();
        }

        return Ret.ok().set("phoneNumber", phoneNumber);
    }


    public Ret uploadSingle(UploadFile uploadFile, String sessionId, boolean saveSuffix) {
        Long userId = EncrypAESUtil.decryptToLong(sessionId);
        if (userId == null) {
            return Ret.error(-2);
        }
        return Ret.ok("url", uploadToOss(uploadFile.getFile(), saveSuffix));
    }

    public Ret uploadBatch(List<UploadFile> uploadFiles, String sessionId, boolean saveSuffix) {
        Long userId = EncrypAESUtil.decryptToLong(sessionId);
        if (userId == null) {
            return Ret.error(-2);
        }
        List<String> urls = new ArrayList<>();
        for (UploadFile uploadFile : uploadFiles) {
            urls.add(uploadToOss(uploadFile.getFile(), saveSuffix));
        }
        return Ret.ok("urls", urls);
    }

    /**
     * 生成oss保存地址
     */
    public static String generatorOssSaveKey() {
        return Const.PROJECT_EDU_API + DateUtil.format(DateUtil.date(), "/yyyy/MM/dd/") + StrKit.getRandomUUID();
    }

    /**
     * 上传文件到OSS
     */
    public static String uploadToOss(File file, boolean saveSuffix) {
        String saveKey = generatorOssSaveKey();
        return uploadToOss(file, saveSuffix, saveKey);
    }

    /**
     * 上传文件到OSS
     */
    public static String uploadToOss(File file, boolean saveSuffix, String saveKey) {
        String suffix = FileUtil.extName(file);
        if (saveSuffix && StrKit.notBlank(suffix)) {
            saveKey = saveKey + StrUtil.C_DOT + suffix;
        }
        OSSUtil.upload(saveKey, file);
        file.delete();
        return Const.OSS_PREFIX + saveKey;
    }

    public Ret getOSSPolicy(String suffix) {
        return Ret.ok().set(OSSUtil.getPolicy(suffix));
    }
}
