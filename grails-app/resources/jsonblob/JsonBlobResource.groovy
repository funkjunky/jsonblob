package jsonblob

import grails.converters.JSON
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException

import javax.ws.rs.*
import javax.ws.rs.core.Response

@Consumes(['application/json'])
@Produces(['application/json'])
class JsonBlobResource {

    def jsonBlobResourceService
    def jsonService
    def id
    
    @GET
    Response read() {
        def blob = jsonBlobResourceService.read(id)
        Response.ok(jsonService.writeValueAsString(blob?.blob)).build()
    }

    @PUT
    Response update(String json) {
        try {
            JSON.parse(json)
            def updatedBlob = jsonBlobResourceService.update(id, json)
            Response.ok(jsonService.writeValueAsString(updatedBlob?.blob)).build()
        } catch (ConverterException ce) {
            Response.serverError().build()
        }
    }

    @DELETE
    void delete(@QueryParam("apiKey") String apiKey) {
        Subject currentUser = SecurityUtils.getSubject();
        boolean canDelete = false
        if (apiKey) {
            canDelete = User.findByApiKey(apiKey) != null
        }

        if (!canDelete && currentUser?.isAuthenticated()) {
            def user = User.findByEmail(currentUser.getPrincipal() as String)
            canDelete = user?.blobIds.contains(id)
        }

        if (canDelete) {
            jsonBlobResourceService.delete(id)
        }
    }
    
}

