package com.eptcomputing.neo4j

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import com.jteigen.scalatest.JUnit4Runner

import org.neo4j.api.core._

@RunWith(classOf[JUnit4Runner])
class NeoConvertersSpec extends Spec with ShouldMatchers with NeoConverters {
  describe("NeoConverters") {
    it("should create a new relationship in --> relType --> notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val relType = DynamicRelationshipType.withName("foo")
        start --> relType --> end
        start.getSingleRelationship(relType, Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should create a new relationship in --> \"relName\" --> notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        start --> "foo" --> end
        start.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should create a new relationship in <-- relType <-- notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val relType = DynamicRelationshipType.withName("foo")
        end <-- relType <-- start
        start.getSingleRelationship(relType, Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should create a new relationship in <-- \"relName\" <-- notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        end <-- "foo" <-- start
        start.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(start) should equal(end)
      }
    }

    it("should allow relationships of the same direction to be chained") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val middle = neo.createNode
        val end = neo.createNode
        start --> "foo" --> middle --> "bar" --> end
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
        left --> "foo" --> middle <-- "bar" <-- right
        left.getSingleRelationship(DynamicRelationshipType.withName("foo"), Direction.OUTGOING).
          getOtherNode(left) should equal(middle)
        right.getSingleRelationship(DynamicRelationshipType.withName("bar"), Direction.OUTGOING).
          getOtherNode(right) should equal(middle)
      }
    }

    it("should ignore a relationshipBuilder with no end node") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        start --> "foo"
        start.getRelationships.iterator.hasNext should equal(false)
      }
    }

    it("should read a property in a node in node('property') notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        start.setProperty("foo", "bar")
        start("foo") should equal(Some("bar"))
        start("bar") should equal(None)
      }
    }

    it("should create a property in a node in node('property')=value notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        start("foo") = "bar"
        start.getProperty("foo") should equal("bar")
      }
    }

    it("should read a property in a relationship in rel('property') notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val rel = start.createRelationshipTo(end, DynamicRelationshipType.withName("foo"))
        rel.setProperty("foo", "bar")
        rel("foo") should equal(Some("bar"))
        rel("bar") should equal(None)
      }
    }

    it("should create a property in a relationship in rel('property')=value notation") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val rel = start.createRelationshipTo(end, DynamicRelationshipType.withName("foo"))
        rel("foo") = "bar"
        rel.getProperty("foo") should equal("bar")
      }
    }

    it("should allow writing stop evaluators in a functional style") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val rel = start.createRelationshipTo(end, DynamicRelationshipType.withName("foo"))
        val traverser = start.traverse(Traverser.Order.BREADTH_FIRST, (tp : TraversalPosition) => false, ReturnableEvaluator.ALL_BUT_START_NODE, DynamicRelationshipType.withName("foo"), Direction.OUTGOING)
        traverser.iterator.hasNext should equal(true)
        traverser.iterator.next should equal(end)
      }
    }

    it("should allow writing returnable evaluators in a functional style") {
      NeoServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        val rel = start.createRelationshipTo(end, DynamicRelationshipType.withName("foo"))
        val traverser = start.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, (tp : TraversalPosition) => tp.notStartNode(), DynamicRelationshipType.withName("foo"), Direction.OUTGOING)
        traverser.iterator.hasNext should equal(true)
        traverser.iterator.next should equal(end)
      }
    }
  }
}
