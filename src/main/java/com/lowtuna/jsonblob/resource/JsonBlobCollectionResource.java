package com.lowtuna.jsonblob.resource;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.lowtuna.jsonblob.business.BlobManager;
import com.lowtuna.jsonblob.business.DemoBlobHelper;
import com.lowtuna.jsonblob.util.view.MapBackedView;
import com.mongodb.DBObject;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class JsonBlobCollectionResource {
    private final BlobManager blobManager;
    private final DemoBlobHelper demoBlobHelper;

    public JsonBlobCollectionResource(BlobManager blobManager, DemoBlobHelper demoBlobHelper) {
        this.blobManager = blobManager;
        this.demoBlobHelper = demoBlobHelper;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.TEXT_HTML)
    public MapBackedView apiInfo() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("demoObjectId", demoBlobHelper.getDemoObjectId().toString());

        return new MapBackedView("/views/api.mustache", Charsets.UTF_8, variables);
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
