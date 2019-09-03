package com.como.interceptor;

import com.como.util.EncrypAESUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

public class AuthCanNullInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation inv) {
        Controller c = inv.getController();
        String sessionId = c.getPara("sessionId");

        Long userId = null;
        if (StrKit.notBlank(sessionId)) {
            userId = EncrypAESUtil.decryptToLong(sessionId);
        }
        c.setAttr("userId", userId);
        inv.invoke();
    }
}
