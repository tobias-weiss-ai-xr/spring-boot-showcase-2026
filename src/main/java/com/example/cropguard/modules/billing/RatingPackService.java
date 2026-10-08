package com.example.cropguard.modules.billing;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Loads and validates YAML rating packs from classpath:rating/*.yml at startup
 * and exposes all rating lookups with country override support.
 *
 * <p>default.yml is the mandatory base pack (Germany) and must define a rating
 * for every enum constant. Every other pack (e.g. pl.yml) is a country override:
 * it may redefine individual entries, which are merged over the base pack.
 *
 * <p>Fail-fast rules (any violation aborts application startup):
 * <ul>
 *   <li>unknown enum key (crop, Bundesland, deductible, risk category)</li>
 *   <li>baseRateEurPerHa / riskFactor / premiumFactor &lt;= 0</li>
 *   <li>deductible percentage &lt; 0</li>
 *   <li>missing entry in the base pack</li>
 * </ul>
 */
@Service
public class RatingPackService {

    public static final String DEFAULT_COUNTRY = "DE";

    /** Per-crop rating: base rate in EUR/ha and risk category. */
    public record CropRating(double baseRateEurPerHa, CropType.RiskCategory riskCategory) {}

    /** Per-deductible rating: payout percentage and premium reduction factor. */
    public record DeductibleRating(double percentage, double premiumFactor) {}

    private record RatingPack(
        Map<CropType, CropRating> crops,
        Map<Bundesland, Double> bundeslandRiskFactors,
        Map<Deductible, DeductibleRating> deductibles
    ) {}

    private final Map<String, RatingPack> packsByCountry = new HashMap<>();

    public RatingPackService() {
        this(loadPacksFromClasspath());
    }

    /** Test-visible constructor: filename -> parsed YAML root. */
    RatingPackService(Map<String, Map<String, Object>> packsByFilename) {
        Map<String, Map<String, Object>> sorted = new TreeMap<>(packsByFilename);
        Map<String, Object> defaultRoot = sorted.remove("default.yml");
        if (defaultRoot == null) {
            throw new IllegalStateException("Rating pack 'rating/default.yml' is missing");
        }

        String baseCountry = country(defaultRoot, "default.yml");
        if (!DEFAULT_COUNTRY.equals(baseCountry)) {
            throw new IllegalStateException("default.yml must declare country: " + DEFAULT_COUNTRY);
        }
        RatingPack base = parsePack(defaultRoot, true, "default.yml");

        packsByCountry.put(baseCountry, base);
        for (var entry : sorted.entrySet()) {
            String country = country(entry.getValue(), entry.getKey());
            if (packsByCountry.containsKey(country)) {
                throw new IllegalStateException("Duplicate rating pack for country " + country);
            }
            packsByCountry.put(country, merge(base, parsePack(entry.getValue(), false, entry.getKey())));
        }
        if (packsByCountry.size() <= 1) {
            throw new IllegalStateException("No country override rating packs found under rating/");
        }
    }

    public double baseRateFor(CropType crop, String country) {
        return pack(country).crops().get(crop).baseRateEurPerHa();
    }

    public CropType.RiskCategory riskCategoryFor(CropType crop, String country) {
        return pack(country).crops().get(crop).riskCategory();
    }

    public double riskFactorFor(Bundesland bundesland, String country) {
        return pack(country).bundeslandRiskFactors().get(bundesland);
    }

    public double premiumFactorFor(Deductible deductible, String country) {
        return pack(country).deductibles().get(deductible).premiumFactor();
    }

    public double percentageFor(Deductible deductible, String country) {
        return pack(country).deductibles().get(deductible).percentage();
    }

    /** Unknown countries fall back to the default (DE) pack. */
    private RatingPack pack(String country) {
        RatingPack pack = country == null ? null : packsByCountry.get(country.trim().toUpperCase());
        if (pack != null) {
            return pack;
        }
        return packsByCountry.get(DEFAULT_COUNTRY);
    }

    private static RatingPack merge(RatingPack base, RatingPack override) {
        Map<CropType, CropRating> crops = new EnumMap<>(CropType.class);
        crops.putAll(base.crops());
        crops.putAll(override.crops());
        Map<Bundesland, Double> bundeslaender = new EnumMap<>(Bundesland.class);
        bundeslaender.putAll(base.bundeslandRiskFactors());
        bundeslaender.putAll(override.bundeslandRiskFactors());
        Map<Deductible, DeductibleRating> deductibles = new EnumMap<>(Deductible.class);
        deductibles.putAll(base.deductibles());
        deductibles.putAll(override.deductibles());
        return new RatingPack(crops, bundeslaender, deductibles);
    }

