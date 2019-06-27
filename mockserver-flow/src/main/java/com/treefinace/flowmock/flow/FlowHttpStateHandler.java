package com.treefinace.flowmock.flow;

import org.mockserver.mock.HttpStateHandler;
import org.mockserver.scheduler.Scheduler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FlowHttpStateHandler extends HttpStateHandler {

    private FlowExpectationSerializer flowExpectationSerializer;

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
        } catch (Exception e) {}
        }
    }
