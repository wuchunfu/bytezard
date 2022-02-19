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
package io.simforce.bytezard.metadata.repository.utils;

import static io.simforce.bytezard.metadata.type.Constants.CLASSIFICATION_NAMES_KEY;
import static io.simforce.bytezard.metadata.type.Constants.ENTITY_TYPE_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.INDEX_SEARCH_VERTEX_PREFIX_DEFAULT;
import static io.simforce.bytezard.metadata.type.Constants.INDEX_SEARCH_VERTEX_PREFIX_PROPERTY;
import static io.simforce.bytezard.metadata.type.Constants.PROPAGATED_CLASSIFICATION_NAMES_KEY;
import static io.simforce.bytezard.metadata.type.Constants.STATE_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.SUPER_TYPES_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.TYPENAME_PROPERTY_KEY;
import static io.simforce.bytezard.metadata.type.Constants.TYPE_NAME_PROPERTY_KEY;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.metadata.ErrorCode;
import io.simforce.bytezard.metadata.SortOrder;
import io.simforce.bytezard.metadata.exception.BaseException;
import io.simforce.bytezard.metadata.model.TypeCategory;
import io.simforce.bytezard.metadata.model.instance.Entity;
import io.simforce.bytezard.metadata.model.instance.Entity.Status;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;
import io.simforce.bytezard.metadata.model.typedef.EnumDef;
import io.simforce.bytezard.metadata.repository.GraphTransactionInterceptor;
import io.simforce.bytezard.metadata.type.Constants;
import io.simforce.bytezard.metadata.type.EntityType;
import io.simforce.bytezard.metadata.type.EnumType;
import io.simforce.bytezard.metadata.type.StructType;
import io.simforce.bytezard.metadata.type.StructType.Attribute;
import io.simforce.bytezard.metadata.type.Type;

/**
 * Utility methods for Graph.
 */
public class GraphUtils {
    private static final Logger LOG = LoggerFactory.getLogger(GraphUtils.class);

    public static final String PROPERTY_PREFIX             = Constants.INTERNAL_PROPERTY_KEY_PREFIX + "type.";
    public static final String SUPERTYPE_EDGE_LABEL        = PROPERTY_PREFIX + ".supertype";
    public static final String ENTITYTYPE_EDGE_LABEL       = PROPERTY_PREFIX + ".entitytype";
    public static final String RELATIONSHIPTYPE_EDGE_LABEL = PROPERTY_PREFIX + ".relationshipType";
    public static final String VERTEX_TYPE                 = "typeSystem";

    private static boolean USE_INDEX_QUERY_TO_FIND_ENTITY_BY_UNIQUE_ATTRIBUTES = false;
    private static boolean USE_UNIQUE_INDEX_PROPERTY_TO_FIND_ENTITY            = true;
    private static String  INDEX_SEARCH_PREFIX;

    static {
        try {
//            Configuration conf = ApplicationProperties.get();

            Configuration conf = new BaseConfiguration();
            USE_INDEX_QUERY_TO_FIND_ENTITY_BY_UNIQUE_ATTRIBUTES = conf.getBoolean(".use.index.query.to.find.entity.by.unique.attributes", USE_INDEX_QUERY_TO_FIND_ENTITY_BY_UNIQUE_ATTRIBUTES);
            USE_UNIQUE_INDEX_PROPERTY_TO_FIND_ENTITY            = conf.getBoolean(".unique.index.property.to.find.entity", USE_UNIQUE_INDEX_PROPERTY_TO_FIND_ENTITY);
            INDEX_SEARCH_PREFIX                                 = conf.getString(INDEX_SEARCH_VERTEX_PREFIX_PROPERTY, INDEX_SEARCH_VERTEX_PREFIX_DEFAULT);
        } catch (Exception excp) {
            LOG.error("Error reading configuration", excp);
        } finally {
            LOG.info(".use.index.query.to.find.entity.by.unique.attributes=" + USE_INDEX_QUERY_TO_FIND_ENTITY_BY_UNIQUE_ATTRIBUTES);
        }
    }

