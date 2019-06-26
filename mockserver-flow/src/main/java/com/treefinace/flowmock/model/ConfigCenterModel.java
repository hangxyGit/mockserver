package com.treefinace.flowmock.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfigCenterModel {
    private String projCode;
    private String profile;
    private String host;
    private Integer port;
}
