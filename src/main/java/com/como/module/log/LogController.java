package com.como.module.log;

import cn.hutool.log.level.Level;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;

@Clear
public class LogController extends Controller {
    private static final LogService service = LogService.service;

    public void index() {
        redirect("/log/error");
    }

    public void info() {
        String dateStr = getPara();
        String log = service.log(dateStr, Level.INFO);
        setAttr("log", log);
        render("log.html");
    }

    public void error() {
        String dateStr = getPara();
        String log = service.log(dateStr, Level.ERROR);
        setAttr("log", log);
        render("log.html");
    }

}
