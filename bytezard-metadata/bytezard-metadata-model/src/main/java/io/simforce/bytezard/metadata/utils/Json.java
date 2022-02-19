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
package io.simforce.bytezard.metadata.utils;

import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.simforce.bytezard.metadata.model.instance.Struct;
import io.simforce.bytezard.metadata.model.typedef.BaseTypeDef;

public class Json {
    private static final Logger LOG = LoggerFactory.getLogger(Json.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
                                            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
    static {

    }

    public static String toJson(Object obj) {
        String ret;
        try {
            if (obj instanceof JsonNode && ((JsonNode) obj).isTextual()) {
                ret = ((JsonNode) obj).textValue();
            } else {
                ret = MAPPER.writeValueAsString(obj);
            }
        }catch (IOException e){
            LOG.error("Json.toJson()", e);

            ret = null;
        }
        return ret;
    }

    public static <T> T fromLinkedHashMap(Object linkedHashMap, Class<T> type) {
        T ret = null;

        if (linkedHashMap != null) {
            ret = MAPPER.convertValue(linkedHashMap, type);
        }

        return ret;
    }

    public static <T> T fromJson(String jsonStr, Class<T> type) {
        T ret = null;

        if (jsonStr != null) {
            try {
                ret = MAPPER.readValue(jsonStr, type);
            } catch (IOException e) {
                LOG.error("Type.fromJson()", e);

                ret = null;
            }
        }

        return ret;
    }

    public static <T> T fromJson(String jsonStr, TypeReference<T> type) {
        T ret = null;

        if (jsonStr != null) {
            try {
                ret = MAPPER.readValue(jsonStr, type);
            } catch (IOException e) {
                LOG.error("Type.fromJson()", e);

                ret = null;
            }
        }

        return ret;
    }

    public static <T> T fromJson(InputStream inputStream, Class<T> type) throws IOException {
        T ret = null;

        if (inputStream != null) {
            ret = MAPPER.readValue(inputStream, type);
        }

        return ret;
    }


    public static ObjectCodec getMapper() {
        return MAPPER;
    }

    static class DateSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (value != null) {
                jgen.writeString(BaseTypeDef.getDateFormatter().format(value));
            }
        }
    }

    static class DateDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            Date ret = null;

            String value = parser.readValueAs(String.class);

            if (value != null) {
                try {
                    ret = BaseTypeDef.getDateFormatter().parse(value);
                } catch (ParseException excp) {
                }
            }

            return ret;
        }
    }

//    static class HookNotificationDeserializer extends JsonDeserializer<HookNotification> {
//        @Override
//        public HookNotification deserialize(JsonParser parser, DeserializationContext context) throws IOException {
//            HookNotification     ret              = null;
//            ObjectCodec          mapper           = parser.getCodec();
//            TreeNode             root             = mapper.readTree(parser);
//            JsonNode typeNode         = root != null ? (JsonNode) root.get("type") : null;
//            String               strType          = typeNode != null ? typeNode.asText() : null;
//            HookNotificationType notificationType = strType != null ? HookNotificationType.valueOf(strType) : null;
//
//            if (notificationType != null) {
//                switch (notificationType) {
//                    case TYPE_CREATE:
//                    case TYPE_UPDATE:
//                        ret = mapper.treeToValue(root, TypeRequest.class);
//                        break;
//
//                    case ENTITY_CREATE:
//                        ret = mapper.treeToValue(root, EntityCreateRequest.class);
//                        break;
//
//                    case ENTITY_PARTIAL_UPDATE:
//                        ret = mapper.treeToValue(root, EntityPartialUpdateRequest.class);
//                        break;
//
//                    case ENTITY_FULL_UPDATE:
//                        ret = mapper.treeToValue(root, EntityUpdateRequest.class);
//                        break;
//
//                    case ENTITY_DELETE:
//                        ret = mapper.treeToValue(root, EntityDeleteRequest.class);
//                        break;
//
//                    case ENTITY_CREATE_V2:
//                        ret = mapper.treeToValue(root, EntityCreateRequestV2.class);
//                        break;
//
//                    case ENTITY_PARTIAL_UPDATE_V2:
//                        ret = mapper.treeToValue(root, EntityPartialUpdateRequestV2.class);
//                        break;
//
//                    case ENTITY_FULL_UPDATE_V2:
//                        ret = mapper.treeToValue(root, EntityUpdateRequestV2.class);
//                        break;
//
//                    case ENTITY_DELETE_V2:
//                        ret = mapper.treeToValue(root, EntityDeleteRequestV2.class);
//                        break;
//                }
//            }
//
//            return ret;
//        }
//    }
//
//    static class EntityNotificationDeserializer extends JsonDeserializer<EntityNotification> {
//        @Override
//        public EntityNotification deserialize(JsonParser parser, DeserializationContext context) throws IOException {
//            EntityNotification     ret              = null;
//            ObjectCodec            mapper           = parser.getCodec();
//            TreeNode               root             = mapper.readTree(parser);
//            JsonNode typeNode         = root != null ? (JsonNode) root.get("type") : null;
//            String                 strType          = typeNode != null ? typeNode.asText() : null;
//            EntityNotificationType notificationType = strType != null ? EntityNotificationType.valueOf(strType) : EntityNotificationType.ENTITY_NOTIFICATION_V1;
//
//            if (root != null) {
//                switch (notificationType) {
//                    case ENTITY_NOTIFICATION_V2:
//                        ret = mapper.treeToValue(root, EntityNotificationV2.class);
//                        break;
//                }
//            }
//
//            return ret;
//        }
//    }

}
