package com.treefinace.flowmock.flow;

import org.mockserver.scheduler.Scheduler;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class SchedulerFactory implements FactoryBean<Scheduler> {

    @Override
    public Scheduler getObject() throws Exception {
        return new Scheduler();
    }

    @Override
    public Class<?> getObjectType() {
        return Scheduler.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
