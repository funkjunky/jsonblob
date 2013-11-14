package com.lowtuna.jsonblob.util.view;

import io.dropwizard.views.View;
import lombok.Getter;

import java.nio.charset.Charset;
import java.util.Map;

@Getter
public class MapBackedView extends View {
    private final Map<String, Object> variables;

    public MapBackedView(String templateName, Map<String, Object> variables) {
        super(templateName);
        this.variables = variables;
    }

    public MapBackedView(String templateName, Charset charset, Map<String, Object> variables) {
        super(templateName, charset);
        this.variables = variables;
    }
}
