package com.treefinace.flowmock.model.secret;

import com.treefinace.flowmock.model.SecretKeyModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SHA256KeyModel extends SecretKeyModel {
    private String key;
}
