package com.lowtuna.jsonblob.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;

@Getter
@Setter
public class MongoDbConfig {
    @NotEmpty
    @JsonProperty
    private String hostname = "localhost";

    @NotNull
    @Min(1)
    @Max(Short.MAX_VALUE)
    @JsonProperty
    private int port = 27017;

    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    @NotEmpty
    @JsonProperty
    private String database;

    public DB instance() {
        try {
            MongoClient mongoClient = new MongoClient(hostname, port);
            DB db = mongoClient.getDB(database);
            if (StringUtils.isNotBlank(username)) {
                if (!db.authenticate(username, password.toCharArray())) {
                    throw new IllegalStateException("Couldn't authenticate with MongoDB (username='" + username + "') database named " + database + " at " + hostname + ":" + port);
                }
            }
            return db;
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Couldn't connected to MongoDB at " + hostname + ":" + port);
        }
    }

}
