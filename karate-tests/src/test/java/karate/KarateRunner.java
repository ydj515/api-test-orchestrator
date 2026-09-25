package karate;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("e2e")
class KarateRunner {

    @Test
    void testAll() {
        String service = System.getenv("SERVICE");
        String api = System.getenv("API");
        List<String> tags = KarateSelection.tags(service, api);
        var builder = Runner.path("classpath:scenarios");
        if (!tags.isEmpty()) {
            builder.tags(tags);
        }

        Results results = builder.parallel(5);
        org.junit.jupiter.api.Assertions.assertTrue(results.getScenariosTotal() > 0, "No scenarios matched the filters");
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

}
