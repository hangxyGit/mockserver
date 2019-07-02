package com.treefinace.flowmock.flow;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.HttpStateHandler;
import org.mockserver.model.HttpRequest;
import org.mockserver.scheduler.Scheduler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

public class FlowHttpStateHandler extends HttpStateHandler {

    private FlowExpectationSerializer flowExpectationSerializer;

    private final Cache<HttpRequest, Expectation> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .maximumSize(2000).build();

    public FlowHttpStateHandler(Scheduler scheduler) {
        super(scheduler);
        flowExpectationSerializer = new FlowExpectationSerializer(getMockServerLogger());

        // 强制写入flowExpectationSerializer
        try {
            Field nameField = HttpStateHandler.class.getDeclaredField("expectationSerializer");
            Field modifiersField = Field.class.getDeclaredField("modifiers"); //①
            modifiersField.setAccessible(true);
            modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL); //②
            nameField.setAccessible(true);
            nameField.set(this, flowExpectationSerializer);
        } catch (Exception e) {
        }
    }

    @Override
    public Expectation firstMatchingExpectation(HttpRequest request) {
        // 增加cache 避免重复mapping
        Expectation expectation = cache.getIfPresent(request);
        if (expectation == null) {
            expectation = super.firstMatchingExpectation(request);
            if (expectation != null) {
                cache.put(request, expectation);
            }
        }
        return expectation;
    }
}
