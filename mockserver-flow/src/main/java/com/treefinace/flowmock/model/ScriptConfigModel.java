package com.treefinace.flowmock.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptConfigModel {
    private String scriptCode;
    private ScriptType scriptType;
    private String scriptContent;
    private String scriptDesc;
    private Integer scriptIndex = 0;
}