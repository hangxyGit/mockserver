package com.treefinace.flowmock.model.secret;

import com.treefinace.flowmock.model.SecretKeyModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RSAKeyModel extends SecretKeyModel {
    //密钥算法
    private String algorithm = "RSA/ECB/PKCS1Padding";
    private String provider = "BC";

    private String privateKey;
    private String publicKey;
}