    public static String getTypeDefPropertyKey(BaseTypeDef typeDef) {
        return getTypeDefPropertyKey(typeDef.getName());
    }

    public static String getTypeDefPropertyKey(BaseTypeDef typeDef, String child) {
        return getTypeDefPropertyKey(typeDef.getName(), child);
    }

    public static String getTypeDefPropertyKey(String typeName) {
        return PROPERTY_PREFIX + typeName;
    }

    public static String getTypeDefPropertyKey(String typeName, String child) {
        return PROPERTY_PREFIX + typeName + "." + child;
    }

    public static String getIdFromVertex(Vertex vertex) {
        return getProperty(vertex, Constants.GUID_PROPERTY_KEY, String.class);
    }

    public static String getIdFromEdge(Edge edge) {
        return getProperty(edge, Constants.GUID_PROPERTY_KEY, String.class);
    }

    public static String getTypeName(Element element) {
        return getProperty(element, ENTITY_TYPE_PROPERTY_KEY, String.class);
    }

    public static String getEdgeLabel(String fromNode, String toNode) {
        return PROPERTY_PREFIX + "edge." + fromNode + "." + toNode;
    }

    public static final String EDGE_LABEL_PREFIX = "__";
    public static String getEdgeLabel(String property) {
        return EDGE_LABEL_PREFIX + property;
    }

    public static String getQualifiedAttributePropertyKey(StructType fromType, String attributeName) throws BaseException, BaseException {
        switch (fromType.getTypeCategory()) {
         case ENTITY:
         case STRUCT:
         case CLASSIFICATION:
             return fromType.getQualifiedAttributePropertyKey(attributeName);
        default:
            throw new BaseException(ErrorCode.UNKNOWN_TYPE, fromType.getTypeCategory().name());
        }
    }

    public static boolean isEntityVertex(Vertex vertex) {
        return StringUtils.isNotEmpty(getIdFromVertex(vertex)) && StringUtils.isNotEmpty(getTypeName(vertex));
    }

    public static boolean isTypeVertex(Vertex vertex) {
        return getProperty(vertex, TYPENAME_PROPERTY_KEY, String.class) != null;
    }

    public static boolean isReference(Type type) {
        return isReference(type.getTypeCategory());
    }

    public static boolean isReference(TypeCategory typeCategory) {
        return typeCategory == TypeCategory.STRUCT ||
               typeCategory == TypeCategory.ENTITY ||
               typeCategory == TypeCategory.OBJECT_ID_TYPE;
    }

    public static String encodePropertyKey(String key) {
        return Attribute.encodePropertyKey(key);
    }

    public static String decodePropertyKey(String key) {
        return Attribute.decodePropertyKey(key);
    }

    /**
     * Adds an additional value to a multi-property.
     *
     * @param propertyName
     * @param value
     */
    public static Vertex addProperty(Vertex vertex, String propertyName, Object value) {
        return addProperty(vertex, propertyName, value, false);
    }

    public static Vertex addEncodedProperty(Vertex vertex, String propertyName, Object value) {
        return addProperty(vertex, propertyName, value, true);
    }

    public static Edge addEncodedProperty(Edge edge, String propertyName, String value) {
        List<String> listPropertyValues = getListFromProperty(edge, propertyName,String.class);

        listPropertyValues.add(value);

        edge.property(propertyName).remove();

        edge.property(propertyName, listPropertyValues);

        return edge;
    }

