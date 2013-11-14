package com.lowtuna.jsonblob;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.assets.AssetsBundle;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.views.ViewBundle;
import com.lowtuna.jsonblob.business.BlobManager;
import com.lowtuna.jsonblob.business.DemoBlobHelper;
import com.lowtuna.jsonblob.healthcheck.MongoHealthCheck;
import com.lowtuna.jsonblob.resource.JsonBlobCollectionResource;
import com.lowtuna.jsonblob.util.mongo.JacksonMongoDbModule;
import com.mongodb.DB;

import java.net.URL;

public class JsonblobApplication extends Application<JsonblobConfiguration> {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[2];
            args[0] = "server";
            URL configUrl = JsonblobApplication.class.getClassLoader().getResource("jsonblob.yml");
            args[1] = configUrl.getPath();
        }
        new JsonblobApplication().run(args);
    }

    @Override
    public String getName() {
        return "snewsy";
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

        BlobManager blobManager = new BlobManager(mongoDBInstance);

        environment.healthChecks().register("MongoDB", new MongoHealthCheck(mongoDBInstance));

        DemoBlobHelper demoBlobHelper = new DemoBlobHelper(blobManager);
        environment.lifecycle().manage(demoBlobHelper);

        environment.jersey().register(new JsonBlobCollectionResource(blobManager, demoBlobHelper, configuration.isDeleteEnabled()));

    }
}
