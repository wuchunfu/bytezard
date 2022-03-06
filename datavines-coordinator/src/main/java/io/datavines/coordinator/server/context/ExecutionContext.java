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
package io.datavines.coordinator.server.context;

import io.datavines.remote.command.Command;
import io.datavines.remote.command.RequestClientType;

/**
 *  execution context
 *  @author zixi0825
 */
public class ExecutionContext {

    /**
     *  command
     */
    private final Command command;

    /**
     *  executor type : executor or client
     */
    private final RequestClientType requestClientType;

    public ExecutionContext(Command command, RequestClientType requestClientType) {
        this.command = command;
        this.requestClientType = requestClientType;
    }

    public Command getCommand() {
        return command;
    }

    public RequestClientType getRequestClientType() {
        return requestClientType;
    }
}
