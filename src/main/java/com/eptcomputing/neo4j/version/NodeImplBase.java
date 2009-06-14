package com.eptcomputing.neo4j.version;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Traverser;
import org.neo4j.api.core.Traverser.Order;


/**
 * Implementation of the boring/repetitive parts of the Neo4j Node interface,
 * to support alternative implementations like versioned resources.
 * (Done in Java because of Scala's trouble with Java varargs.)
 * Much of this is 'borrowed' from org.neo4j.impl.core.NodeImpl.
 *
 * See the Neo4j Node interface for documentation.
 */
abstract class NodeImplBase extends PrimitiveBase implements Node, Comparable<Node> {

    public abstract long getId();

    public abstract Iterable<Relationship> getRelationships();

    public abstract void delete();

    public abstract Relationship createRelationshipTo(Node otherNode, RelationshipType type);

    protected abstract Traverser traverse(Order traversalOrder, RelationshipType[] types, Direction[] dirs,
                                          StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator);

    public int compareTo(Node n) {
        long ourId = this.getId(), theirId = n.getId();
        if (ourId < theirId) return -1;
        else if (ourId > theirId) return 1;
        else return 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }
        return this.getId() == ((Node) o).getId();
    }

    public int hashCode() {
        return (int) getId();
    }

    public String toString() {
        return "NodeImpl#" + this.getId();
    }

    public Iterable<Relationship> getRelationships(RelationshipType... types) {
        return new RelationshipFilter(getRelationships(), types, Direction.BOTH, this);
    }

    public Iterable<Relationship> getRelationships(Direction dir) {
        return new RelationshipFilter(getRelationships(), null, dir, this);
    }

    public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        return new RelationshipFilter(getRelationships(), new RelationshipType[] {type}, dir, this);
    }

    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        Iterator<Relationship> rels = getRelationships(type, dir).iterator();
        if (!rels.hasNext()) return null;
        Relationship rel = rels.next();
        if (rels.hasNext()) throw new NotFoundException("More than one relationship[" + type +
                                                        ", " + dir + "] found for " + this);
        return rel;
    }

    public boolean hasRelationship() {
        return getRelationships().iterator().hasNext();
    }

    public boolean hasRelationship(RelationshipType... types) {
        return getRelationships(types).iterator().hasNext();
    }

    public boolean hasRelationship(Direction dir) {
        return getRelationships(dir).iterator().hasNext();
    }

    public boolean hasRelationship(RelationshipType type, Direction dir) {
        return getRelationships(type, dir).iterator().hasNext();
    }

    public Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator,
                              ReturnableEvaluator returnableEvaluator,
                              RelationshipType relationshipType, Direction direction) {
        if (direction == null) throw new IllegalArgumentException("Null direction");
        if (relationshipType == null) throw new IllegalArgumentException("Null relationship type");
        return traverse(traversalOrder, new RelationshipType[] { relationshipType },
                        new Direction[] { direction }, stopEvaluator, returnableEvaluator);
    }


    public Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator,
                              ReturnableEvaluator returnableEvaluator,
                              RelationshipType firstRelationshipType, Direction firstDirection,
                              RelationshipType secondRelationshipType, Direction secondDirection) {
        if (firstDirection == null || secondDirection == null) {
            throw new IllegalArgumentException("Null direction, firstDirection=" + firstDirection +
                                               ", secondDirection=" + secondDirection);
        }
        if (firstRelationshipType == null || secondRelationshipType == null) {
            throw new IllegalArgumentException("Null rel type, first=" + firstRelationshipType +
                                               ", second=" + secondRelationshipType);
        }
        return traverse(traversalOrder,
                        new RelationshipType[] { firstRelationshipType, secondRelationshipType },
                        new Direction[] { firstDirection, secondDirection },
                        stopEvaluator, returnableEvaluator);
    }


    public Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator,
                              ReturnableEvaluator returnableEvaluator,
                              Object... relationshipTypesAndDirections) {
        int length = relationshipTypesAndDirections.length;
        if ((length % 2) != 0 || length == 0) {
            throw new IllegalArgumentException("Variable argument should consist of [RelationshipType,Direction] pairs");
        }
        int elements = relationshipTypesAndDirections.length / 2;
        RelationshipType[] types = new RelationshipType[elements];
        Direction[] dirs = new Direction[elements];
        int j = 0;
        for (int i = 0; i < elements; i++) {
            Object relType = relationshipTypesAndDirections[j++];
            if (!(relType instanceof RelationshipType)) {
                throw new IllegalArgumentException("Expected RelationshipType at var args pos " + (j - 1) +
                                                   ", found " + relType);
            }
            types[i] = (RelationshipType) relType;
            Object direction = relationshipTypesAndDirections[j++];
            if (!(direction instanceof Direction)) {
                throw new IllegalArgumentException("Expected Direction at var args pos " + (j - 1) +
                                                   ", found " + direction);
            }
            dirs[i] = (Direction) direction;
        }
        return traverse(traversalOrder, types, dirs, stopEvaluator, returnableEvaluator);
    }


    /**
     * An iterable which returns only those relationships matching the given relationship
     * types and directions.
     */
    private static class RelationshipFilter extends Filter<Relationship> {
        private final Set<String> types;
        private final Direction dir;
        private final Node relativeTo;

        RelationshipFilter(Iterable<Relationship> input, RelationshipType[] types, Direction dir,
                           Node relativeTo) {
            super(input);
            this.dir = dir;
            this.relativeTo = relativeTo;
            if (types == null) {
                this.types = null;
            } else {
                this.types = new HashSet<String>();
                for (RelationshipType type : types) this.types.add(type.name());
            }
        }

        @Override
        boolean condition(Relationship rel) {
            if ((dir == Direction.OUTGOING) && !rel.getStartNode().equals(relativeTo)) return false;
            if ((dir == Direction.INCOMING) && !rel.getEndNode().equals(relativeTo)) return false;
            if ((types != null) && !types.contains(rel.getType().name())) return false;
            return true;
        }
    }
}
