/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lonewolfworks.wolke.task.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lonewolfworks.wolke.aws.ecs.broker.ddoswaf.DdosWafBrokerProperties;
import com.lonewolfworks.wolke.aws.ecs.broker.newrelic.NewRelicBrokerProperties;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.S3BrokerProperties;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.SSLCertificate;
import com.lonewolfworks.wolke.aws.ecs.logging.SplunkInstance;
import com.lonewolfworks.wolke.task.auth0.Auth0BrokerProperties;
import com.lonewolfworks.wolke.task.common.CommonTaskProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ECSPushTaskProperties extends CommonTaskProperties {

    private NewRelicBrokerProperties newRelic;
    private DdosWafBrokerProperties ddosWaf;
    private List<SSLCertificate> sslCertificates = new ArrayList<>();
    private String ecsConsoleLinkPattern;
    private List<SplunkInstance> splunkInstances = new ArrayList<>();
    private String rdsCredentialBrokerImage;
    private String dnsBrokerLambda;
    private List<String> externalElbSecurityGroups = new ArrayList<>();
    private S3BrokerProperties s3;
    private Auth0BrokerProperties auth0;

    //typically only need one logging bucket
    private String logsBucket;
    //Allow overrides to accomodate non-standard/clumsy LFG setup
    private String s3LogsBucket;
    private String elbLogsBucket;
    
    
    public String getS3LogsBucket() {
		return s3LogsBucket;
	}

	public void setS3LogsBucket(String s3LogsBucket) {
		this.s3LogsBucket = s3LogsBucket;
	}

	public String getElbLogsBucket() {
		return elbLogsBucket;
	}

	public void setElbLogsBucket(String elbLogsBucket) {
		this.elbLogsBucket = elbLogsBucket;
	}


    @Override
    public ECSPushTaskProperties withCompany(final String company) {
        setCompany(company);
        return this;
    }

    @Override
    public ECSPushTaskProperties withSbu(final String sbu) {
        setSbu(sbu);
        return this;
    }

    @Override
    public ECSPushTaskProperties withOrg(final String org) {
        setOrg(org);
        return this;
    }

    @Override
    public ECSPushTaskProperties withEngine(final String engine) {
        setEngine(engine);
        return this;
    }

    public NewRelicBrokerProperties getNewRelic() {
        return newRelic;
    }

    public void setNewRelic(NewRelicBrokerProperties newRelic) {
        this.newRelic = newRelic;
    }

    public DdosWafBrokerProperties getDdosWaf() {
        return ddosWaf;
    }

    public void setDdosWaf(DdosWafBrokerProperties ddosWaf) {
        this.ddosWaf = ddosWaf;
    }

    public List<SSLCertificate> getSslCertificates() {
        return sslCertificates;
    }

    public void setSslCertificates(
            List<SSLCertificate> sslCertificates) {
        this.sslCertificates = sslCertificates;
    }

    public String getEcsConsoleLinkPattern() {
        return ecsConsoleLinkPattern;
    }

    public void setEcsConsoleLinkPattern(String ecsConsoleLinkPattern) {
        this.ecsConsoleLinkPattern = ecsConsoleLinkPattern;
    }

    public List<SplunkInstance> getSplunkInstances() {
        return splunkInstances;
    }

    public void setSplunkInstances(List<SplunkInstance> splunkInstances) {
        this.splunkInstances = splunkInstances;
    }

    public String getRdsCredentialBrokerImage() {
        return rdsCredentialBrokerImage;
    }

    public void setRdsCredentialBrokerImage(String rdsCredentialBrokerImage) {
        this.rdsCredentialBrokerImage = rdsCredentialBrokerImage;
    }

    public String getDnsBrokerLambda() {
        return dnsBrokerLambda;
    }

    public void setDnsBrokerLambda(String dnsBrokerLambda) {
        this.dnsBrokerLambda = dnsBrokerLambda;
    }

    public List<String> getExternalElbSecurityGroups() {
        return externalElbSecurityGroups;
    }

    public void setExternalElbSecurityGroups(List<String> externalElbSecurityGroups) {
        this.externalElbSecurityGroups = externalElbSecurityGroups;
    }

    public S3BrokerProperties getS3() {
        return s3;
    }

    public void setS3(S3BrokerProperties s3) {
        this.s3 = s3;
    }

    public String getLogsBucket() {
        return logsBucket;
    }

    public void setLogsBucket(String logsBucket) {
        this.logsBucket = logsBucket;
    }

    public Auth0BrokerProperties getAuth0() {
        return auth0;
    }

    public void setAuth0(Auth0BrokerProperties auth0) {
        this.auth0 = auth0;
    }

    public ECSPushTaskProperties withNewRelic(
            final NewRelicBrokerProperties newRelic) {
        this.newRelic = newRelic;
        return this;
    }

    public ECSPushTaskProperties withDdosWaf(
            final DdosWafBrokerProperties ddosWaf) {
        this.ddosWaf = ddosWaf;
        return this;
    }

    public ECSPushTaskProperties withSslCertificates(
            final List<SSLCertificate> sslCertificates) {
        this.sslCertificates = sslCertificates;
        return this;
    }

    public ECSPushTaskProperties withEcsConsoleLinkPattern(final String ecsConsoleLinkPattern) {
        this.ecsConsoleLinkPattern = ecsConsoleLinkPattern;
        return this;
    }

    public ECSPushTaskProperties withSplunkInstances(
            final List<SplunkInstance> splunkInstances) {
        this.splunkInstances = splunkInstances;
        return this;
    }

    public ECSPushTaskProperties withRdsCredentialBrokerImage(final String rdsCredentialBrokerImage) {
        this.rdsCredentialBrokerImage = rdsCredentialBrokerImage;
        return this;
    }

    public ECSPushTaskProperties withDnsBrokerLambda(final String dnsBrokerLambda) {
        this.dnsBrokerLambda = dnsBrokerLambda;
        return this;
    }

    public ECSPushTaskProperties withExternalElbSecurityGroups(
            final List<String> externalElbSecurityGroups) {
        this.externalElbSecurityGroups = externalElbSecurityGroups;
        return this;
    }

    public ECSPushTaskProperties withS3(final S3BrokerProperties s3) {
        this.s3 = s3;
        return this;
    }

    public ECSPushTaskProperties withLogsBucket(final String logsBucket) {
        this.logsBucket = logsBucket;
        return this;
    }

    public ECSPushTaskProperties withAuth0(final Auth0BrokerProperties auth0) {
        this.auth0 = auth0;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ECSPushTaskProperties.class.getSimpleName() + "[", "]")
                .add("newRelic=" + newRelic)
                .add("ddosWaf=" + ddosWaf)
                .add("sslCertificates=" + sslCertificates)
                .add("ecsConsoleLinkPattern='" + ecsConsoleLinkPattern + "'")
                .add("splunkInstances=" + splunkInstances)
                .add("rdsCredentialBrokerImage='" + rdsCredentialBrokerImage + "'")
                .add("dnsBrokerLambda='" + dnsBrokerLambda + "'")
                .add("externalElbSecurityGroups=" + externalElbSecurityGroups)
                .add("s3=" + s3)
                .add("logsBucket='" + logsBucket + "'")
                .add("auth0=" + auth0)
                .toString();
    }
}