package com.eptcomputing.neo4j

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import com.jteigen.scalatest.JUnit4Runner

import org.neo4j.api.core._

@RunWith(classOf[JUnit4Runner])
class NeoConvertersSpec extends Spec with ShouldMatchers with NeoConverters {
  describe("NeoConverters") {
    it("should create a new relationship in --| relType --> notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val relType = DynamicRelationshipType.withName("foo")
        start --| relType --> end
        start.getSingleRelationship(relType, Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should create a new relationship in --| \"relName\" --> notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        start --| "foo" --> end
        start.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should create a new relationship in <-- relType |-- notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val relType = DynamicRelationshipType.withName("foo")
        end <-- relType |-- start
        start.getSingleRelationship(relType, Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should create a new relationship in <-- \"relName\" |-- notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        end <-- "foo" |-- start
        start.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should allow relationships of the same direction to be chained") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val middle = neo.createNode
        val end = neo.createNode
        start --| "foo" --> middle --| "bar" --> end
        start.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(start) should equal(middle)
        middle.getSingleRelationship(DynamicRelationshipType.withName("bar"), Direction.OUTGOING).
          getOtherNode(middle) should equal(end)
      }
    }

    it("should allow relationships of different directions to be chained") {
      NeoServer.exec { neo =>
        val left = neo.createNode
        val middle = neo.createNode
        val right = neo.createNode
        left --| "foo" --> middle <-- "bar" |-- right
        left.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(left) should equal(middle)
        right.getSingleRelationship(DynamicRelationshipType.withName("bar"), Direction.OUTGOING).
          getOtherNode(right) should equal(middle)
      }
    }

    it("should ignore a relationshipBuilder with no end node") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        start --| "foo"
        start.getRelationships.iterator.hasNext should equal(false)
      }
    }
  }
}
