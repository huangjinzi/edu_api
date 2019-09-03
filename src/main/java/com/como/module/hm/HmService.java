package com.como.module.hm;

import com.como.common.Ret;
import com.como.model.EduUser;
import com.como.util.EncrypAESUtil;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.cache.IAccessTokenCache;

/**
 * Created by commando on 2019/1/10.
 */
public class HmService {
    public static final com.como.module.hm.HmService service = new com.como.module.hm.HmService();
    public Ret needLogin(String sessionId) {
        if (StrKit.notBlank(sessionId)) {
            IAccessTokenCache accessTokenCache = ApiConfigKit.getAccessTokenCache();
            String sessionJson = accessTokenCache.get("wxa:session:" + sessionId);
            if (StrKit.isBlank(sessionJson)) {
                return Ret.ok().set("needLogin", true);
            } else {
                Long userId = EncrypAESUtil.decryptToLong(sessionId);
                EduUser user = EduUser.dao.findById(userId);
                if (user == null) {
                    return Ret.ok().set("needLogin", true);
                } else {
                    return Ret.ok().set("needLogin", false);
                }
            }
        } else {
            return Ret.ok().set("needLogin", true);
        }
    }


}
