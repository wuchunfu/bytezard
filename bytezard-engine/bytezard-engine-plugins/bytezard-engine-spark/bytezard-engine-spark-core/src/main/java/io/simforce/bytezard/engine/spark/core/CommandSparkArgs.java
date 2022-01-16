/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.simforce.bytezard.engine.spark.core;

import com.beust.jcommander.Parameter;

import io.simforce.bytezard.engine.core.command.DeployModeValidator;

public class CommandSparkArgs {

    @Parameter(names = {"-c", "--config"},
        description = "config file",
        required = true)
    private String configFile = "application.conf";

    @Parameter(names = {"-e", "--deploy-mode"},
        description = "spark deploy mode",
        required = true,
        validateWith = DeployModeValidator.class)
    private String deployMode = "client";

    @Parameter(names = {"-m", "--master"},
        description = "spark master",
        required = true)
    private String master = null;

    @Parameter(names = {"-i", "--variable"},
        description = "variable substitution, such as -i city=beijing, or -i date=20190318")
    private String variable = null;

    @Parameter(names = {"-t", "--check"},
        description = "check config")
    private boolean testConfig = false;

    public String getConfigFile() {
        return configFile;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public boolean isTestConfig() {
        return testConfig;
    }

}
