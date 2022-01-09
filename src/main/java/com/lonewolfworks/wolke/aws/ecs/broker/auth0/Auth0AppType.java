package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

public enum Auth0AppType {
    SPA("spa"),
    M2M("non_interactive"),
    WEB("regular_web"),
    NATIVE("native");

    private final String description;

    Auth0AppType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
