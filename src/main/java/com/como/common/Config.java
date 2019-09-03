package com.como.common;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.como.interceptor.GlobalInterceptor;
import com.como.model._MappingKitForEduApi;
import com.como.module.common.CommonController;
import com.como.module.hm.HmController;
import com.como.module.index.IndexController;
import com.como.module.log.LogController;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.json.MixedJsonFactory;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.template.Engine;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.cache.RedisAccessTokenCache;
import com.jfinal.wxaapp.WxaConfig;
import com.jfinal.wxaapp.WxaConfigKit;
import net.dreamlu.event.EventPlugin;

import java.io.File;
import java.sql.Connection;

public class Config extends JFinalConfig {
    public static final Prop p = PropKit.use("config.properties")
            .appendIfExists(new File("/mnt/projectdata/eduApi/config/config.properties"));

    /**
     * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
     */
    public static void main(String[] args) {
        JFinal.start("src/main/webapp", 8080, "/", 5);
    }

    /**
     * 配置常量
     */
    @Override
    public void configConstant(Constants me) {
        me.setDevMode(Const.DEV_MODE);
        me.setJsonFactory(MixedJsonFactory.me());
        // 限制上传文件大小为100M
        me.setMaxPostSize(1024 * 1024 * 100);
        me.setBaseUploadPath("/mnt/projectdata/eduApi/upload/");
        me.setInjectDependency(true);
    }

    /**
     * 配置路由
     */
    @Override
    public void configRoute(Routes me) {
        me.setBaseViewPath("_view");
        me.add("/", IndexController.class);
        me.add("/common", CommonController.class);
        me.add("/log", LogController.class);
        me.add("/hm", HmController.class);
    }

    @Override
    public void configEngine(Engine me) {
    }

    /**
     * 配置Druid连接池
     */
    public static DruidPlugin createDruidPluginForEduApi() {
        return new DruidPlugin(p.get("eduApiJdbcUrl"), p.get("eduApiJdbcUsername"), p.get("eduApiJdbcPassword").trim());
    }

    /**
     * 配置插件
     */
    @Override
    public void configPlugin(Plugins me) {
        // mysql防火墙
        WallFilter wallFilter = new WallFilter();
        wallFilter.setDbType("mysql");
        // WallConfig详细说明 https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE-wallfilter
        WallConfig wallConfig = new WallConfig();
        // 不适用严格的语法检测
        wallConfig.setStrictSyntaxCheck(false);
        // 让druid允许在sql中使用union
        wallConfig.setSelectUnionCheck(false);
        wallFilter.setConfig(wallConfig);

        /**
         * 数据库
         */
        // 配置数据库连接池
        DruidPlugin druidPluginForEduApi = createDruidPluginForEduApi();
        // 配置druid监控
        druidPluginForEduApi.addFilter(new StatFilter());
        // 配置防火墙
        druidPluginForEduApi.addFilter(wallFilter);
        me.add(druidPluginForEduApi);

        // 配置ActiveRecord
        ActiveRecordPlugin arpForEduApi = new ActiveRecordPlugin(Const.DB_EDU_API, druidPluginForEduApi);
        // 防止脏读
        arpForEduApi.setTransactionLevel(Connection.TRANSACTION_READ_COMMITTED);

        // 所有配置在 MappingKit 中搞定
        _MappingKitForEduApi.mapping(arpForEduApi);
        me.add(arpForEduApi);

        //redis插件，配合Cache使用
        me.add(new RedisPlugin(Const.PROJECT_EDU_API, Const.REDIS_SERVER_URL));

        // 初始化事件插件、 开启全局异步
        EventPlugin eventPlugin = new EventPlugin();
        eventPlugin.async();
        eventPlugin.scanPackage("com.como.async");
        me.add(eventPlugin);

    }

    /**
     * 配置全局拦截器
     */
    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new GlobalInterceptor());
    }

    /**
     * 配置处理器
     */
    @Override
    public void configHandler(Handlers me) {
//        me.add(new DruidStatViewHandler("/druid"));
    }

    @Override
    public void afterJFinalStart() {
        WxaConfig wxaConfig = new WxaConfig();
        wxaConfig.setAppId(PropKit.get("appId"));
        wxaConfig.setAppSecret(PropKit.get("appSecret"));
        WxaConfigKit.setWxaConfig(wxaConfig);
        ApiConfigKit.setAccessTokenCache(new RedisAccessTokenCache());
    }



}
