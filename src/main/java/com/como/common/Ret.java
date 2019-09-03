package com.como.common;

import com.jfinal.json.Json;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class Ret extends HashMap {
	private static final Prop p = PropKit.use("error_en.txt");
	
    private static final String STATUS_OK = "isOk";
    private static final String ERRNO = "errno";
    private static final String ERRMSG = "errmsg";

    public Ret() {
    }

    public static Ret ok() {
        return new Ret().setOk();
    }

    public static Ret ok(Object key, Object value) {
        return ok().set(key, value);
    }

    public static Ret fail() {
        return new Ret().setFail();
    }

    public static Ret fail(Object key, Object value) {
        return fail().set(key, value);
    }

    public static Ret error(int errno) {
        return fail().set(ERRNO, errno).set(ERRMSG,p.get(String.valueOf(errno)));
    }

    public static Ret create() {
        return new Ret();
    }

    public static Ret create(Object key, Object value) {
        return new Ret().set(key, value);
    }

    public Ret setOk() {
        super.put(STATUS_OK, Boolean.TRUE);
        return this;
    }

    public Ret setFail() {
        super.put(STATUS_OK, Boolean.FALSE);
        return this;
    }

    public boolean isOk() {
        Boolean isOk = (Boolean) get(STATUS_OK);
        return isOk != null && isOk;
    }

    public boolean isFail() {
        Boolean isOk = (Boolean) get(STATUS_OK);
        return isOk != null && !isOk;
    }

    public Ret set(Object key, Object value) {
        super.put(key, value);
        return this;
    }

    public Ret set(Map map) {
        super.putAll(map);
        return this;
    }

    public Ret set(Ret ret) {
        super.putAll(ret);
        return this;
    }

    public Ret delete(Object key) {
        super.remove(key);
        return this;
    }

    public <T> T getAs(Object key) {
        return (T) get(key);
    }

    public BigDecimal getBigDecimal(Object key) {
        if (get(key)==null)
            return null;
        return new BigDecimal(get(key).toString());
    }

    public String getStr(Object key) {
        Object s = get(key);
        return s != null ? s.toString() : null;
    }

    public Integer getInt(Object key) {
        Number n = (Number)get(key);
        return n != null ? n.intValue() : null;
    }

    public Long getLong(Object key) {
        Number n = (Number)get(key);
        return n != null ? n.longValue() : null;
    }

    public Number getNumber(Object key) {
        return (Number)get(key);
    }

    public Double getDouble(Object key) {
        Number n = (Number)get(key);
        return n != null ? n.doubleValue() : null;
    }

    public Boolean getBoolean(Object key) {
        return (Boolean) get(key);
    }

    /**
     * key 存在，并且 value 不为 null
     */
    public boolean notNull(Object key) {
        return get(key) != null;
    }

    /**
     * key 不存在，或者 key 存在但 value 为null
     */
    public boolean isNull(Object key) {
        return get(key) == null;
    }

    /**
     * key 存在，并且 value 为 true，则返回 true
     */
    public boolean isTrue(Object key) {
        Object value = get(key);
        return (value instanceof Boolean && ((Boolean) value == true));
    }

    /**
     * key 存在，并且 value 为 false，则返回 true
     */
    public boolean isFalse(Object key) {
        Object value = get(key);
        return (value instanceof Boolean && ((Boolean) value == false));
    }

    public String toJson() {
        return Json.getJson().toJson(this);
    }

    @Override
    public boolean equals(Object ret) {
        return ret instanceof Ret && super.equals(ret);
    }
}
