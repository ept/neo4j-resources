
package com.eptcomputing.neo4j.version;

import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;

abstract class PrimitiveBase implements PropertyContainer {

    public abstract Object getProperty(String key);

    public abstract Iterable<String> getPropertyKeys();

    public abstract Iterable<Object> getPropertyValues();

    public abstract void setProperty(String key, Object value);

    public abstract Object removeProperty(String key);

    public Object getProperty(String key, Object defaultValue) {
        try {
            return getProperty(key);
        } catch (NotFoundException e) {
            return defaultValue;
        }
    }

    public boolean hasProperty(String key) {
        try {
            getProperty(key);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}
