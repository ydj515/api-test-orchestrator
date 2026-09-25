package architecture.policy;

import java.util.List;

/** One dependency policy shared by the JDK and ArchUnit analysis engines. */
public final class LayerPolicy {
    private LayerPolicy() { }

    public static boolean forbidden(String base, String source, String target) {
        if (!source.startsWith(base + ".")) {
            return false;
        }
        if (target.startsWith("com.example.") && !target.startsWith(base + ".")) {
            return true;
        }
        String from = source.substring(base.length() + 1);
        String layer = from.contains(".") ? from.substring(0, from.indexOf('.')) : "bootstrap";
        if (base.equals("karate")) {
            return false;
        }
        String bootstrap = switch (base) {
            case "com.example.gateway" -> "GatewayApplication";
            case "com.example.mockserver" -> "MockRestApiServerApplication";
            case "com.example.reportserver" -> "ReportServerApplication";
            default -> "";
        };
        if (layer.equals("bootstrap") && !from.equals(bootstrap) && !from.startsWith(bootstrap + "$")) {
            return true;
        }
        if (!List.of("bootstrap", "presentation", "application", "domain", "infrastructure", "config").contains(layer)) {
            return true;
        }
        if (target.equals("org.springframework.transaction.annotation.Transactional")) {
            return !layer.equals("application");
        }
        boolean spring = target.startsWith("org.springframework.");
        boolean serialization = target.startsWith("com.fasterxml.jackson.") || target.startsWith("org.yaml.");
        boolean servlet = target.startsWith("jakarta.");
        if (layer.equals("domain") && (spring || serialization || servlet)) {
            return true;
        }
        if (layer.equals("application")) {
            if (serialization || servlet) {
                return true;
            }
            if (spring && !target.startsWith("org.springframework.stereotype.")
                    && !target.startsWith("org.springframework.transaction.annotation.")) {
                return true;
            }
        }
        if (!target.startsWith(base + ".")) {
            return false;
        }
        String to = target.substring(base.length() + 1);
        if (!to.contains(".") && !layer.equals("bootstrap") && !layer.equals("config")) {
            return true;
        }
        return switch (layer) {
            case "domain" -> !to.startsWith("domain.");
            case "application" -> !to.startsWith("application.") && !to.startsWith("domain.");
            case "presentation" -> to.startsWith("infrastructure.") || to.startsWith("config.")
                    || to.contains(".port.out.")
                    || (to.startsWith("domain.") && !(to.contains(".model.")
                        || to.contains(".exception.") || to.equals("domain.shared.BusinessException")
                        || to.startsWith("domain.shared.BusinessException$")));
            case "infrastructure" -> to.startsWith("presentation.") || to.startsWith("config.")
                    || (to.startsWith("application.") && to.contains(".service."));
            default -> false;
        };
    }
}
