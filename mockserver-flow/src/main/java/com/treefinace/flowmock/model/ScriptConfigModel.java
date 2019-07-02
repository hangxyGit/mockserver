package com.treefinace.flowmock.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptConfigModel {
    private String scriptCode;
    private ScriptType scriptType = ScriptType.JAVA;
    private HandleType handleType = HandleType.pre;
    private String scriptContent;
    private String scriptDesc;
    private Integer scriptIndex = 0;
}