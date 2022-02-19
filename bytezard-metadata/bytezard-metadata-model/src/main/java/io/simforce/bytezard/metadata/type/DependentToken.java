/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.metadata.type;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.instance.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DependentToken implements io.simforce.bytezard.metadata.type.TemplateToken {
    private final String       path;
    private final List<String> objectPath;
    private final String       attrName;

    private static final String DYN_ATTRIBUTE_NAME_SEPARATOR = "\\.";

    public DependentToken(String path){
        List<String> objectPath = new ArrayList<>(Arrays.asList(path.split(DYN_ATTRIBUTE_NAME_SEPARATOR)));

        this.path      = path;
        this.attrName  = objectPath.remove(objectPath.size() - 1);
        this.objectPath = Collections.unmodifiableList(objectPath);
    }

    @Override
    public String eval(Entity entity) throws BaseException {
        return "TEMP";
    }

    @Override
    public String getValue() {
        return path;
    }
}
