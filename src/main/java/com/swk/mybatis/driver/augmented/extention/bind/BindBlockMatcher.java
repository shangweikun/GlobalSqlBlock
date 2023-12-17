package com.swk.mybatis.driver.augmented.extention.bind;

import com.swk.mybatis.driver.augmented.extention.BlockMatcher;

public class BindBlockMatcher implements BlockMatcher {
    @Override
    public boolean match(String text) {
        return false;
    }
}
