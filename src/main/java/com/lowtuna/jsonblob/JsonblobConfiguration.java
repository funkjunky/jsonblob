package com.lowtuna.jsonblob;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowtuna.jsonblob.config.GoogleAnalyticsConfig;
import com.lowtuna.jsonblob.config.MongoDbConfig;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class JsonblobConfiguration extends Configuration {

    @JsonProperty
    private boolean deleteEnabled = false;

    @JsonProperty
    @NotEmpty
    private String blobCollectionName = "blob";

    @Valid
    @NotNull
    @JsonProperty("mongo")
    private MongoDbConfig mongoDbConfig = new MongoDbConfig();

    @Valid
    @NotNull
    @JsonProperty("ga")
    private GoogleAnalyticsConfig googleAnalyticsConfig = new GoogleAnalyticsConfig();

}
