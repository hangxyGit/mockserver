package com.treefinace.flowmock.flow;

import org.springframework.beans.factory.InitializingBean;

/**
 * mock 刷新
 */
public interface FlowMockRefresher extends InitializingBean {

    void refreshAll();

    void refresh(String projectCode);

    @Override
    default void afterPropertiesSet() throws Exception{
        refreshAll();
    }
}
