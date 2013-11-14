package com.lowtuna.jsonblob;

import com.codahale.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowtuna.jsonblob.config.GoogleAnalyticsConfig;
import com.lowtuna.jsonblob.config.MongoDbConfig;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class JsonblobConfiguration extends Configuration {

    @JsonProperty
    private boolean deleteEnabled = false;

    @Valid
    @NotNull
    @JsonProperty("mongo")
    private MongoDbConfig mongoDbConfig = new MongoDbConfig();

    @Valid
    @NotNull
    @JsonProperty("ga")
    private GoogleAnalyticsConfig googleAnalyticsConfig = new GoogleAnalyticsConfig();

}
