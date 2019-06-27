package com.treefinace.flowmock.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.treefinace.flowmock.flow.model.FlowExpectationDTO;
import com.treefinace.flowmock.model.secret.AESKeyModel;
import com.treefinace.flowmock.model.secret.DESKeyModel;
import com.treefinace.flowmock.model.secret.RSAKeyModel;
import com.treefinace.flowmock.model.secret.SHA256KeyModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProjectModel {
    private String projCode;
    private String projName;
    private String projDesc;
    private String projDomain;
    private Map<String, ConfigCenterModel> configCenters;
    private Map<String, DbConfigModel> dbConfigs;
    private Map<String, AESKeyModel> aesKeys;
    private Map<String, RSAKeyModel> rsaKeys;
    private Map<String, DESKeyModel> desKeys;
    private Map<String, SHA256KeyModel> sha256Keys;
    // 前置处理
    private Map<String, ScriptConfigModel> preProcess;
    // 后置处理
    private Map<String, ScriptConfigModel> postProcess;

    @JSONField(serialize = false)
    private Map<String, FlowExpectationDTO> flowExpectations;

}
