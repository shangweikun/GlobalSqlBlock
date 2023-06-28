package com.swk.mybatis.driver.augmented.param;

public class DefaultSQLParam extends SQLParam {

    private static final String _DEFAULT_PREFIX = "#{";

    private static final String _DEFAULT_SUFFIX = "}";

    protected DefaultSQLParam(String paramName) {
        super(paramName);
    }

    @Override
    protected String doHandle(String sql, Integer left, Integer right) {

        String prefix = sql.substring(0, left);
        String suffix = sql.substring(right + 1);

        return prefix + _DEFAULT_PREFIX + this.paramName + _DEFAULT_SUFFIX + suffix;
    }
}
