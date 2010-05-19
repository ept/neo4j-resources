package com.eptcomputing.neo4j

import com.sun.jersey.api.client.{ClientResponse, UniformInterface, WebResource}
import javax.ws.rs.core.MediaType
import org.codehaus.jettison.json.JSONObject

/**
 * Extend your class with this trait to fix some oddities in the Jersey client API.
 * - Jersey defines a method WebResource.type for setting the content type.
 *   Unfortunately "type" is a reserved word in Scala, so this converter offers
 *   an alternative "contentType" method instead.
 * - If you want to get a response back from a GET/POST/PUT/DELETE/OPTIONS, you have
 *   to pass an ugly type parameter. With these conversions, you can just use
 *   e.g. getResponse instead of get, and it defaults to a type of ClientResponse, or
 *   e.g. getJSON instead of get, and it defaults to a type of JSONObject.
 */
trait JerseyConverters {

  class WebResourceMethods(resource: WebResource) {
    def contentType(contentType: String) = {
      val typeMethod = classOf[WebResource].getMethod("type", classOf[java.lang.String])
      typeMethod.invoke(resource, contentType).asInstanceOf[UniformInterface]
    }

    def contentType(contentType: MediaType) = {
      val typeMethod = classOf[WebResource].getMethod("type", classOf[MediaType])
      typeMethod.invoke(resource, contentType).asInstanceOf[UniformInterface]
    }
  }

  class UniformInterfaceMethods(uniformInterface: UniformInterface) {
    def getResponse     = uniformInterface.get    (classOf[ClientResponse])
    def deleteResponse  = uniformInterface.delete (classOf[ClientResponse])
    def optionsResponse = uniformInterface.options(classOf[ClientResponse])
    def postResponse(entity: java.lang.Object) = uniformInterface.post(classOf[ClientResponse], entity)
    def putResponse (entity: java.lang.Object) = uniformInterface.put (classOf[ClientResponse], entity)

    def getJSON     = uniformInterface.get    (classOf[JSONObject])
    def deleteJSON  = uniformInterface.delete (classOf[JSONObject])
    def optionsJSON = uniformInterface.options(classOf[JSONObject])
    def postJSON(entity: java.lang.Object) = uniformInterface.post(classOf[JSONObject], entity)
    def putJSON (entity: java.lang.Object) = uniformInterface.put (classOf[JSONObject], entity)
  }


  implicit def addContentTypeMethodToWebResource(resource: WebResource) =
    new WebResourceMethods(resource)

  implicit def addResponseMethodsToUniformInterface(uniformInterface: UniformInterface) =
    new UniformInterfaceMethods(uniformInterface)
}