    public static Vertex addProperty(Vertex vertex, String propertyName, Object value, boolean isEncoded) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> addProperty({}, {}, {})", toString(vertex), propertyName, value);
        }

        if (!isEncoded) {
            propertyName = encodePropertyKey(propertyName);
        }

        vertex.property(propertyName, value);

        return vertex;
    }

    public static <T extends Element> void setProperty(T element, String propertyName, Object value) {
        setProperty(element, propertyName, value, false);
    }

    public static <T extends Element> void setEncodedProperty(T element, String propertyName, Object value) {
        setProperty(element, propertyName, value, true);
    }

    public static <T extends Element> void setProperty(T element, String propertyName, Object value, boolean isEncoded) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> setProperty({}, {}, {})", toString(element), propertyName, value);
        }

        if (!isEncoded) {
            propertyName = encodePropertyKey(propertyName);
        }

        Object existingValue = element.property(propertyName, Object.class);

        if (value == null) {
            if (existingValue != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing property {} from {}", propertyName, toString(element));
                }
                element.property(propertyName).remove();
            }
        } else {
            if (!value.equals(existingValue)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Setting property {} in {}", propertyName, toString(element));
                }

                if ( value instanceof Date) {
                    Long encodedValue = ((Date) value).getTime();
                    element.property(propertyName, encodedValue);
                } else {
                    element.property(propertyName, value);
                }
            }
        }
    }

    public static <T extends Element, O> O getProperty(T element, String propertyName, Class<O> returnType) {
        return getProperty(element, propertyName, returnType, false);
    }

    public static <T extends Element, O> O getEncodedProperty(T element, String propertyName, Class<O> returnType) {
        return getProperty(element, propertyName, returnType, true);
    }

    public static <T extends Element, O> O getProperty(T element, String propertyName, Class<O> returnType, boolean isEncoded) {
        if (!isEncoded) {
            propertyName = encodePropertyKey(propertyName);
        }

        Object property = element.property(propertyName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getProperty({}, {}) ==> {}", toString(element), propertyName, returnType.cast(property));
        }

        return returnType.cast(property);
    }

    public static Vertex getVertexByUniqueAttributes(Graph graph, EntityType entityType, Map<String, Object> attrValues) throws BaseException {
        Vertex vertex = findByUniqueAttributes(graph, entityType, attrValues);

        if (vertex == null) {
            throw new BaseException(ErrorCode.INSTANCE_BY_UNIQUE_ATTRIBUTE_NOT_FOUND, entityType.getTypeName(),
                                         attrValues.toString());
        }

        return vertex;
    }


    public static String getGuidByUniqueAttributes(Graph graph, EntityType entityType, Map<String, Object> attrValues) throws BaseException {
        Vertex vertexByUniqueAttributes = getVertexByUniqueAttributes(graph, entityType, attrValues);
        return getIdFromVertex(vertexByUniqueAttributes);
    }


    public static Vertex findByUniqueAttributes(Graph graph, EntityType entityType, Map<String, Object> attrValues) {
//        MetricRecorder metric = RequestContext.get().startMetricRecord("findByUniqueAttributes");
        Vertex vertex = null;
        final Map<String, Attribute> uniqueAttributes = entityType.getUniqAttributes();

        if (MapUtils.isNotEmpty(uniqueAttributes) && MapUtils.isNotEmpty(attrValues)) {
            Map<String, Object> uniqAttrValues = populateUniqueAttributesMap(uniqueAttributes, attrValues);
            Map<String, Object> attrNameValues = populateAttributesMap(uniqueAttributes, attrValues);
            String typeName = entityType.getTypeName();
            Set<String> entitySubTypes = entityType.getAllSubTypes();

            if (USE_UNIQUE_INDEX_PROPERTY_TO_FIND_ENTITY && MapUtils.isNotEmpty(uniqAttrValues)) {
                vertex = findByTypeAndUniquePropertyName(graph, typeName, uniqAttrValues);

                // if no instance of given typeName is found, try to find an instance of type's sub-type
                if (vertex == null && !entitySubTypes.isEmpty()) {
                    vertex = findBySuperTypeAndUniquePropertyName(graph, typeName, uniqAttrValues);
                }
            } else {
                vertex = findByTypeAndPropertyName(graph, typeName, attrNameValues);

                // if no instance of given typeName is found, try to find an instance of type's sub-type
                if (vertex == null && !entitySubTypes.isEmpty()) {
                    vertex = findBySuperTypeAndPropertyName(graph, typeName, attrNameValues);
                }
            }
        }

//        RequestContext.get().endMetricRecord(metric);

        return vertex;
    }

    public static String findFirstDeletedDuringSpooledByQualifiedName(Graph graph, String qualifiedName, long timestamp) {
//        MetricRecorder metric = RequestContext.get().startMetricRecord("findDeletedDuringSpooledByQualifiedName");

        Iterator<Vertex> iterator = graph.traversal().V().has(STATE_PROPERTY_KEY, Status.DELETED.name())
                                .has(Constants.ENTITY_DELETED_TIMESTAMP_PROPERTY_KEY, P.gte(timestamp))
                                .has(Constants.QUALIFIED_NAME, qualifiedName)
                                .order().by(Constants.ENTITY_DELETED_TIMESTAMP_PROPERTY_KEY).by(Order.asc).toList().iterator();

        String ret = iterator.hasNext() ? (String)iterator.next().property("__guid").value() : null;

//        RequestContext.get().endMetricRecord(metric);

        return ret;
    }

    public static Vertex findByGuid(Graph graph, String guid) {
//        PerfMetrics.MetricRecorder metric = RequestContext.get().startMetricRecord("findByGuid");

        Vertex ret = GraphTransactionInterceptor.getVertexFromCache(guid);

        if (ret == null) {
            Iterator<Vertex> results = graph.traversal().V()
                    .has(Constants.GUID_PROPERTY_KEY, guid)
                    .toList().iterator();

            ret = results.hasNext() ? results.next() : null;

            if (ret != null) {
                GraphTransactionInterceptor.addToVertexCache(guid, ret);
            }
        }

//        RequestContext.get().endMetricRecord(metric);
        return ret;
    }

    public static Vertex findDeletedByGuid(Graph graph, String guid) {
        Vertex ret = GraphTransactionInterceptor.getVertexFromCache(guid);

        if (ret == null) {
            Iterator<Vertex> results = graph.traversal().V()
                    .has(Constants.GUID_PROPERTY_KEY, guid)
                    .has(STATE_PROPERTY_KEY, Status.DELETED.name()).toList().iterator();

            ret = results.hasNext() ? results.next() : null;

            if (ret != null) {
                GraphTransactionInterceptor.addToVertexCache(guid, ret);
            }
        }

        return ret;
    }

    public static String getTypeNameFromGuid(Graph graph, String guid) {
        String ret = null;

        if (StringUtils.isNotEmpty(guid)) {
            Vertex vertex = findByGuid(graph, guid);

            ret = (vertex != null) ? getTypeName(vertex) : null;
        }

        return ret;
    }

    public static boolean typeHasInstanceVertex(Graph graph, String typeName) throws BaseException {


        Iterator<Vertex> results = graph.traversal().V()
                .has(Constants.TYPE_NAME_PROPERTY_KEY, P.eq(typeName))
                .toList().iterator();

        boolean hasInstanceVertex =  results.hasNext();

        if (LOG.isDebugEnabled()) {
            LOG.debug("typeName {} has instance vertex {}", typeName, hasInstanceVertex);
        }

        return hasInstanceVertex;
    }

    public static Vertex findByTypeAndUniquePropertyName(Graph graph, String typeName, String propertyName, Object attrVal) {
//        MetricRecorder metric = RequestContext.get().startMetricRecord("findByTypeAndUniquePropertyName");

        graph.traversal().V().has(ENTITY_TYPE_PROPERTY_KEY, typeName)
                             .has(propertyName, attrVal);

        Iterator<Vertex> results = graph.traversal().V().has(ENTITY_TYPE_PROPERTY_KEY, typeName)
                .has(propertyName, attrVal).toList().iterator();

        Vertex vertex = results.hasNext() ? results.next() : null;

//        RequestContext.get().endMetricRecord(metric);

        return vertex;
    }

    private static Map<String, Object> populateUniqueAttributesMap(Map<String, Attribute> uniqueAttributes, Map<String, Object> attrValues) {
        return populateAttributesMap(uniqueAttributes, attrValues, true);
    }

    private static Map<String, Object> populateAttributesMap(Map<String, Attribute> uniqueAttributes, Map<String, Object> attrValues) {
        return populateAttributesMap(uniqueAttributes, attrValues, false);
    }

    private static Map<String, Object> populateAttributesMap(Map<String, Attribute> uniqueAttributes, Map<String, Object> attrValues, boolean isUnique) {
        Map<String, Object> ret = new HashMap<>();

        for (Attribute attribute : uniqueAttributes.values()) {
            String attrName  = isUnique ? attribute.getVertexUniquePropertyName() : attribute.getVertexPropertyName();
            Object attrValue = attrValues.get(attribute.getName());

            if (attrName != null && attrValue != null) {
                ret.put(attrName, attrValue);
            }
        }

        return ret;
    }

    public static Vertex findByTypeAndUniquePropertyName(Graph graph, String typeName, Map<String, Object> attributeValues) {
        return findByTypeAndUniquePropertyName(graph, typeName, attributeValues, false);
    }

    public static Vertex findBySuperTypeAndUniquePropertyName(Graph graph, String typeName, Map<String, Object> attributeValues) {
        return findByTypeAndUniquePropertyName(graph, typeName, attributeValues, true);
    }

    public static Vertex findByTypeAndUniquePropertyName(Graph graph, String typeName, Map<String, Object> attributeValues, boolean isSuperType) {
//        String metricName = isSuperType ? "findBySuperTypeAndUniquePropertyName" : "findByTypeAndUniquePropertyName";
//        MetricRecorder  metric          = RequestContext.get().startMetricRecord(metricName);
        String typePropertyKey = isSuperType ? SUPER_TYPES_PROPERTY_KEY : ENTITY_TYPE_PROPERTY_KEY;
        GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V().has(typePropertyKey, typeName);

        for (Map.Entry<String, Object> entry : attributeValues.entrySet()) {
            String attrName  = entry.getKey();
            Object attrValue = entry.getValue();

            if (attrName != null && attrValue != null) {
                graphTraversal.has(attrName, attrValue);
            }
        }

        Iterator<Vertex> results = graphTraversal.toList().iterator();
        Vertex           vertex  = results.hasNext() ? results.next() : null;
//
//        RequestContext.get().endMetricRecord(metric);

        return vertex;
    }

    public static Vertex findByTypeAndPropertyName(Graph graph, String typeName, Map<String, Object> attributeValues) {
        return findByTypeAndPropertyName(graph, typeName, attributeValues, false);
    }

    public static Vertex findBySuperTypeAndPropertyName(Graph graph, String typeName, Map<String, Object> attributeValues) {
        return findByTypeAndPropertyName(graph, typeName, attributeValues, true);
    }

    public static Vertex findByTypeAndPropertyName(Graph graph, String typeName, Map<String, Object> attributeValues, boolean isSuperType) {
//        String          metricName      = isSuperType ? "findBySuperTypeAndPropertyName" : "findByTypeAndPropertyName";
//        MetricRecorder  metric          = RequestContext.get().startMetricRecord(metricName);
        String  typePropertyKey = isSuperType ? SUPER_TYPES_PROPERTY_KEY : ENTITY_TYPE_PROPERTY_KEY;
        GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V()
                                               .has(typePropertyKey, typeName)
                                               .has(STATE_PROPERTY_KEY, Entity.Status.ACTIVE.name());

        for (Map.Entry<String, Object> entry : attributeValues.entrySet()) {
            String attrName  = entry.getKey();
            Object attrValue = entry.getValue();

            if (attrName != null && attrValue != null) {
                graphTraversal.has(attrName, attrValue);
            }
        }

        Iterator<Vertex> results = graphTraversal.toList().iterator();
        Vertex vertex = results.hasNext() ? results.next() : null;

//        RequestContext.get().endMetricRecord(metric);

        return vertex;
    }

    public static List<String> findEntityGUIDsByType(Graph graph, String typename, SortOrder sortOrder) {
        GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V().has(ENTITY_TYPE_PROPERTY_KEY, typename);
        if (sortOrder != null) {
            graphTraversal.order().by(Constants.QUALIFIED_NAME).by(sortOrder == SortOrder.ASCENDING ?Order.asc:Order.desc);
        }

        Iterator<Vertex> results = graphTraversal.toList().iterator();
        ArrayList<String> ret = new ArrayList<>();

        if (!results.hasNext()) {
            return Collections.emptyList();
        }

        while (results.hasNext()) {
            ret.add(getIdFromVertex(results.next()));
        }

        return ret;
    }

    public static List<String> findEntityGUIDsByType(Graph graph, String typename) {
        return findEntityGUIDsByType(graph, typename, null);
    }

    public static Iterator<Vertex> findActiveEntityVerticesByType(Graph graph, String typename) {
        GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V()
                                          .has(ENTITY_TYPE_PROPERTY_KEY, typename)
                                          .has(STATE_PROPERTY_KEY, Status.ACTIVE.name());

        return graphTraversal.toList().iterator();
    }

    public static boolean relationshipTypeHasInstanceEdges(Graph graph, String typeName) throws BaseException {
        GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V()
                .has(TYPE_NAME_PROPERTY_KEY, P.eq(typeName));

        Iterator<Edge> results = graphTraversal.bothE().toList().iterator();

        boolean hasInstanceEdges = results.hasNext();

        if (LOG.isDebugEnabled()) {
            LOG.debug("relationshipType {} has instance edges {}", typeName, hasInstanceEdges);
        }

        return hasInstanceEdges;
    }

    private static String toString(Element element) {
        if (element instanceof Vertex) {
            return toString((Vertex) element);
        } else if (element instanceof Edge) {
            return toString((Edge)element);
        }

        return element.toString();
    }

    public static String toString(Vertex vertex) {
        if(vertex == null) {
            return "vertex[null]";
        } else {
            if (LOG.isDebugEnabled()) {
                return getVertexDetails(vertex);
            } else {
                return String.format("vertex[id=%s]", vertex.id().toString());
            }
        }
    }


    public static String toString(Edge edge) {
        if(edge == null) {
            return "edge[null]";
        } else {
            if (LOG.isDebugEnabled()) {
                return getEdgeDetails(edge);
            } else {
                return String.format("edge[id=%s]", edge.id().toString());
            }
        }
    }

    public static String getVertexDetails(Vertex vertex) {
        return String.format("vertex[id=%s type=%s guid=%s]",
                vertex.id().toString(), getTypeName(vertex), getIdFromVertex(vertex));
    }

    public static String getEdgeDetails(Edge edge) {
        return String.format("edge[id=%s label=%s from %s -> to %s]", edge.id(), edge.label(),
                toString(edge.outVertex()), toString(edge.inVertex()));
    }

    public static Entity.Status getState(Element element) {
        String state = getStateAsString(element);
        return state == null ? null : Entity.Status.valueOf(state);
    }

    public static String getStateAsString(Element element) {
        return getProperty(element, STATE_PROPERTY_KEY, String.class);
    }

