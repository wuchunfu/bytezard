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

package io.datavines.engine.api;

/**
 * Constants
 */
public class EngineConstants {
    public static final String RESULT_TABLE_NAME = "result_table_name";

    public static final String SOURCE_TABLE_NAME = "source_table_name";

    public static final String TMP_TABLE_NAME = "tmp_table_name";

    /**
     * exit code success
     */
    public static final int EXIT_CODE_SUCCESS = 0;
    /**
     * exit code kill
     */
    public static final int EXIT_CODE_KILL = 137;
    /**
     * exit code failure
     */
    public static final int EXIT_CODE_FAILURE = -1;

    /**
     * application regex
     */
    public static final String APPLICATION_REGEX = "application_\\d+_\\d+";

    public static final String PID = "pid";
}
