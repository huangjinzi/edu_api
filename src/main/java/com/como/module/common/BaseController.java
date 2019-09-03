package com.como.module.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

public abstract class BaseController extends Controller {

    @Override
    public String getPara(String name) {
        return getRequstParameter(name);
    }

    @Override
    public Long getParaToLong(String name) {
        if (("application/json".equals(getRequest().getContentType()) || "text/json".equals(getRequest().getContentType())) && getRequest().getMethod().equalsIgnoreCase("post")) {
            String json = getAttr("rawData");
            JSONObject reqJson = JSON.parseObject(json);
            if (reqJson != null && reqJson.containsKey(name)) {
                return Long.parseLong(reqJson.getString(name));
            }
        }
        return super.getParaToLong(name);
    }

    private String getRequstParameter(String name) {
        String para = getRequest().getParameter(name);
        if (StrKit.isBlank(para)) {
            String json = getAttr("rawData");
            JSONObject reqJson = JSON.parseObject(json);
            if (reqJson != null && !reqJson.isEmpty()) {
                para = reqJson.getString(name);
            }
        }
        return para;
    }

}
