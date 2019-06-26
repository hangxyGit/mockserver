package com.treefinace.flowmock.utils;

import com.google.common.base.Joiner;
import com.treefinace.flowmock.model.Constants;

public abstract class RedisKeyGenerator {
    public static final String PREFIX = "pay-mockserver";


    public static String get(String... keys) {
        return Joiner.on(":").useForNull("null").join(PREFIX, Constants.VERSION, keys);
    }
}
