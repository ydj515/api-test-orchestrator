package com.example.reportserver.presentation.run.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.stereotype.Component;

@Component
public final class PageJson {
    private final ObjectWriter writer;
    public PageJson(ObjectMapper mapper) { this.writer = mapper.writer(); }

    /** Safe only for script[type=application/json] text, not arbitrary HTML. */
    public String write(Object value) {
        try {
            return writer.writeValueAsString(value)
                    .replace("<", "\\u003c").replace(">", "\\u003e").replace("&", "\\u0026")
                    .replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
        } catch (JsonProcessingException cause) {
            throw new IllegalStateException("Failed to serialize page data", cause);
        }
    }
}
