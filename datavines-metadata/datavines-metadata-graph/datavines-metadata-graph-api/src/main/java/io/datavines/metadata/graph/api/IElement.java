package io.datavines.metadata.graph.api;

import java.util.Collection;
import java.util.List;

/**
 * @author zixi0825
 */
public interface IElement {

    /**
     * Gets the id of this element.  If the object has not been physically created in the underlying graph,
     * calling this method will force the element to be created so that a valid Id can be returned.
     *
     * @return
     */
    Object getId();

    /**
     * Gets the names of the properties associated with this element.
     * @return
     */
    Collection<? extends String> getPropertyKeys();

    /**
     * Gets the value of the element property with the given name.  The value
     * returned is guaranteed to be an instance of the specified class (or
     * an exception will be thrown).
     *
     * @param propertyName
     * @return
     * @throws IllegalStateException if the property is multi-valued in the graph schema.
     */
    <T> T getProperty(String propertyName, Class<T> clazz);

    /**
     * Gets all of the values of the given property.
     * @param propertyName
     * @return
     */
    <T> Collection<T> getPropertyValues(String propertyName, Class<T> type);

    /**
     * Gets the value of a multiplicity one property whose value is a String list.
     * The lists of super types and traits are stored this way.  A separate method
     * is needed for this because special logic is required to handle this situation
     * in some implementations.
     */
    List<String> getListProperty(String propertyName);

    /**
     * Gets the value of a multiplicity one property whose value is a list.  It
     * attempts to convert the elements in the list to the specified type.  Currently
     * conversion is only supported for subclasses of AtlasElement and String.
     */
    <V> List<V> getListProperty(String propertyName, Class<V> elementType);

    /**
     * Sets a multiplicity one property whose value is a String list.
     * The lists of super types and traits are stored this way.  A separate method
     * is needed for this because special logic is required to handle this situation
     * in some implementations.
     */
    void setListProperty(String propertyName, List<String> values);


    /**
     * Sets a multiplicity one property whose effective value is a String list whose
     * values consist of the ids of the supplied elements.  This is implemented efficiently
     * so that in many cases the property can be set without requiring new elements
     * to be immediately created in the graph database.  It allows the actual underlying element
     * creation to be deferred until commit time.
     */
    void setPropertyFromElementsIds(String propertyName, List<IElement> values);

    /**
     * Sets a multiplicity one property whose effective value is a String whose value is the id  of the supplied
     * element.  This is implemented efficiently so that in many cases the property can be set without requiring
     * new elements to be immediately created in the graph database.  It allows the actual underlying element
     * creation to be deferred until commit time.
     */
    void setPropertyFromElementId(String propertyName, IElement value);


    /**
     * Removes a property from the vertex.
     */
    void removeProperty(String propertyName);

    /**
     * Removes a property with given property value from the vertex.
     */
    void removePropertyValue(String propertyName, Object propertyValue);

    /**
     * Removes a property with given property value from the vertex.
     */
    void removeAllPropertyValue(String propertyName, Object propertyValue);

    /**
     * Sets a single-valued property to the given value.  For
     * properties defined as multiplicty many in the graph schema, the value is added instead
     * (following set semantics)
     *
     * @param propertyName
     * @param value
     */
    <T> void setProperty(String propertyName, T value);

    /**
     * @param propertyName
     * @param value
     */
    <T> void setJsonProperty(String propertyName, T value);

    /**
     * @param propertyName
     * @return
     */
    <T> T getJsonProperty(String propertyName);

    /**
     * Gets a human-readable id without forcing the element to
     * be created if it does not exist in the graph yet.
     *
     * @return
     */
    String getIdForDisplay();

    /**
     * Whether or not an id has been assigned yet for this Element.  This can happen if the element has been created
     * in memory but has not been actually pushed into the underlying graph yet.
     *
     * @return
     */
    boolean isIdAssigned();

    <T> T getOriginalElement();
}
