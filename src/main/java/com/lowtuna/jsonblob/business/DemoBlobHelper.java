package com.lowtuna.jsonblob.business;

import com.codahale.dropwizard.lifecycle.Managed;
import com.mongodb.DBObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.io.StringWriter;

@Slf4j
public class DemoBlobHelper implements Managed {
    private final BlobManager blobManager;

    @Getter
    private ObjectId demoObjectId;

    public DemoBlobHelper(BlobManager blobManager) {
        this.blobManager = blobManager;
    }

    @Override
    public void start() throws Exception {
        InputStream apiDemo = getClass().getClassLoader().getResourceAsStream("apiDemo.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(apiDemo, writer);
        String apiDemoJsonString = writer.toString();

        log.debug("creating API demo object with from json={}", apiDemoJsonString);

        if (blobManager.isValidJson(apiDemoJsonString)) {
            DBObject demoObject = blobManager.create(apiDemoJsonString);
            this.demoObjectId = (ObjectId) demoObject.get("_id");
            log.info("created API demo object with ObjectId={}", demoObjectId);
        } else {
            throw new RuntimeException("Invalid api demo JSON");
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("deleting API demo object with ObjectId={}", demoObjectId);
        boolean deleted = blobManager.delete(demoObjectId);
        if (deleted) {
            log.info("deleted API demo object with ObjectId={}", demoObjectId);
        } else {
            log.warn("couldn't delete API demo object with ObjectId={}", demoObjectId);
        }
    }
}
