package com.eptcomputing.neo4j.rest

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import org.codehaus.jettison.json.{JSONObject, JSONArray}
import org.neo4j.graphdb._

import com.eptcomputing.neo4j.Neo4jServer

@RunWith(classOf[JUnitRunner])
class JsonConverterSpec extends Spec with ShouldMatchers {

  // Helper for converting JSON to Node
  implicit def jsonObjectToNode(json: JSONObject) = new {
    def toNode[T<:Any](operation: Node => T): T = {
      Neo4jServer.exec { neo =>
        operation(Neo4jJsonConverter.jsonToNeo(json, neo, null))
      }
    }
  }

  // Helper for converting Node to JSON
  def serializedNodeWithProperty(property: Object) = Neo4jServer.exec { neo =>
    val node = neo.createNode
    node.setProperty("foo", property)
    Neo4jJsonConverter.neoToJson(node)
  }


  describe("Neo4jJsonConverter") {

    it("should serialize a string property to JSON") {
      val json = serializedNodeWithProperty("test")
      json.getString("foo") should equal("test")
      json.toString should include("\"foo\":\"test\"")
    }

    it("should unserialize a string property from JSON") {
      new JSONObject().put("foo", "test") toNode { node =>
        node.getProperty("foo") should equal("test")
      }
    }

    it("should serialize an integer property to JSON") {
      val json = serializedNodeWithProperty(new java.lang.Integer(42))
      json.getInt("foo") should equal(42)
      json.toString should include("\"foo\":42")
    }

    it("should unserialize an integer property from JSON") {
      new JSONObject().put("foo", 42) toNode { node =>
        node.getProperty("foo") should equal(42)
      }
    }

    it("should serialize a double property to JSON") {
      val json = serializedNodeWithProperty(new java.lang.Double(4.2))
      json.getDouble("foo") should equal(4.2)
      json.toString should include("\"foo\":4.2")
    }

    it("should unserialize a double property from JSON") {
      new JSONObject().put("foo", 4.2) toNode { node =>
        node.getProperty("foo") should equal(4.2)
      }
    }

    it("should serialize an outgoing relationship to JSON") {
      Neo4jServer.exec { neo =>
        val node1 = neo.createNode
        val node2 = neo.createNode
        node1.createRelationshipTo(node2, DynamicRelationshipType.withName("REL"))
        val json = Neo4jJsonConverter.neoToJson(node1)
        json.getJSONObject("_out").getJSONObject("REL").getInt("_end") should equal(node2.getId)
        json.toString should include("\"_out\":{\"REL\":{\"_end\":%d}}".format(node2.getId))
      }
    }

    it("should serialize an incoming relationship to JSON") {
      Neo4jServer.exec { neo =>
        val node1 = neo.createNode
        val node2 = neo.createNode
        node1.createRelationshipTo(node2, DynamicRelationshipType.withName("REL"))
        val json = Neo4jJsonConverter.neoToJson(node2)
        json.getJSONObject("_in").getJSONObject("REL").getInt("_start") should equal(node1.getId)
        json.toString should include("\"_in\":{\"REL\":{\"_start\":%d}}".format(node1.getId))
      }
    }

    it("should serialize several relationships of the same type to a JSON array") {
      Neo4jServer.exec { neo =>
        val node1 = neo.createNode
        val node2 = neo.createNode
        val node3 = neo.createNode
        node1.createRelationshipTo(node2, DynamicRelationshipType.withName("REL"))
        node1.createRelationshipTo(node3, DynamicRelationshipType.withName("REL"))
        val json = Neo4jJsonConverter.neoToJson(node1)
        val arr = json.getJSONObject("_out").getJSONArray("REL")
        Set(arr.getJSONObject(0).getInt("_end"),
            arr.getJSONObject(1).getInt("_end")) should equal(Set(node2.getId, node3.getId))
      }
    }

    it("should serialize relationship properties to JSON") {
      Neo4jServer.exec { neo =>
        val node1 = neo.createNode
        val node2 = neo.createNode
        val rel = node1.createRelationshipTo(node2, DynamicRelationshipType.withName("REL"))
        rel.setProperty("hello", "world")
        val json = Neo4jJsonConverter.neoToJson(node1).getJSONObject("_out").getJSONObject("REL")
        json.getInt("_end") should equal(node2.getId)
        json.getString("hello") should equal("world")
      }
    }

    it("should unserialize a relationship given as just a plain ID") {
      val id = Neo4jServer.exec { _.createNode.getId }
      new JSONObject().put("_out",
        new JSONObject().put("REL", id)
      ) toNode { node =>
        node.getSingleRelationship(DynamicRelationshipType.withName("REL"),
                                   Direction.OUTGOING).getEndNode.getId should equal(id)
      }
    }

    it("should unserialize multiple relationships given as array of IDs") {
      val (id1, id2) = Neo4jServer.exec { neo => (neo.createNode.getId, neo.createNode.getId) }
      new JSONObject().put("_out",
        new JSONObject().put("REL", new JSONArray().put(id1).put(id2))
      ) toNode { node =>
        val iter = node.getRelationships(DynamicRelationshipType.withName("REL"), Direction.OUTGOING).iterator
        Set(iter.next.getEndNode.getId, iter.next.getEndNode.getId) should equal(Set(id1, id2))
      }
    }

    it("should unserialize a relationship given as hash") {
      val id = Neo4jServer.exec { _.createNode.getId }
      new JSONObject().put("_out",
        new JSONObject().put("REL", new JSONObject().put("_end", id))
      ) toNode { node =>
        node.getSingleRelationship(DynamicRelationshipType.withName("REL"),
                                   Direction.OUTGOING).getEndNode.getId should equal(id)
      }
    }

    it("should unserialize a relationship with properties") {
      val id = Neo4jServer.exec { _.createNode.getId }
      new JSONObject().put("_in",
        new JSONObject().put("REL", new JSONObject().put("_start", id).put("foo", "bar"))
      ) toNode { node =>
        val rel = node.getSingleRelationship(DynamicRelationshipType.withName("REL"), Direction.INCOMING)
        rel.getStartNode.getId should equal(id)
        rel.getProperty("foo") should equal("bar")
      }
    }

    it("should unserialize multiple relationships given as array of hashes") {
      val (id1, id2) = Neo4jServer.exec { neo => (neo.createNode.getId, neo.createNode.getId) }
      new JSONObject().put("_in",
        new JSONObject().put("REL", new JSONArray().put(
          new JSONObject().put("_start", id1)
        ).put(
          new JSONObject().put("_start", id2)
        ))
      ) toNode { node =>
        val iter = node.getRelationships(DynamicRelationshipType.withName("REL"), Direction.INCOMING).iterator
        Set(iter.next.getStartNode.getId, iter.next.getStartNode.getId) should equal(Set(id1, id2))
      }
    }
  }
}
