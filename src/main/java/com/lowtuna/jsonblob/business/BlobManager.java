package com.lowtuna.jsonblob.business;

import com.mongodb.*;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.util.Date;

@Slf4j
public class BlobManager {
    private static final String COLLECTION_NAME = "blob";

    private final DBCollection collection;

    public BlobManager(DB mongoDb) {
        this.collection = mongoDb.getCollection(COLLECTION_NAME);
    }

    private BasicDBObject getDBObject(ObjectId objectId) {
        return new BasicDBObject("_id", objectId);
    }

    private DBObject createDBObject(String json) {
        return BasicDBObjectBuilder.start("updated", new Date()).append("blob", JSON.parse(json)).get();
    }

    public boolean isValidJson(String json) {
        try {
            JSON.parse(json);
            return true;
        } catch (JSONParseException e) {
            return false;
        }
    }

    public DBObject create(String json) {
        log.debug("inserting blob with json='{}'", json);
        DBObject parsed = createDBObject(json);
        WriteResult result = collection.insert(parsed);
        log.debug("successfully inserted blob of json as objectId='{}'", result.getField("_id"));
        return parsed;
    }

    public DBObject read(ObjectId id) throws BlobNotFoundException {
        log.debug("attempting to retrieve blob with id='{}'", id);
        DBObject objectId = getDBObject(id);
        if (objectId != null) {
            log.debug("finding blob with objectId='{}'", objectId);
            DBObject obj = collection.findOne(objectId);
            if (obj != null) {
                return obj;
            }
        }
        log.debug("couldn't retrieve blob with id='{}'", id);
        throw new BlobNotFoundException(id);
    }

    public DBObject update(ObjectId id, String json) throws BlobNotFoundException {
        log.debug("attempting to update blob with id='{}' and json='{}'", id, json);
        DBObject objectId = getDBObject(id);
        if (objectId != null) {
            log.debug("finding blob to update with objectId='{}'", objectId);
            DBObject obj = collection.findOne(objectId);
            if (obj != null) {
                DBObject parsed = createDBObject(json);
                collection.update(obj, parsed);
                log.debug("successfully updated blob of json with objectId='{}'", id);
                return parsed;
            }
        }
        log.debug("couldn't update blob with id='{}'", id);
        throw new BlobNotFoundException(id);
    }

    public boolean delete(ObjectId id) throws BlobNotFoundException {
        log.debug("attempting to delete blob with id='{}'", id);
        DBObject objectId = getDBObject(id);
        if (objectId != null) {
            log.debug("finding blob to delete with objectId='{}'", objectId);
            DBObject obj = collection.findOne(objectId);
            if (obj != null) {
                WriteResult result = collection.remove(obj);
                boolean removed = result.getN() > 0 && result.getLastError().ok();
                if (removed) {
                    log.debug("successfully removed {} blob(s) of json with objectId='{}'", result.getN(), id);
                } else {
                    log.debug("did not remove any blob(s) of json with objectId='{}'", id);
                }
                return removed;
            }
        }
        log.debug("couldn't remove blob with id='{}'", id);
        throw new BlobNotFoundException(id);
    }
}
