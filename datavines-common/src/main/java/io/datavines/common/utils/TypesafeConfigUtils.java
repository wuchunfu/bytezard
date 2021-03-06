package io.datavines.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.common.config.Config;

public class TypesafeConfigUtils {

    private static final Logger logger = LoggerFactory.getLogger(TypesafeConfigUtils.class);
    /**
     * Extract sub config with fixed prefix
     *
     * @param source config source
     * @param prefix config prefix
     * @param keepPrefix true if keep prefix
     */
    public static Config extractSubConfig(Config source, String prefix, boolean keepPrefix) {

        // use LinkedHashMap to keep insertion order
        Map<String, Object> values = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            final String key = entry.getKey();
            final String value = String.valueOf(entry.getValue());

            if (key.startsWith(prefix)) {

                if (keepPrefix) {
                    values.put(key, value);
                } else {
                    values.put(key.substring(prefix.length()), value);
                }
            }
        }

        return new Config(values);
    }

    /**
     * Check if config with specific prefix exists
     * @param source config source
     * @param prefix config prefix
     * @return true if it has sub config
     */
    public static boolean hasSubConfig(Config source, String prefix) {

        boolean hasConfig = false;

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            final String key = entry.getKey();

            if (key.startsWith(prefix)) {
                hasConfig = true;
                break;
            }
        }

        return hasConfig;
    }

    public static Config extractSubConfigThrowable(Config source, String prefix, boolean keepPrefix) {

        Config config = extractSubConfig(source, prefix, keepPrefix);

        if (config.isEmpty()) {
            logger.error("config is empty");
        }

        return config;
    }
}
