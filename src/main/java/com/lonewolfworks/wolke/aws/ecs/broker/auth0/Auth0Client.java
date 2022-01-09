package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0Client {

    private String name;
    private String description;
    private Auth0AppType appType;
    private String logoUri;
    private Boolean isFirstParty;
    private Boolean oidcConformant;
    private List<String> callbacks;
    private List<String> allowedOrigins;
    private List<String> webOrigins;
    private List<GrantTypes> grantTypes;
    private List<String> clientAliases;
    private List<String> allowedClients;
    private List<String> allowedLogoutUrls;
    private Boolean useAuth0SSO;
    private Boolean ssoDisabled;
    private Boolean customLoginPageOn;
    private String customLoginPagePreview;
    private String formTemplate;
    private String tokenEndpointAuthMethod;
    private Map<String, Object> clientMetadata;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Auth0AppType getAppType() {
        return appType;
    }

    public void setAppType(Auth0AppType appType) {
        this.appType = appType;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public Boolean getFirstParty() {
        return isFirstParty;
    }

    public void setFirstParty(Boolean firstParty) {
        isFirstParty = firstParty;
    }

    public Boolean getOidcConformant() {
        return oidcConformant;
    }

    public void setOidcConformant(Boolean oidcConformant) {
        this.oidcConformant = oidcConformant;
    }

    public List<String> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<String> callbacks) {
        this.callbacks = callbacks;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getWebOrigins() {
        return webOrigins;
    }

    public void setWebOrigins(List<String> webOrigins) {
        this.webOrigins = webOrigins;
    }

    public List<GrantTypes> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<GrantTypes> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public List<String> getClientAliases() {
        return clientAliases;
    }

    public void setClientAliases(List<String> clientAliases) {
        this.clientAliases = clientAliases;
    }

    public List<String> getAllowedClients() {
        return allowedClients;
    }

    public void setAllowedClients(List<String> allowedClients) {
        this.allowedClients = allowedClients;
    }

    public List<String> getAllowedLogoutUrls() {
        return allowedLogoutUrls;
    }

    public void setAllowedLogoutUrls(List<String> allowedLogoutUrls) {
        this.allowedLogoutUrls = allowedLogoutUrls;
    }

    public Boolean getUseAuth0SSO() {
        return useAuth0SSO;
    }

    public void setUseAuth0SSO(Boolean useAuth0SSO) {
        this.useAuth0SSO = useAuth0SSO;
    }

    public Boolean getSsoDisabled() {
        return ssoDisabled;
    }

    public void setSsoDisabled(Boolean ssoDisabled) {
        this.ssoDisabled = ssoDisabled;
    }

    public Boolean getCustomLoginPageOn() {
        return customLoginPageOn;
    }

    public void setCustomLoginPageOn(Boolean customLoginPageOn) {
        this.customLoginPageOn = customLoginPageOn;
    }

    public String getCustomLoginPagePreview() {
        return customLoginPagePreview;
    }

    public void setCustomLoginPagePreview(String customLoginPagePreview) {
        this.customLoginPagePreview = customLoginPagePreview;
    }

    public String getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(String formTemplate) {
        this.formTemplate = formTemplate;
    }

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public Map<String, Object> getClientMetadata() {
        return clientMetadata;
    }

    public void setClientMetadata(Map<String, Object> clientMetadata) {
        this.clientMetadata = clientMetadata;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0Client.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("appType=" + appType)
                .add("logoUri='" + logoUri + "'")
                .add("isFirstParty=" + isFirstParty)
                .add("oidcConformant=" + oidcConformant)
                .add("callbacks=" + callbacks)
                .add("allowedOrigins=" + allowedOrigins)
                .add("webOrigins=" + webOrigins)
                .add("grantTypes=" + grantTypes)
                .add("clientAliases=" + clientAliases)
                .add("allowedClients=" + allowedClients)
                .add("allowedLogoutUrls=" + allowedLogoutUrls)
                .add("useAuth0SSO=" + useAuth0SSO)
                .add("ssoDisabled=" + ssoDisabled)
                .add("customLoginPageOn=" + customLoginPageOn)
                .add("customLoginPagePreview='" + customLoginPagePreview + "'")
                .add("formTemplate='" + formTemplate + "'")
                .add("tokenEndpointAuthMethod='" + tokenEndpointAuthMethod + "'")
                .add("clientMetadata=" + clientMetadata)
                .toString();
    }
}
