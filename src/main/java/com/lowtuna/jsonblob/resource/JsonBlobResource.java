package com.lowtuna.jsonblob.resource;

import com.lowtuna.jsonblob.business.BlobManager;
import com.lowtuna.jsonblob.business.BlobNotFoundException;
import com.mongodb.DBObject;
import com.sun.jersey.api.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/{blobId}")
@Slf4j
public class JsonBlobResource {
    private final ObjectId blobId;
    private final BlobManager blobManager;

    public JsonBlobResource(ObjectId blobId, BlobManager blobManager) {
        this.blobId = blobId;
        this.blobManager = blobManager;
    }

    @GET
    public DBObject read() {
        try {
            DBObject object = blobManager.read(blobId);
            return (DBObject) object.get("blob");
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    public DBObject update(String json) {
        if (!blobManager.isValidJson(json)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            DBObject object = blobManager.update(blobId, json);
            return (DBObject) object.get("blob");
        } catch (BlobNotFoundException e) {
            throw new NotFoundException();
        }
    }

//    @DELETE
//    public void delete() {
//        try {
//            blobManager.delete(blobId);
//        } catch (BlobNotFoundException e) {
//            throw new NotFoundException();
//        }
//    }
}
