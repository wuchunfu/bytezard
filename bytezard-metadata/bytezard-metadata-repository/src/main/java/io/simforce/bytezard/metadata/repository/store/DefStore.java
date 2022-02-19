/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.simforce.bytezard.metadata.repository.store;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;

/**
 * Interface for graph persistence store for TypeDef
 */
public interface DefStore<T extends BaseTypeDef> {

    Vertex preCreate(T typeDef) throws BaseException;

    T create(T typeDef, Vertex preCreateResult) throws BaseException;

    List<T> getAll() throws BaseException;

    T getByName(String name) throws BaseException;

    T getByGuid(String guid) throws BaseException;

    T update(T typeDef) throws BaseException;

    T updateByName(String name, T typeDef) throws BaseException;

    T updateByGuid(String guid, T typeDef) throws BaseException;

    Vertex preDeleteByName(String name) throws BaseException;

    void deleteByName(String name, Vertex preDeleteResult) throws BaseException;

    Vertex preDeleteByGuid(String guid) throws BaseException;

    void deleteByGuid(String guid, Vertex preDeleteResult) throws BaseException;
}
