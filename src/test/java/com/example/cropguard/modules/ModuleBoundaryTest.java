package com.example.cropguard.modules;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Architectural test: enforces the policy/billing/claims module boundaries.
 *
 * <p>Rule: a class inside {@code modules/<A>} must not directly reference another
 * module's repositories or entities — cross-module data access goes through the
 * owning module's service facade (or a port), and every such exception must be
 * listed in {@link #ALLOWED_CROSS_MODULE_ACCESS} below with a reason.
 *
 * <p>Entities/repositories currently live in the shared-layer packages
 * {@code com.example.cropguard.entity} / {@code ...repository} (legacy layout that
 * also serves insured/plot/hail, which are not part of this change). Module
 * ownership is therefore encoded in {@link #OWNED_TYPES}: Policy(+Repository)
 * belongs to the policy module, Claim(+Repository) to the claims module. Types not
 * listed there (Insured, Plot, HailEvent, domain enums, DTOs) are shared kernel and
 * may be referenced by any module. Any repository class placed inside
 * {@code modules/<other>} is treated as module-owned by its package automatically.
 */
class ModuleBoundaryTest {

    private static final String MODULES_ROOT = "src/main/java/com/example/cropguard/modules";
    private static final String MODULES_PKG = "com.example.cropguard.modules";

    /** repository/entity type FQN -> owning module */
    private static final Map<String, String> OWNED_TYPES = Map.of(
        "com.example.cropguard.repository.PolicyRepository", "policy",
        "com.example.cropguard.entity.Policy", "policy",
        "com.example.cropguard.repository.ClaimRepository", "claims",
        "com.example.cropguard.entity.Claim", "claims"
    );

    /**
     * Explicit allowances: source module -> referenced type -> reason.
     * Every cross-module repository/entity access must be registered here.
     */
    private static final Map<String, Map<String, String>> ALLOWED_CROSS_MODULE_ACCESS = Map.of(
        "claims", Map.of(
            "com.example.cropguard.entity.Policy",
            "claims reads the policy aggregate only through the policy module facade "
                + "PolicyService (issue/lookup port); no ClaimRepository-side join",
            "com.example.cropguard.modules.policy.PolicyService",
            "policy lookup facade/port used by ClaimService.submit()"
        )
    );

    /** A module's own classes are never violations. */
    private static String moduleOf(Path file) {
        for (int i = 0; i < file.getNameCount(); i++) {
            if ("modules".equals(file.getName(i).toString())) {
                return file.getName(i + 1).toString();
            }
        }
        fail("Not inside a module package: " + file);
        return null;
    }

    @Test
    void modulesMustNotReferenceForeignRepositoriesOrEntities() throws IOException {
        Path root = Path.of(MODULES_ROOT);
        assertTrue(Files.isDirectory(root), "modules root missing: " + MODULES_ROOT);

        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(file -> {
                String module = moduleOf(file);
                String source = read(file);
                String className = file.getFileName().toString().replace(".java", "");

                for (Map.Entry<String, String> owned : OWNED_TYPES.entrySet()) {
                    String typeFqn = owned.getKey();
                    String owner = owned.getValue();
                    if (owner.equals(module) || !references(source, typeFqn)) {
                        continue;
                    }
                    if (isAllowed(module, typeFqn)) {
                        continue;
                    }
                    violations.add(module + "/" + className + " references " + typeFqn
                        + " (owned by '" + owner + "') — route access through the '"
                        + owner + "' module facade, or list the allowance in "
                        + "ModuleBoundaryTest.ALLOWED_CROSS_MODULE_ACCESS");
                }

                // any repository class living inside a foreign module package is off-limits too
                for (String other : List.of("policy", "billing", "claims")) {
                    if (other.equals(module)) {
                        continue;
                    }
                    String foreignPrefix = MODULES_PKG + "." + other + ".";
                    for (String line : source.split("\n")) {
                        String fqn = foreignRepositoryReference(line, foreignPrefix);
                        if (fqn != null && !isAllowed(module, fqn)) {
                            violations.add(module + "/" + className + " references foreign "
                                + "module repository: " + fqn);
                        }
                    }
                }
            });
        }

        assertTrue(violations.isEmpty(),
            "Module boundary violations:\n  " + String.join("\n  ", violations));
    }

    /** true if the source imports the type or uses its fully qualified name inline */
    private static boolean references(String source, String typeFqn) {
        String simpleName = typeFqn.substring(typeFqn.lastIndexOf('.') + 1);
        if (source.contains("import " + typeFqn + ";")) {
            return true;
        }
        // fully qualified inline usage (e.g. parameter types)
        return source.contains(typeFqn);
    }

    private static boolean isAllowed(String module, String typeFqn) {
        Map<String, String> allowances = ALLOWED_CROSS_MODULE_ACCESS.get(module);
        return allowances != null && allowances.containsKey(typeFqn);
    }

    /** extracts modules.<other>.XxxRepository from import or inline FQN usage */
    private static String foreignRepositoryReference(String line, String foreignPrefix) {
        String candidate = null;
        if (line.trim().startsWith("import ") && line.contains(foreignPrefix)) {
            candidate = line.trim().replace("import ", "").replace(";", "").trim();
        } else if (line.contains(foreignPrefix)) {
            int idx = line.indexOf(foreignPrefix);
            int end = idx + foreignPrefix.length();
            while (end < line.length() && Character.isJavaIdentifierPart(line.charAt(end))) {
                end++;
            }
            candidate = line.substring(idx, end);
        }
        if (candidate != null && candidate.endsWith("Repository")) {
            return candidate;
        }
        return null;
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            return fail("Cannot read " + file, e);
        }
    }
}
