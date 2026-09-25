package karate;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KarateSelectionTest {
    @Test
    void defaultsToReservationWithoutInventingAnOrganization() {
        assertEquals(List.of("@service=reservation"), KarateSelection.tags(" ", null));
    }

    @Test
    void combinesServiceAndApiFilters() {
        assertEquals(List.of("@service=visit", "@api=listSites"),
                KarateSelection.tags("visit", "listSites"));
    }
}
