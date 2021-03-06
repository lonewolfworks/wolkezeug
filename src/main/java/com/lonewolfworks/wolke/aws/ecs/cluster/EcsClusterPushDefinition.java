/*
 * Copyright (C) 2018, Liberty Mutual Group
 *
 * Created on 7/31/18
 */
package com.lonewolfworks.wolke.aws.ecs.cluster;

import java.util.ArrayList;
import java.util.List;

public class EcsClusterPushDefinition {
    private String clusterName;
    private String sharedCftName;
    private String instanceCftName;
    private int instanceRegistrationTimeout = 3;
    private boolean drainingEnabled = true;
    private int maxConcurrentDraining = 3;
    private List<CftParameter> cftParameters = new ArrayList<>();

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getSharedCftName() {
        return sharedCftName;
    }

    public void setSharedCftName(String sharedCftName) {
        this.sharedCftName = sharedCftName;
    }

    public String getInstanceCftName() {
        return instanceCftName;
    }

    public void setInstanceCftName(String instanceCftName) {
        this.instanceCftName = instanceCftName;
    }

    public int getInstanceRegistrationTimeout() {
        return instanceRegistrationTimeout;
    }

    public void setInstanceRegistrationTimeout(int instanceRegistrationTimeout) {
        this.instanceRegistrationTimeout = instanceRegistrationTimeout;
    }

    public boolean isDrainingEnabled() {
        return drainingEnabled;
    }

    public void setDrainingEnabled(boolean drainingEnabled) {
        this.drainingEnabled = drainingEnabled;
    }

    public int getMaxConcurrentDraining() {
        return maxConcurrentDraining;
    }

    public void setMaxConcurrentDraining(int maxConcurrentDraining) {
        this.maxConcurrentDraining = maxConcurrentDraining;
    }

    public List<CftParameter> getCftParameters() {
        return cftParameters;
    }

    public void setCftParameters(List<CftParameter> cftParameters) {
        this.cftParameters = cftParameters;
    }
}
