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
package com.lonewolfworks.wolke.aws.ecs;

import com.lonewolfworks.wolke.aws.AwsExecException;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CliPropertyHandler implements PropertyHandler {

    static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9\\.\\_\\-]+)\\}");
    private static final Logger LOGGER = LoggerFactory.getLogger(CliPropertyHandler.class);
    private Properties props = new Properties();


    private Set<String> propertyKeysUsed = new HashSet<>();

    private HermanLogger hermanLogger;
    private String environmentName;
    private String rootDirectory;
    private Map<String, String> customVariables;

    public CliPropertyHandler(HermanLogger hermanLogger, String environmentName, String rootDirectory, Map<String, String> customVariables) {
        this.hermanLogger = hermanLogger;
        this.environmentName = environmentName;
        this.rootDirectory = rootDirectory;
        this.customVariables = customVariables;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.lonewolfworks.wolke.aws.ecs.PropertyHandler#addProperty(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void addProperty(String key, String value) {
        props.put(key, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.lonewolfworks.wolke.aws.ecs.PropertyHandler#mapInProperties(java.lang.String)
     */
    @Override
    public String mapInProperties(String template) {
        importPropFiles();

        Set<String> propertiesToMatch = getPropertiesToMatch(template);
        String result = template;
        for (String prop : propertiesToMatch) {
            String token = prop.replace("${", "");
            token = token.replace("}", "");
            Pattern groupPattern = Pattern.compile("\\$\\{(" + token + ")\\}");
            Matcher matcher = groupPattern.matcher(result);
            String value = lookupVariable(token);
            if (value == null) {
                throw new AwsExecException("Missing property set for " + token);
            }
            result = matcher.replaceAll(value);
        }
        return result;

    }

    Set<String> getPropertiesToMatch(String template) {
        Set<String> propertiesToMatch = new HashSet<>();
        Matcher propMatcher = PROPERTY_PATTERN.matcher(template);
        while (propMatcher.find()) {
            String propVal = propMatcher.group();
            propertiesToMatch.add(propVal);
        }
        return propertiesToMatch;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.lonewolfworks.wolke.aws.ecs.PropertyHandler#lookupProperties(java.lang.String)
     */
    @Override
    public Properties lookupProperties(String... propList) {
        Properties newProps = new Properties();
        for (String prop : propList) {
            String val = lookupVariable(prop);
            if (val != null) {
                newProps.put(prop, val);
            }
        }
        return newProps;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.lonewolfworks.wolke.aws.ecs.PropertyHandler#lookupVariable(java.lang.String)
     */
    @Override
    public String lookupVariable(String key) {
        if (this.props.containsKey(key)) {
            return this.props.getProperty(key);
        }
        return this.customVariables.getOrDefault(key, null);
    }

    private void importPropFiles() {
        FileUtil util = new FileUtil(this.rootDirectory, this.hermanLogger);
        String envProps = util.findFile(this.environmentName + ".properties", true);

        if (props != null && envProps != null) {
            try {
                InputStream propStream = new ByteArrayInputStream(envProps.getBytes());
                props.load(propStream);
            } catch (IOException e) {
                LOGGER.debug("Error loading properties file: " + this.environmentName, e);
                this.hermanLogger.addLogEntry("Error loading " + this.environmentName + ".properties: " + e.getMessage());
            }
        } else {
            hermanLogger.addErrorLogEntry("No property file was loaded - we looked for " + this.environmentName + ".properties");
        }
    }

    public Set<String> getPropertyKeysUsed() {
        return propertyKeysUsed;
    }

}
