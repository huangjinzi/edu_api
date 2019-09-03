package com.como.module.hm;

import com.como.interceptor.AuthInterceptor;
import com.como.module.common.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;

@Before(AuthInterceptor.class)
public class HmController extends BaseController {
    private static final HmService service = HmService.service;

    /**
     * 是否需要重新login
     */
    @Clear(AuthInterceptor.class)
    public void needLogin() {
        String sessionId = getPara("sessionId");
        renderJson(service.needLogin(sessionId));
    }

}
