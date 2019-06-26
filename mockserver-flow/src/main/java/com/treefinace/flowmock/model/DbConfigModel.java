package com.treefinace.flowmock.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DbConfigModel {
    private String dbCode;
    private String dbUrl;
    private String dbUser;
    private String dbPwd;
}
