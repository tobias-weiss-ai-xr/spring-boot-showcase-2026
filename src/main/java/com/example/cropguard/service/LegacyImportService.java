package com.example.cropguard.service;

import com.example.cropguard.config.AppProperties;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.modules.billing.RatingPackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * One-off migration aid: reads the legacy CSV rating export
 * ({@code classpath:legacy/ratings_legacy.csv}), validates every record and
 * maps valid rows to rating-pack data ({@link RatingPackService.CropRating} /
 * {@link RatingPackService.DeductibleRating}).
 *
 * <p>Invalid rows (unknown crop, empty fields, negative numbers, wrong column
 * count) are collected into a per-record failure report instead of aborting —
 * the import never throws on bad data. Gated behind
 * {@code cropguard.legacy-import.enabled} (default {@code false}).
 */
@Service
public class LegacyImportService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyImportService.class);
    private static final String FIXTURE = "legacy/ratings_legacy.csv";
    private static final int COLUMNS = 6;

    /** One successfully imported legacy row, mapped to rating-pack data. */
    public record ImportedRating(
        int lineNumber,
        CropType crop,
        RatingPackService.CropRating cropRating,
        double bundeslandFactor,
        RatingPackService.DeductibleRating deductibleRating
    ) {}

    /** One rejected row: 1-based line number, raw text and the reason. */
    public record RejectedRow(int lineNumber, String rawLine, String reason) {}

    /** Full import outcome. */
    public record ImportReport(int totalRows, List<ImportedRating> imported, List<RejectedRow> rejected) {}

    private final AppProperties properties;

    public LegacyImportService(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws IOException {
        if (!isEnabled()) {
            return;
        }
        ImportReport report = importFixture();
        log.info("Legacy rating import: {} rows total, {} imported, {} rejected",
            report.totalRows(), report.imported().size(), report.rejected().size());
        for (RejectedRow row : report.rejected()) {
            log.warn("Legacy import rejected line {}: [{}] — {}", row.lineNumber(), row.rawLine(), row.reason());
        }
    }

    private boolean isEnabled() {
        return properties.legacyImport() != null && properties.legacyImport().enabled();
    }

    /** Reads the committed fixture from the classpath. */
    public ImportReport importFixture() throws IOException {
        try (InputStream in = new ClassPathResource(FIXTURE).getInputStream()) {
            return importCsv(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Parses, validates and maps every CSV line. Never throws on invalid
     * records — they are reported via {@link ImportReport#rejected()}.
     */
    public ImportReport importCsv(String content) {
        List<ImportedRating> imported = new ArrayList<>();
        List<RejectedRow> rejected = new ArrayList<>();
        int lineNumber = 0;
        int dataRows = 0;
        boolean headerSeen = false;
        try (Scanner scanner = new Scanner(content)) {
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (!headerSeen) {
                    headerSeen = true; // first record is the column header
                    continue;
                }
                dataRows++;
                ImportedRating rating = parseAndValidate(line, lineNumber, rejected);
                if (rating != null) {
                    imported.add(rating);
                }
            }
        }
        return new ImportReport(dataRows, imported, rejected);
    }

    /** Returns the mapped row, or {@code null} after adding a rejection reason. */
    private ImportedRating parseAndValidate(String line, int lineNumber, List<RejectedRow> rejected) {
        String[] cols = line.split(",");
        if (cols.length != COLUMNS) {
            rejected.add(new RejectedRow(lineNumber, line, "expected " + COLUMNS + " columns, got " + cols.length));
            return null;
        }
        String cropName = cols[0].trim();
        if (cropName.isEmpty()) {
            rejected.add(new RejectedRow(lineNumber, line, "crop is empty"));
            return null;
        }
        CropType crop;
        try {
            crop = CropType.valueOf(cropName);
        } catch (IllegalArgumentException e) {
            rejected.add(new RejectedRow(lineNumber, line, "unknown crop '" + cropName + "'"));
            return null;
        }

        Double baseRate = positive(cols[1], "base_rate", line, lineNumber, rejected);
        Double bundeslandFactor = positive(cols[3], "bundesland_factor", line, lineNumber, rejected);
        Double deductibleFactor = positive(cols[5], "deductible_factor", line, lineNumber, rejected);
        if (baseRate == null || bundeslandFactor == null || deductibleFactor == null) {
            return null;
        }

        String risk = cols[2].trim();
        CropType.RiskCategory category;
        try {
            category = CropType.RiskCategory.valueOf(risk);
        } catch (IllegalArgumentException e) {
            rejected.add(new RejectedRow(lineNumber, line, "unknown risk category '" + risk + "'"));
            return null;
        }

        double deductiblePercent;
        try {
            deductiblePercent = Double.parseDouble(cols[4].trim());
        } catch (NumberFormatException e) {
            rejected.add(new RejectedRow(lineNumber, line, "deductible_percent is not a number: '" + cols[4].trim() + "'"));
            return null;
        }
        if (deductiblePercent < 0) {
            rejected.add(new RejectedRow(lineNumber, line, "deductible_percent must be >= 0, got " + deductiblePercent));
            return null;
        }

        return new ImportedRating(lineNumber, crop,
            new RatingPackService.CropRating(baseRate, category),
            bundeslandFactor,
            new RatingPackService.DeductibleRating(deductiblePercent, deductibleFactor));
    }

    private Double positive(String raw, String field, String line, int lineNumber, List<RejectedRow> rejected) {
        String value = raw.trim();
        if (value.isEmpty()) {
            rejected.add(new RejectedRow(lineNumber, line, field + " is empty"));
            return null;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed <= 0) {
                rejected.add(new RejectedRow(lineNumber, line, field + " must be > 0, got " + parsed));
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            rejected.add(new RejectedRow(lineNumber, line, field + " is not a number: '" + value + "'"));
            return null;
        }
    }
}