    /**
     * Parse and validate one pack. When {@code complete} (base pack) every enum
     * constant must be present; override packs may define a subset.
     */
    private RatingPack parsePack(Map<String, Object> root, boolean complete, String file) {
        Map<CropType, CropRating> crops = new EnumMap<>(CropType.class);
        for (var e : section(root, "crops", file, complete).entrySet()) {
            CropType crop = parseEnum(CropType.class, e.getKey(), file);
            Map<String, Object> spec = asMap(e.getValue(), file + ": crop " + e.getKey());
            double rate = positive(number(spec, "baseRateEurPerHa", file + ": crop " + e.getKey()),
                file + ": crop " + e.getKey() + " baseRateEurPerHa");
            CropType.RiskCategory category = parseEnum(CropType.RiskCategory.class,
                text(spec, "riskCategory", file + ": crop " + e.getKey()), file);
            crops.put(crop, new CropRating(rate, category));
        }

        Map<Bundesland, Double> bundeslaender = new EnumMap<>(Bundesland.class);
        for (var e : section(root, "bundeslaender", file, complete).entrySet()) {
            Bundesland land = parseEnum(Bundesland.class, e.getKey(), file);
            bundeslaender.put(land, positive(asNumber(e.getValue(), file + ": bundesland " + e.getKey()),
                file + ": bundesland " + e.getKey() + " riskFactor"));
        }

        Map<Deductible, DeductibleRating> deductibles = new EnumMap<>(Deductible.class);
        for (var e : section(root, "deductibles", file, complete).entrySet()) {
            Deductible deductible = parseEnum(Deductible.class, e.getKey(), file);
            String ctx = file + ": deductible " + e.getKey();
            Map<String, Object> spec = asMap(e.getValue(), ctx);
            double percentage = number(spec, "percentage", ctx);
            if (percentage < 0) {
                throw new IllegalStateException(ctx + " percentage must be >= 0, got: " + percentage);
            }
            deductibles.put(deductible, new DeductibleRating(percentage,
                positive(number(spec, "premiumFactor", ctx), ctx + " premiumFactor")));
        }

        if (complete) {
            requireAll(crops.keySet(), CropType.values(), "crop", file);
            requireAll(bundeslaender.keySet(), Bundesland.values(), "bundesland", file);
            requireAll(deductibles.keySet(), Deductible.values(), "deductible", file);
        }
        return new RatingPack(crops, bundeslaender, deductibles);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> root, String name, String file, boolean required) {
        Object value = root.get(name);
        if (value == null) {
            if (required) {
                throw new IllegalStateException(file + ": missing section '" + name + "'");
            }
            return Map.of();
        }
        return asMap(value, file + ": section " + name);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value, String ctx) {
        if (!(value instanceof Map)) {
            throw new IllegalStateException(ctx + " must be a mapping, got: " + value);
        }
        return (Map<String, Object>) value;
    }

    private static double number(Map<String, Object> map, String key, String ctx) {
        Object value = map.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalStateException(ctx + ": missing or non-numeric '" + key + "'");
        }
        return ((Number) value).doubleValue();
    }

    private static String text(Map<String, Object> map, String key, String ctx) {
        Object value = map.get(key);
        if (!(value instanceof String s) || s.isBlank()) {
            throw new IllegalStateException(ctx + ": missing '" + key + "'");
        }
        return s.trim();
    }

    private static double positive(double value, String ctx) {
        if (value <= 0) {
            throw new IllegalStateException(ctx + " must be > 0, got: " + value);
        }
        return value;
    }

    private static double asNumber(Object value, String ctx) {
        if (!(value instanceof Number)) {
            throw new IllegalStateException(ctx + " must be a number, got: " + value);
        }
        return ((Number) value).doubleValue();
    }

    private static <E extends Enum<E>> void requireAll(Set<E> present, E[] all, String what, String file) {
        for (E constant : all) {
            if (!present.contains(constant)) {
                throw new IllegalStateException(file + ": base pack is missing " + what + " '" + constant.name() + "'");
            }
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name, String file) {
        try {
            return Enum.valueOf(type, name.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(file + ": unknown " + type.getSimpleName() + " '" + name + "'");
        }
    }

    private static String country(Map<String, Object> root, String file) {
        String value = text(root, "country", file);
        if (!value.matches("[A-Z]{2}")) {
            throw new IllegalStateException(file + ": 'country' must be a 2-letter ISO code, got: " + value);
        }
        return value;
    }

    private static Map<String, Map<String, Object>> loadPacksFromClasspath() {
        Map<String, Map<String, Object>> packs = new LinkedHashMap<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:rating/*.yml");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                try (InputStream in = resource.getInputStream()) {
                    Object root = new Yaml().load(in);
                    if (!(root instanceof Map)) {
                        throw new IllegalStateException("Rating pack " + filename + " is empty or not a mapping");
                    }
                    packs.put(filename, (Map<String, Object>) root);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load rating packs from classpath:rating/", e);
        }
        if (!packs.containsKey("default.yml")) {
            throw new IllegalStateException("Rating pack 'rating/default.yml' is missing");
        }
        return packs;
    }
}
