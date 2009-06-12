package com.eptcomputing.neo4j.rest

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import com.jteigen.scalatest.JUnit4Runner

import org.codehaus.jettison.json.{JSONObject, JSONArray}
import org.neo4j.api.core._

import com.eptcomputing.neo4j.NeoServer

@RunWith(classOf[JUnit4Runner])
class JsonConverterSpec extends Spec with ShouldMatchers {

  // Helper for converting JSON to Node
  implicit def jsonObjectToNode(json: JSONObject) = new {
    def toNode[T<:Any](operation: Node => T): T = {
      NeoServer.exec { neo =>
        operation(NeoJsonConverter.jsonToNeo(json, neo, null))
      }
    }
  }

  // Helper for converting Node to JSON
  def serializedNodeWithProperty(property: Object) = NeoServer.exec { neo =>
    val node = neo.createNode
    node.setProperty("foo", property)
    NeoJsonConverter.neoToJson(node)
  }


  describe("NeoJsonConverter") {

    it("should serialize a string property to JSON") {
      val json = serializedNodeWithProperty("test")
      json.getString("foo") should equal("test")
      json.toString should include("\"foo\":\"test\"")
    }

    it("should serialize an integer property to JSON") {
      val json = serializedNodeWithProperty(new java.lang.Integer(42))
      json.getInt("foo") should equal(42)
      json.toString should include("\"foo\":42")
    }

    it("should unserialize a string property from JSON") {
      new JSONObject().put("foo", "test") toNode { node =>
        node.getProperty("foo") should equal("test")
      }
    }

    it("should unserialize an integer property from JSON") {
      new JSONObject().put("foo", 42) toNode { node =>
        node.getProperty("foo") should equal(42)
      }
    }

    // TODO lots more tests needed!
  }
}
