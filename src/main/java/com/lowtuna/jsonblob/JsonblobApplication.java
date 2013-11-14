package com.lowtuna.jsonblob;

import java.net.URL;

import com.lowtuna.jsonblob.business.BlobManager;
import com.lowtuna.jsonblob.business.DemoBlobHelper;
import com.lowtuna.jsonblob.healthcheck.MongoHealthCheck;
import com.lowtuna.jsonblob.resource.JsonBlobCollectionResource;
import com.lowtuna.jsonblob.util.mongo.JacksonMongoDbModule;
import com.mongodb.DB;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.apache.commons.lang3.StringUtils;

public class JsonblobApplication extends Application<JsonblobConfiguration> {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[2];
            args[0] = "server";
        }
        if (StringUtils.isEmpty(args[args.length - 1])) {
            URL configUrl = JsonblobApplication.class.getClassLoader().getResource("jsonblob.yml");
            args[args.length - 1] = configUrl.getPath();
        }
        new JsonblobApplication().run(args);
    }

    @Override
    public String getName() {
        return "jsonblob";
    }

    @Override
    public void initialize(Bootstrap<JsonblobConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(JsonblobConfiguration configuration,
                    Environment environment) throws ClassNotFoundException {
        environment.getObjectMapper().registerModule(new JacksonMongoDbModule());

        DB mongoDBInstance = configuration.getMongoDbConfig().instance();

        BlobManager blobManager = new BlobManager(mongoDBInstance, configuration.getBlobCollectionName(), environment.metrics());

        environment.healthChecks().register("MongoDB", new MongoHealthCheck(mongoDBInstance));

        DemoBlobHelper demoBlobHelper = new DemoBlobHelper(blobManager);
        environment.lifecycle().manage(demoBlobHelper);

        environment.jersey().register(new JsonBlobCollectionResource(blobManager, demoBlobHelper, configuration.isDeleteEnabled()));

    }

}
