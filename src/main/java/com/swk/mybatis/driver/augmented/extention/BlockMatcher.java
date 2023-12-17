package com.swk.mybatis.driver.augmented.extention;

@FunctionalInterface
public interface BlockMatcher {

    boolean match(String text);
}
