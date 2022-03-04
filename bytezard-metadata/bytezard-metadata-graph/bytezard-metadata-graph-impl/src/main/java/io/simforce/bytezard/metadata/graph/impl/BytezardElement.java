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
package io.simforce.bytezard.metadata.graph.impl;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.simforce.bytezard.metadata.graph.api.IEdge;
import io.simforce.bytezard.metadata.graph.api.IElement;
import io.simforce.bytezard.metadata.graph.api.IVertex;

/**
 * Implementation of IElement.
 *
 * @param <T> the implementation class of the wrapped Janus element
 * that is stored.
 */
public class BytezardElement<T extends Element> implements IElement {

    private final T element;

    protected BytezardGraph graph;

    public BytezardElement(BytezardGraph graph, T element) {
        this.element = element;
        this.graph = graph;
    }

    @Override
    public <T> T getProperty(String propertyName, Class<T> clazz) {

        //add explicit logic to return null if the property does not exist
        //This is the behavior I expects.  Janus throws an exception
        //in this scenario.
        Property p = getOriginalElement().property(propertyName);
        if (p.isPresent()) {
            Object propertyValue= p.value();
            if (propertyValue == null) {
                return null;
            }

            if (IEdge.class == clazz) {
                return (T)graph.getEdge(propertyValue.toString());
            }

            if (IVertex.class == clazz) {
                return (T)graph.getVertex(propertyValue.toString());
            }

            return (T)propertyValue;

        }
        return null;
    }

    /**
     * Gets all of the values of the given property.
     * @param propertyName
     * @return
     */
    @Override
    public <T> Collection<T> getPropertyValues(String propertyName, Class<T> type) {
        return Collections.singleton(getProperty(propertyName, type));
    }

    @Override
    public Set<String> getPropertyKeys() {
        return getOriginalElement().keys();
    }

    @Override
    public void removeProperty(String propertyName) {
        Iterator<? extends Property<String>> it = getOriginalElement().properties(propertyName);
        while(it.hasNext()) {
            Property<String> property = it.next();
            property.remove();
        }
    }

    @Override
    public void removePropertyValue(String propertyName, Object propertyValue) {
        Iterator<? extends Property<Object>> it = getOriginalElement().properties(propertyName);

        while (it.hasNext()) {
            Property currentProperty = it.next();
            Object currentPropertyValue = currentProperty.value();

            if (Objects.equals(currentPropertyValue, propertyValue)) {
                currentProperty.remove();
                break;
            }
        }
    }

    @Override
    public void removeAllPropertyValue(String propertyName, Object propertyValue) {
        Iterator<? extends Property<Object>> it = getOriginalElement().properties(propertyName);

        while (it.hasNext()) {
            Property currentProperty = it.next();
            Object currentPropertyValue = currentProperty.value();

            if (Objects.equals(currentPropertyValue, propertyValue)) {
                currentProperty.remove();
            }
        }
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        if (value == null) {
            Object existingVal = getProperty(propertyName, Object.class);
            if (existingVal != null) {
                removeProperty(propertyName);
            }
        } else {
            getOriginalElement().property(propertyName, value);
        }
    }

    @Override
    public Object getId() {
        return element.id();
    }

    @Override
    public T getOriginalElement() {
        return element;
    }

    @Override
    public int hashCode() {
        int result = 37;
        result = 17 * result + getClass().hashCode();
        result = 17 * result + getOriginalElement().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }
        IElement otherElement = (IElement) other;
        return getOriginalElement().equals(otherElement.getOriginalElement());
    }

    @Override
    public List<String> getListProperty(String propertyName) {
        List<String> value =  getProperty(propertyName, List.class);
        return value;
    }

    @Override
    public void setListProperty(String propertyName, List<String> values) {
        setProperty(propertyName, values);
    }

    @Override
    public <T> void setJsonProperty(String propertyName, T value) {
        setProperty(propertyName, value);
    }

    @Override
    public <T> T getJsonProperty(String propertyName) {
        return (T)getProperty(propertyName, String.class);
    }

    @Override
    public String getIdForDisplay() {
        return getId().toString();
    }

    @Override
    public <V> List<V> getListProperty(String propertyName, Class<V> elementType) {

        List<String> value = getListProperty(propertyName);

        if (value == null || value.isEmpty()) {
            return (List<V>)value;
        }

        if (IEdge.class.isAssignableFrom(elementType)) {

            return (List<V>) value
                    .stream()
                    .map(input -> graph.getEdge(input)).collect(Collectors.toList());
        }

        if (IVertex.class.isAssignableFrom(elementType)) {

            return (List<V>) value
                    .stream()
                    .map(input -> graph.getVertex(input)).collect(Collectors.toList());
        }

        return (List<V>)value;
    }


    @Override
    public void setPropertyFromElementsIds(String propertyName, List<IElement> values) {
        List<String> propertyValue = new ArrayList<>(values.size());
        for(IElement value: values) {
            propertyValue.add(value.getIdForDisplay());
        }
        setProperty(propertyName, propertyValue);
    }


    @Override
    public void setPropertyFromElementId(String propertyName, IElement value) {
        setProperty(propertyName, value.getIdForDisplay());
    }

    @Override
    public boolean isIdAssigned() {
        return true;
    }

}
