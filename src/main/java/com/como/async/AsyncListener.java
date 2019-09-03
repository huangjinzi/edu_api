package com.como.async;

import cn.hutool.core.date.DateUtil;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.weixin.sdk.kit.IpKit;
import net.dreamlu.event.core.EventListener;

public class AsyncListener {

    /**
     * 处理访问日志
     */
    @EventListener(async = true)
    public void listenLogEvent(LogEvent event) {
        Invocation inv = (Invocation) event.getSource();
        if (!"/".equals(inv.getActionKey())) {
            Controller c = inv.getController();
            String ip = IpKit.getRealIp(c.getRequest());
            String userAgent = c.getHeader("User-Agent");
            String actionKey = inv.getActionKey();
            System.out.println(DateUtil.now() + "-----" + ip + "-----" + actionKey + "-----" + userAgent);
        }
    }

}
