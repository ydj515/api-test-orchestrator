package karate;

import java.util.ArrayList;
import java.util.List;

final class KarateSelection {
    private KarateSelection() {
    }

    static List<String> tags(String service, String api) {
        List<String> tags = new ArrayList<>();
        tags.add("@service=" + (service == null || service.isBlank() ? "reservation" : service));
        if (api != null && !api.isBlank()) {
            tags.add("@api=" + api);
        }
        return List.copyOf(tags);
    }
}
