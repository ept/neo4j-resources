package com.eptcomputing.neo4j.rest.test

import javax.ws.rs.Path
import com.eptcomputing.neo4j.rest.SimpleNeoResource

/**
 * Example of a RESTful CRUD (create/read/update/delete) resource which maps to a single
 * Neo4j node. JSON object fields are mapped to node attributes, and relationships are
 * represented explicitly -- see <tt>NeoJsonConverter</tt>.
 */
@Path("/neo_resource")
class ExampleNeoResource extends SimpleNeoResource
