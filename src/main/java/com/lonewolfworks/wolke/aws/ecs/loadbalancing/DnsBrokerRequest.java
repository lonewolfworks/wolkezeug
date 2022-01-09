package com.lonewolfworks.wolke.aws.ecs.loadbalancing;

public class DnsBrokerRequest {

    private String elbName;
    private String elbType;
    private String protocol;
    private String vanityUrl;
    private boolean internetFacingUrlScheme;

    public String getElbName() {
        return elbName;
    }

    public void setElbName(String elbName) {
        this.elbName = elbName;
    }

    public String getElbType() {
        return elbType;
    }

    public void setElbType(String elbType) {
        this.elbType = elbType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getVanityUrl() {
        return vanityUrl;
    }

    public void setVanityUrl(String vanityUrl) {
        this.vanityUrl = vanityUrl;
    }

    public boolean isInternetFacingUrlScheme() {
        return internetFacingUrlScheme;
    }

    public void setInternetFacingUrlScheme(boolean internetFacingUrlScheme) {
        this.internetFacingUrlScheme = internetFacingUrlScheme;
    }

    @Override
    public String toString() {
        return "DnsBrokerRequest{" +
            "elbName='" + elbName + '\'' +
            ", elbType='" + elbType + '\'' +
            ", protocol='" + protocol + '\'' +
            ", vanityUrl='" + vanityUrl + '\'' +
            ", internetFacingUrlScheme=" + internetFacingUrlScheme +
            '}';
    }

    public DnsBrokerRequest withElbName(final String elbName) {
        this.elbName = elbName;
        return this;
    }

    public DnsBrokerRequest withElbType(final String elbType) {
        this.elbType = elbType;
        return this;
    }

    public DnsBrokerRequest withProtocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    public DnsBrokerRequest withVanityUrl(final String vanityUrl) {
        this.vanityUrl = vanityUrl;
        return this;
    }

    public DnsBrokerRequest withInternetFacingUrlScheme(final boolean internetFacingUrlScheme) {
        this.internetFacingUrlScheme = internetFacingUrlScheme;
        return this;
    }
}