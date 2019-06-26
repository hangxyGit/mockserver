package com.treefinace.flowmock.model;

import lombok.Data;

@Data
public class SecretKeyModel {
    // 密钥编码
    String secretCode;
    // 算法类型：rsa, base64,aes,des
    String secretModel;
    // 密钥描述名称
    String secretName;

    public SecretKeyModel() {
        // 类名去除后缀即为加密算法
        this.secretModel = getClass().getSimpleName().toUpperCase().replaceAll("KEYMODEL", "");
    }
}
