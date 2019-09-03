package com.como.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.como.common.Ret;
import com.como.model.EduUser;
import com.como.util.EncrypAESUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.cache.IAccessTokenCache;

import javax.servlet.http.HttpServletRequest;

public class AuthInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller c = inv.getController();
        String sessionId = "";

        HttpServletRequest request = c.getRequest();
        if (("application/json".equals(request.getContentType()) || "text/json".equals(request.getContentType())) && request.getMethod().equalsIgnoreCase("post")) {
            String json = HttpKit.readData(request);
            request.setAttribute("rawData", json); //attr ,controller use
            JSONObject reqJson = JSON.parseObject(json);
            if (reqJson != null && reqJson.containsKey("sessionId")) {
                sessionId = reqJson.getString("sessionId");
            }
        } else {
            sessionId = c.getPara("sessionId");
        }

        IAccessTokenCache accessTokenCache = ApiConfigKit.getAccessTokenCache();
        String sessionJson = accessTokenCache.get("wxa:session:" + sessionId);
        if (StrKit.isBlank(sessionJson)) {
            c.renderJson(Ret.error(-2));
            LogKit.error("sessionId cache expired，sessionId:" + sessionId);
            return;
        }

        Long userId = EncrypAESUtil.decryptToLong(sessionId);

        if (userId == null || EduUser.dao.findById(userId) == null) {
            c.renderJson(Ret.error(-2));
            LogKit.error("user not find，sessionId：" + sessionId);
            return;
        } else {
            c.setAttr("userId", userId);
            inv.invoke();
        }
    }
}
