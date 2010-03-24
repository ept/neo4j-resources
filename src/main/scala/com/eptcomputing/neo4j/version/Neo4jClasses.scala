package com.eptcomputing.neo4j.version

import java.io.Serializable
import org.neo4j.graphdb._

// THIS IS WORK IN PROGRESS
//
// The idea is to support features such as version control or access control via
// additional properties and relationships in the graph, but to hide them from
// application query code by wrapping them transparently and exposing an API
// fully compatible to Neo4j's own API. Fortunately that API is not too complicated,
// so implemeting the interfaces with our own classes is not too troublesome.
//
// At the moment, these classes simply delegate to an underlying 'real' Neo4j object.

private class VersionedNeo(delegate: GraphDatabaseService) extends GraphDatabaseService {
  def beginTx: Transaction = delegate.beginTx

  def createNode: Node = delegate.createNode

  def enableRemoteShell = delegate.enableRemoteShell

  def enableRemoteShell(initialProperties: java.util.Map[String,Serializable]) = delegate.enableRemoteShell(initialProperties)

  def getAllNodes: java.lang.Iterable[Node] = delegate.getAllNodes

  def getNodeById(id: Long): Node = delegate.getNodeById(id)

  def getReferenceNode: Node = delegate.getReferenceNode

  def getRelationshipById(id: Long): Relationship = delegate.getRelationshipById(id)

  def getRelationshipTypes: java.lang.Iterable[RelationshipType] = delegate.getRelationshipTypes

  def shutdown = delegate.shutdown
}


private class NodeImpl(delegate: Node) extends NodeImplBase {
  def getId: Long = delegate.getId

  def getRelationships: java.lang.Iterable[Relationship] = delegate.getRelationships

  def delete = delegate.delete

  def createRelationshipTo(otherNode: Node, relType: RelationshipType): Relationship =
    delegate.createRelationshipTo(otherNode, relType)

  def traverse(traversalOrder: Traverser.Order, types: Array[RelationshipType], dirs: Array[Direction],
               stopEvaluator: StopEvaluator, returnableEvaluator: ReturnableEvaluator) = {
    val interleaved = types.zip(dirs).foldRight(Nil.asInstanceOf[List[java.lang.Object]]) {
      (pair, rest) => pair._1 :: pair._2 :: rest
    }
    delegate.traverse(traversalOrder, stopEvaluator, returnableEvaluator, interleaved : _*)
  }

  def getProperty(key: String): java.lang.Object = delegate.getProperty(key)

  def getPropertyKeys: java.lang.Iterable[String] = delegate.getPropertyKeys

  def getPropertyValues: java.lang.Iterable[java.lang.Object] = delegate.getPropertyValues

  def setProperty(key: String, value: java.lang.Object) = delegate.setProperty(key, value)

  def removeProperty(key: String): java.lang.Object = delegate.removeProperty(key)
}


private class RelationshipImpl(delegate: Relationship) extends RelationshipImplBase {
  def getId: Long = delegate.getId

  def getStartNode: Node = delegate.getStartNode

  def getEndNode: Node = delegate.getEndNode

  def getType: RelationshipType = delegate.getType

  def delete = delegate.delete

  def getProperty(key: String): java.lang.Object = delegate.getProperty(key)

  def getPropertyKeys: java.lang.Iterable[String] = delegate.getPropertyKeys

  def getPropertyValues: java.lang.Iterable[java.lang.Object] = delegate.getPropertyValues

  def setProperty(key: String, value: java.lang.Object) = delegate.setProperty(key, value)

  def removeProperty(key: String): java.lang.Object = delegate.removeProperty(key)
}