//    private static IndexQuery getIndexQuery(Graph graph, EntityType entityType, String propertyName, String value) {
//        StringBuilder sb = new StringBuilder();
//
//        sb.append(INDEX_SEARCH_PREFIX + "\"").append(TYPE_NAME_PROPERTY_KEY).append("\":").append(entityType.getTypeAndAllSubTypesQryStr())
//                .append(" AND ")
//                .append(INDEX_SEARCH_PREFIX + "\"").append(propertyName).append("\":").append(Attribute.escapeIndexQueryValue(value))
//                .append(" AND ")
//                .append(INDEX_SEARCH_PREFIX + "\"").append(STATE_PROPERTY_KEY).append("\":ACTIVE");
//
//        return graph.indexQuery(Constants.VERTEX_INDEX, sb.toString());
//    }

    public static String getIndexSearchPrefix() {
        return INDEX_SEARCH_PREFIX;
    }

    public static List<String> getClassificationNames(Vertex entityVertex) {
        return getClassificationNamesHelper(entityVertex, CLASSIFICATION_NAMES_KEY);
    }

    public static List<String> getPropagatedClassificationNames(Vertex entityVertex) {
        return getClassificationNamesHelper(entityVertex, PROPAGATED_CLASSIFICATION_NAMES_KEY);
    }

    private static List<String> getClassificationNamesHelper(Vertex vertex, String propertyKey) {
        List<String> classificationNames = null;
        String classificationNamesString =  getProperty(vertex, propertyKey, String.class);
        if (StringUtils.isNotEmpty(classificationNamesString)) {
            classificationNames = Arrays.asList(StringUtils.split(classificationNamesString, "\\|"));
        }
        return classificationNames;
    }

    public static List<Date> dateParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Date> ret = new ArrayList();
        for (String s : arr) {
            try{
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date date = formatter.parse(s);
                ret.add(date);
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is not of Date type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is not of Date type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List<Boolean> booleanParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Boolean> ret = new ArrayList();
        for (String s : arr) {
            try{
                ret.add(Boolean.parseBoolean(s));
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is not of Boolean type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is not of Boolean type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List<Double> doubleParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Double> ret = new ArrayList();
        for (String s : arr) {
            try{
                ret.add(Double.parseDouble(s));
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is not of Double type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is not of Double type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List<Short> shortParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Short> ret = new ArrayList();
        for (String s : arr) {
            try{
                ret.add(Short.parseShort(s));
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is not of Short type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is not of Short type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List<Long> longParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Long> ret = new ArrayList();
        for (String s : arr) {
            try{
                ret.add(Long.parseLong(s));
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is not of Long type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is not of Long type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List<Integer> intParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Integer> ret = new ArrayList();
        for (String s : arr) {
            try{
                ret.add(Integer.parseInt(s));
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is not of Integer type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is Integer of Long type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List<Float> floatParser(String[] arr, List failedTermMsgList, int lineIndex) {

        List<Float> ret = new ArrayList<>();
        for (String s : arr) {
            try{
                ret.add(Float.parseFloat(s));
            }
            catch(Exception e){
                LOG.error("Provided value "+s+" is Float of Long type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is Float of Long type at line #"+lineIndex);
            }
        }
        return ret;
    }

    public static List assignEnumValues(String bmAttributeValues, EnumType enumType, List<String> failedTermMsgList, int lineIndex) {
        List<String> ret = new ArrayList<>();
        String[] arr = bmAttributeValues.split(FileUtils.ESCAPE_CHARACTER + FileUtils.PIPE_CHARACTER);
        EnumDef.EnumElementDef EnumDef;
        for(String s : arr){
            EnumDef = enumType.getEnumElementDef(s);
            if(EnumDef==null){
                LOG.error("Provided value "+s+" is Enumeration of Long type at line #"+lineIndex);
                failedTermMsgList.add("Provided value "+s+" is Enumeration of Long type at line #"+lineIndex);
            }else{
                ret.add(s);
            }
        }
        return ret;
    }

    public static void addItemToListProperty(Edge edge, String property, String value) {
        List list = getListFromProperty(edge, property);

        list.add(value);

        edge.property(property, list);
    }

    public static void removeItemFromListProperty(Edge edge, String property, String value) {
        List list = getListFromProperty(edge, property);

        list.remove(value);

        if (CollectionUtils.isEmpty(list)) {
            edge.property(property).remove();
        } else {
            edge.property(property, list);
        }
    }

    private static List getListFromProperty(Edge edge, String property) {
        List list = IteratorUtils.toList(edge.properties(property));

        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    private static <E> List<E> getListFromProperty(Edge edge, String property,Class<E> clazz) {
        List<E> list = new ArrayList<>();
        Iterator<Property<E>> it = edge.properties(property);
        while(it.hasNext()) {
            Property<E> e = it.next();
            list.add(e.value());
        }
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    public static <T> T getProperty(Vertex vertex, String property, Class<T> clazz) {
        if (vertex.property(property).isPresent()) {
            Object propertyValue= vertex.property(property).value();
            if (propertyValue == null) {
                return null;
            }
            return (T)propertyValue;
        }
        return null;
    }
}