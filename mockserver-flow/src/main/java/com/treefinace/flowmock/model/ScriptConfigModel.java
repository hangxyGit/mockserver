package com.treefinace.flowmock.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScriptConfigModel {
    private String scriptCode;
    private ScriptType scriptType;
    private String scriptContent;
    private String scriptDesc;
    private Integer scriptIndex = 0;
}