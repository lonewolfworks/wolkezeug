package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

public enum GrantTypes {
    IMPLICIT("implicit"),
    AUTHORIZATION_CODE("authorization_code"),
    CLIENT_CREDENTIALS("client_credentials"),
    PASSWORD("password"),
    REFRESH_TOKEN("refresh_token"),
    PASSWORD_REALM("http://auth0.com/oauth/grant-type/password-realm"),
    MFA_OOB("http://auth0.com/oauth/grant-type/mfa-oob"),
    MFA_OTP("http://auth0.com/oauth/grant-type/mfa-otp"),
    MFA_RECOVER("http://auth0.com/oauth/grant-type/mfa-recovery-code"),
    OTP("http://auth0.com/oauth/grant-type/passwordless/otp");

    private final String code;

    GrantTypes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
