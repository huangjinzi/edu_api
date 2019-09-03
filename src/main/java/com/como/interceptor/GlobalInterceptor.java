package com.como.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.como.async.LogEvent;
import com.como.common.Ret;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import net.dreamlu.event.EventKit;

import javax.servlet.http.HttpServletResponse;

public class GlobalInterceptor implements Interceptor {

    @Override
    final public void intercept(Invocation inv) {
        EventKit.post(new LogEvent(inv));
        Controller c = inv.getController();
        HttpServletResponse response = c.getResponse();
        String origin = c.getHeader("Origin");
        if (origin == null) {
            response.addHeader("Access-Control-Allow-Origin", "*");
        } else if (origin.startsWith("http://192.168.") || origin.endsWith("goodshenti.com") || origin.startsWith("http://localhost")) {
            response.addHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Access-Control-Allow-Headers", "*,x-requested-with,content-type");
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
        if ("OPTIONS".equals(c.getRequest().getMethod())) {
            c.renderNull();
        } else {
            try {
                inv.invoke();
            } catch (Exception e) {
                String userAgent = c.getHeader("User-Agent");
                LogKit.error("UA:" + userAgent
                        + "\nParams:" + JSONObject.toJSONString(c.getParaMap())
                        + "\nCookies:" + JSONObject.toJSONString(c.getCookieObjects())
                        + "\nErrorMessage:" + e.getMessage(), e);
                c.renderJson(Ret.error(-1));
            }
        }
    }
}