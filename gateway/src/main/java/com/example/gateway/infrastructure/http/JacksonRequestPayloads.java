package com.example.gateway.infrastructure.http;

import com.example.gateway.application.proxy.port.out.RequestPayloads;
import com.example.gateway.domain.routing.model.ApiRoute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class JacksonRequestPayloads implements RequestPayloads {
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");
    private final ObjectMapper objectMapper;

    public JacksonRequestPayloads(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public URI targetUri(ApiRoute route, String plainBody) {
        if (!RouteUtils.hasTemplateVariables(route.externalPath())) {
            return RouteUtils.buildTargetUri(route);
        }

        Set<String> requiredVariables = RouteUtils.extractTemplateVariables(route.externalPath());
        Map<String, String> variableValues = parseTemplateVariables(plainBody, requiredVariables);
        return RouteUtils.buildTargetUri(route, variableValues);
    }

    public Map<String, Object> parse(String plainBody) {
        try {
            return objectMapper.readValue(plainBody, new TypeReference<>() {});
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid JSON body", ex);
        }
    }

    private Map<String, String> parseTemplateVariables(String plainBody, Set<String> requiredVariables) {
        Map<String, Object> payload = parse(plainBody);
        Map<String, String> variables = new HashMap<>();

        for (String variable : requiredVariables) {
            Object directValue = payload.get(variable);
            if (directValue != null) {
                String value = String.valueOf(directValue);
                validatePathVariable(variable, value);
                variables.put(variable, value);
                continue;
            }

            Object pathVariables = payload.get("pathVariables");
            if (pathVariables instanceof Map<?, ?> nested) {
                Object nestedValue = nested.get(variable);
                if (nestedValue != null) {
                    String value = String.valueOf(nestedValue);
                    validatePathVariable(variable, value);
                    variables.put(variable, value);
                }
            }
        }
        return variables;
    }

    private void validatePathVariable(String variableName, String value) {
        if (!PATH_VARIABLE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid path variable format: " + variableName);
        }
    }

}
