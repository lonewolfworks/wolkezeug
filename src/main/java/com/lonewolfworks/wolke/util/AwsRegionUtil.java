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
package com.lonewolfworks.wolke.util;

import com.google.common.collect.Maps;
import java.util.Map;

public class AwsRegionUtil {

    private AwsRegionUtil() {
        throw new IllegalAccessError("Utility class");
    }

    public static Map<String, String> getRegionList() {
        Map<String, String> list = Maps.newHashMap();
        list.put("us-east-1", "US East (N. Virginia)");
        list.put("us-east-2", "US East (Ohio)");
        list.put("us-west-1", "US West (N. California)");
        list.put("us-west-2", "US West (Oregon)");
        list.put("ca-central-1", "Canada (Central)");
        list.put("eu-central-1", "EU (Frankfurt)");
        list.put("eu-west-1", "EU (Ireland)");
        list.put("eu-west-2", "EU (London)");
        list.put("ap-northeast-1", "Asia Pacific (Tokyo)");
        list.put("ap-southeast-1", "Asia Pacific (Singapore)");
        list.put("ap-southeast-2", "Asia Pacific (Sydney)");
        return list;
    }
}
