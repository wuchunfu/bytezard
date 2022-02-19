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

package io.simforce.bytezard.metric.plugin;

import io.sinforce.bytezard.metric.api.SqlMetric;

public class NullCount implements SqlMetric {

    @Override
    public String getName() {
        return "NullCount";
    }

    @Override
    public String getType() {
        return "SingleTable";
    }

    @Override
    public String getInvalidateItemsSql() {
        return "SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '') AND (${src_filter})";
    }

    @Override
    public String getActualValueSql() {
        return "SELECT COUNT(*) AS invalidate_count FROM invalidate_items";
    }

    @Override
    public boolean isInvalidateItemsCanOutput() {
        return true;
    }
}
