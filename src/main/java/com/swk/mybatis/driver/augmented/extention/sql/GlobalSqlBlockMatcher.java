package com.swk.mybatis.driver.augmented.extention.sql;

import com.swk.mybatis.driver.augmented.extention.BlockMatcher;

public class GlobalSqlBlockMatcher implements BlockMatcher {
    @Override
    public boolean match(String text) {
        return false;
    }
}
