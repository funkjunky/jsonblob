package com.lowtuna.jsonblob.business;

import java.util.Date;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

@Slf4j
public class BlobManager {

    private final DBCollection collection;
    private final Timer createTimer;
    private final Timer readTimer;
    private final Timer updateTimer;
    private final Timer deleteTimer;
    private final Meter createMeter;
    private final Meter readMeter;
    private final Meter updateMeter;
    private final Meter deleteMeter;

    public BlobManager(DB mongoDb, String blobCollectionName, MetricRegistry metrics) {
        this.collection = mongoDb.getCollection(blobCollectionName);

        this.createTimer = metrics.timer(MetricRegistry.name(getClass(), "create"));
        this.readTimer = metrics.timer(MetricRegistry.name(getClass(), "read"));
        this.updateTimer = metrics.timer(MetricRegistry.name(getClass(), "update"));
        this.deleteTimer = metrics.timer(MetricRegistry.name(getClass(), "delete"));
        this.createMeter = metrics.meter(MetricRegistry.name(getClass(), "create"));
        this.readMeter = metrics.meter(MetricRegistry.name(getClass(), "read"));
        this.updateMeter = metrics.meter(MetricRegistry.name(getClass(), "update"));
        this.deleteMeter = metrics.meter(MetricRegistry.name(getClass(), "delete"));
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
        createMeter.mark();
        try (Timer.Context timerContext = createTimer.time()) {
            log.debug("inserting blob with json='{}'", json);
            DBObject parsed = createDBObject(json);
            WriteResult result = collection.insert(parsed);
            log.debug("successfully inserted blob of json as objectId='{}'", result.getField("_id"));
            return parsed;
        }
    }

    public DBObject read(ObjectId id) throws BlobNotFoundException {
        readMeter.mark();
        try (Timer.Context timerContext = readTimer.time()) {
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
    }

    public DBObject update(ObjectId id, String json) throws BlobNotFoundException {
        updateMeter.mark();
        try (Timer.Context timerContext = updateTimer.time()) {
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
    }

    public boolean delete(ObjectId id) throws BlobNotFoundException {
        deleteMeter.mark();
        try (Timer.Context timerContext = deleteTimer.time();) {
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
}
