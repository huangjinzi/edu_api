package com.como.module.log;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.level.Level;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

import java.io.File;
import java.util.Date;

public class LogService {
    static final LogService service = new LogService();
    private static final Prop PROP = PropKit.use("log4j.properties");
    private static final String BASE_LOG_PATH = "log4j.appender.{}.File";
    private static final String EMPTY_LOG_DESC = "当天没有{}日志记录！";

    public String log(String dateStr, Level level) {
        String logFilePath = PROP.get(StrUtil.format(BASE_LOG_PATH, level));
        if (StrKit.notBlank(dateStr)) {
            String suffix;
            Date date = new Date();
            if (dateStr.length() == 2) {
                suffix = DateUtil.format(date, "yyyyMM") + dateStr;
            } else if (dateStr.length() == 4) {
                suffix = DateUtil.format(date, "yyyy") + dateStr;
            } else {
                suffix = dateStr;
            }
            logFilePath += "." + suffix;
        }
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            return StrUtil.format(EMPTY_LOG_DESC, level);
        }
        return FileUtil.readUtf8String(logFile);
    }
}
