package io.datavines.metadata.graph.enums;

/**
 * Status of the entity - can be active or deleted.
 * Deleted entities are not removed from store.
 */
public enum Status {
    /**
     * 启用、删除、清除
     */
    ACTIVE, DELETED, PURGED
}