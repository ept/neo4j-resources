package com.eptcomputing.neo4j.rest

import scala.collection.JavaConversions._
import java.util.logging.Logger
import javax.ws.rs._
import javax.ws.rs.core._

import org.codehaus.jettison.json.JSONObject
import org.neo4j.graphdb.{GraphDatabaseService, Node}

import com.eptcomputing.neo4j.Neo4jServer
import Neo4jJsonConverter._

/**
 * A template for a CRUD (Create/Read/Update/Delete) RESTful JSON resource. You can
 * override each of the operations with a concise method to perform the desired task.
 * See <tt>SimpleNeoResource</tt> for a default implementation.
 */
abstract class Neo4jResource extends RequiredParam {

  /**
   * Override this method to perform the creation of a Neo4j node based on a given
   * JSON object. Should return the newly created node. Is called within a Neo4j
   * transaction.
   */
  def create(neo: GraphDatabaseService, json: JSONObject): Node

  /**
   * Override this method to perform mapping from a Neo4j node to a JSON object
   * for output. Should return the desired JSON serialisation, or an object which
   * Jersey is able to serialise to JSON automatically.
   */
  def read(neo: GraphDatabaseService, node: Node): Any

  /**
   * Override this method to perform the overwriting of a Neo4j node with new data.
   */
  def update(neo: GraphDatabaseService, existing: Node, newValue: JSONObject)

  /**
   * Override this method to perform the deletion of a Neo4j node. Should return
   * a JSON serialisation of the node in its most recent version before it was
   * deleted (or an object which Jersey is able to serialise to JSON automatically).
   */
  def delete(neo: GraphDatabaseService, node: Node): Any

  /**
   * <tt>POST /neo_resource</tt> with a JSON document as body creates a new entity
   * from that document, and returns a HTTP 201 "Created" response with a Location
   * header indicating the URL of the newly created entity.
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createJSON(json: JSONObject) = {
    Neo4jServer.exec { neo =>
      val node = create(neo, json)
      val uri = UriBuilder.fromResource(this.getClass).path("{id}").build(new java.lang.Long(node.getId))
      Response.created(uri).entity(read(neo, node)).build
    }
  }

  /**
   * <tt>GET /neo_resource/&lt;id&gt;</tt> returns a JSON representation of the entity
   * with the given ID.
   */
  @GET @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def readJSON(@PathParam("id") node: NeoNodeParam) = {
    requiredParam("id", node)
    Neo4jServer.exec { neo => read(neo, node.getNode(neo)) }
  }

  /**
   * <tt>PUT /neo_resource/&lt;id&gt;</tt> with a JSON document as body replaces an
   * existing entity with the contents of that document. Returns the same as you would
   * get from a subsequent <tt>GET</tt> of the same URL.
   */
  @PUT @Path("/{id}")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateJSON(@PathParam("id") node: NeoNodeParam, json: JSONObject) = {
    requiredParam("id", node)
    Neo4jServer.exec { neo =>
      val neoNode = node.getNode(neo)
      update(neo, neoNode, json)
      read(neo, neoNode)
    }
  }

  /**
   * <tt>DELETE /neo_resource/&lt;id&gt;</tt> deletes the entity with the given ID.
   * Returns a JSON representation of the entity that was deleted.
   */
  @DELETE @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteJSON(@PathParam("id") node: NeoNodeParam) = {
    requiredParam("id", node)
    Neo4jServer.exec { neo => delete(neo, node.getNode(neo)) }
  }
}


/**
 * Simple default implementation of a CRUD resource which maps to a single Neo4j node.
 * See <tt>Neo4jJsonConverter</tt> for the format used.
 */
class SimpleNeo4jResource extends Neo4jResource {

  def create(neo: GraphDatabaseService, json: JSONObject) =
    jsonToNeo(json, neo, null)

  def read(neo: GraphDatabaseService, node: Node) =
    neoToJson(node)

  def update(neo: GraphDatabaseService, existing: Node, newValue: JSONObject) =
    jsonToNeo(newValue, neo, existing)

  def delete(neo: GraphDatabaseService, node: Node) = {
    val json = neoToJson(node)
    node.getRelationships.foreach{_.delete}
    node.delete
    json
  }
}
