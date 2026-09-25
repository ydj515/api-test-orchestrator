package com.example.reportserver.presentation.run.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public final class PageJson {
    private final ObjectMapper mapper;
    public PageJson(ObjectMapper mapper) { this.mapper = mapper; }

    /** Safe only for script[type=application/json] text, not arbitrary HTML. */
    public String write(Object value) {
        try {
            return mapper.writeValueAsString(value)
                    .replace("<", "\\u003c").replace(">", "\\u003e").replace("&", "\\u0026")
                    .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
        } catch (JsonProcessingException cause) {
            throw new IllegalStateException("Failed to serialize page data", cause);
        }
    }
}
