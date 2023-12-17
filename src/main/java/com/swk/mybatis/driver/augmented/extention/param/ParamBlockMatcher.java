package com.swk.mybatis.driver.augmented.extention.param;

import com.swk.mybatis.driver.augmented.extention.BlockMatcher;

public class ParamBlockMatcher implements BlockMatcher {

    @Override
    public boolean match(String text) {
        return "@([0-9a-Z ]+)".matches(text);
    }
}
