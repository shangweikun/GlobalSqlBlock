package com.swk.mybatis.driver.augmented.param;

public class ListSQLParam extends SQLParam {

    private static final String _FOREACH_STRING_PREFIX = "\n <foreach collection=\"";
    private static final String _FOREACH_STRING_SUFFIX = "\" item=\"item\" index=\"index\" separator=\",\">\n" +
            "                #{item}\n" +
            "            </foreach>";


    protected ListSQLParam(String paramName) {
        super(paramName);
    }

    @Override
    protected String doHandle(String sql, Integer left, Integer right) {

        String prefix = sql.substring(0, left);
        String suffix = sql.substring(right + 1);

        return prefix + _FOREACH_STRING_PREFIX + this.paramName + _FOREACH_STRING_SUFFIX + suffix;
    }
}
