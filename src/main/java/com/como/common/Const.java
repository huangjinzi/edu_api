package com.como.common;

import com.jfinal.kit.PropKit;

public class Const {
    public static final boolean DEV_MODE = PropKit.getBoolean("devMode", false);
    public static final String DB_EDU_API = "eduApi";

    public static final String PROJECT_EDU_API = "eduApi";
    public static final String REDIS_SERVER_URL = PropKit.get("redisServerUrl");

    public static final String OSS_PREFIX = PropKit.get("ossPrefix");

}
