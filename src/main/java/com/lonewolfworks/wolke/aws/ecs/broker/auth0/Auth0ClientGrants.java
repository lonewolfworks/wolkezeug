package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0ClientGrants {
    private String audience;
    private List<String> scope;

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0ClientGrants.class.getSimpleName() + "[", "]")
                .add("audience='" + audience + "'")
                .add("scope=" + scope)
                .toString();
    }
}
