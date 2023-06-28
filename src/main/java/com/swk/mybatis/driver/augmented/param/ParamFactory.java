package com.swk.mybatis.driver.augmented.param;

public class ParamFactory {

    private ParamFactory() {

    }

    public static DefaultSQLParam newDefaultSQLParam(String paramName) {
        return new DefaultSQLParam(paramName);
    }

    public static ListSQLParam newListSQLParam(String paramName) {
        return new ListSQLParam(paramName);
    }

    public static String handle(SQLParam sqlParam, String sqlStr, int left, int right) {
        return sqlParam.doHandle(sqlStr, left, right);
    }
}
