package com.treefinace.flowmock.model.secret;

import com.treefinace.flowmock.model.SecretKeyModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Base64KeyModel extends SecretKeyModel {
    // 编码方式
    String encoding;
}
