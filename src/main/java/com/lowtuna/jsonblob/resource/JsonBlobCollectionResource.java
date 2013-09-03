package com.lowtuna.jsonblob.resource;

import com.lowtuna.jsonblob.business.BlobManager;
import com.mongodb.DBObject;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class JsonBlobCollectionResource {
    private final BlobManager blobManager;

    public JsonBlobCollectionResource(BlobManager blobManager) {
        this.blobManager = blobManager;
    }

    @POST
    @Path("/jsonBlob")
    public Response create(String json) {
        if (!blobManager.isValidJson(json)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        DBObject newBlob = blobManager.create(json);
        ObjectId id = (ObjectId) newBlob.get("_id");
        if (id == null) {
            return Response.serverError().build();
        }

        return Response.created(UriBuilder.fromResource(JsonBlobResource.class).build(id)).entity(newBlob.get("blob")).build();
    }

    @Path("/jsonBlob/{blobId}")
    public JsonBlobResource getJsonBlobResource(@PathParam("blobId") ObjectId blobId) {
        return createJsonBlobResource(blobId);
    }

    @Path("/{path: .*}")
    public JsonBlobResource getJsonBlobResource(@PathParam("path") String path, @HeaderParam("X-jsonblob") String jsonBlobId) {
        ObjectId blobId = null;
        try {
            blobId = new ObjectId(jsonBlobId);
        } catch (IllegalArgumentException e) {
            for (String part : path.split("/")) {
                try {
                    blobId = new ObjectId(part);
                } catch (IllegalArgumentException iae) {
                    //try the next part or fall out of the loop
                }
            }
        }

        if (blobId == null) {
            throw new NotFoundException();
        }

        return createJsonBlobResource(blobId);
    }

    private JsonBlobResource createJsonBlobResource(ObjectId id) {
        return new JsonBlobResource(id, blobManager);
    }
}
