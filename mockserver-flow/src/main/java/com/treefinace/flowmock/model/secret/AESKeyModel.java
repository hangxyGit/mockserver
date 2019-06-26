package com.treefinace.flowmock.model.secret;

import com.treefinace.flowmock.model.SecretKeyModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AESKeyModel extends SecretKeyModel{
    private int keySize = 16;
    // 密钥算法
    private String algorithm = "AES";
    // 加解密算法/工作模式/填充方式
    private String algorithmStr = "AES/ECB/PKCS5Padding";
    private String provider = "BC";
    private String key;
}
