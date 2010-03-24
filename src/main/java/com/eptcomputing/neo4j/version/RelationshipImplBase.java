package com.eptcomputing.neo4j.version;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;


/**
 * Implementation of the boring/repetitive parts of the Neo4j Relationship interface,
 * to support alternative implementations like versioned resources.
 *
 * See the Neo4j Relationship interface for documentation.
 */
abstract class RelationshipImplBase extends PrimitiveBase implements Relationship, Comparable<Relationship> {

    public abstract long getId();

    public abstract Node getStartNode();

    public abstract Node getEndNode();

    public abstract RelationshipType getType();

    public abstract void delete();

    public Node[] getNodes() {
        return new Node[] { getStartNode(), getEndNode() };
    }

    public Node getOtherNode(Node node) {
        if (getStartNode().equals(node)) return getEndNode();
        else if (getEndNode().equals(node)) return getStartNode();
        throw new RuntimeException("Node[" + node.getId() + "] not connected to this relationship[" + getId() + "]");
    }

    public boolean isType(RelationshipType otherType) {
        return (otherType != null) && otherType.name().equals(getType().name());
    }

    public String toString() {
        return "Relationship #" + this.getId() + " of type " + getType().name() +
            " between Node[" + getStartNode().getId() + "] and Node[" + getEndNode().getId() + "]";
    }

    public int compareTo(Relationship r) {
        int ourId = (int) this.getId(), theirId = (int) r.getId();
        if (ourId < theirId) return -1;
        else if (ourId > theirId) return 1;
        else return 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Relationship)) return false;
        return this.getId() == ((Relationship) o).getId();
    }

    public int hashCode() {
        return (int) getId();
    }
}
