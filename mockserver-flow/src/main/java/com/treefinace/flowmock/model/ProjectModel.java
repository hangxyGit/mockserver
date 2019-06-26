package com.treefinace.flowmock.model;

import com.treefinace.flowmock.flow.FlowExpectation;
import com.treefinace.flowmock.model.secret.AESKeyModel;
import com.treefinace.flowmock.model.secret.DESKeyModel;
import com.treefinace.flowmock.model.secret.RSAKeyModel;
import com.treefinace.flowmock.model.secret.SHA256KeyModel;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
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
    private Map<String, ScriptConfigModel> preProccess;
    // 后置处理
    private Map<String, ScriptConfigModel> postProccess;

    private Map<String, FlowExpectation> flowExpectations;
}
