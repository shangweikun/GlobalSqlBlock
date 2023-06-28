package com.swk.mybatis.driver.augmented.param;

public abstract class SQLParam {

    public final String paramName;

    protected SQLParam(String paramName) {
        this.paramName = paramName;
    }

    protected abstract String doHandle(String sql, Integer left, Integer right);
}
