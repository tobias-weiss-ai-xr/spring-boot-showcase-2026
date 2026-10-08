package com.example.cropguard.service;

import com.example.cropguard.config.AppProperties;
import com.example.cropguard.domain.CropType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the legacy CSV rating import: happy path, invalid-row reporting
 * and the startup gate.
 */
class LegacyImportServiceTest {

    private LegacyImportService service(boolean enabled) {
        return new LegacyImportService(new AppProperties("secret".repeat(6), 3600L, "CropGuard",
            new AppProperties.LegacyImport(enabled)));
    }

    @Test
    void happyPath_importsCommittedFixture() throws IOException {
        LegacyImportService.ImportReport report = service(true).importFixture();

        assertEquals(9, report.totalRows(), "fixture has 9 data rows");
        assertEquals(3, report.imported().size(), "3 valid rows");
        assertEquals(6, report.rejected().size(), "6 deliberately invalid rows");

        LegacyImportService.ImportedRating wheat = report.imported().get(0);
        assertEquals(CropType.WHEAT, wheat.crop());
        assertEquals(45.0, wheat.cropRating().baseRateEurPerHa(), 0.0001);
        assertEquals(CropType.RiskCategory.LOW, wheat.cropRating().riskCategory());
        assertEquals(1.0, wheat.bundeslandFactor(), 0.0001);
        assertEquals(5.0, wheat.deductibleRating().percentage(), 0.0001);
        assertEquals(0.92, wheat.deductibleRating().premiumFactor(), 0.0001);
    }

    @Test
    void invalidRows_reportedPerRecord_withReason() {
        String csv = """
            crop,base_rate,risk,bundesland_factor,deductible_percent,deductible_factor
            WHEAT,45.0,LOW,1.0,5.0,0.92
            NO_SUCH_CROP,30.0,LOW,1.0,5.0,0.9
            CORN,,LOW,1.0,5.0,0.9
            RAPESEED,-50.0,MODERATE,1.1,5.0,0.9
            APPLES,180.0,EXTREME,1.2,5.0,0.9
            POTATOES,55.0,MODERATE,1.1,abc,0.9
            TOBACCO,120.0,MODERATE,1.1,-5.0,0.9
            HOPS
            """;

        LegacyImportService.ImportReport report = service(true).importCsv(csv);

        assertEquals(8, report.totalRows());
        assertEquals(1, report.imported().size(), "only WHEAT is valid");
        assertEquals(7, report.rejected().size());

        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("unknown crop 'NO_SUCH_CROP'")));
        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("base_rate is empty")));
        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("base_rate must be > 0")));
        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("unknown risk category 'EXTREME'")));
        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("deductible_percent is not a number")));
        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("deductible_percent must be >= 0")));
        assertTrue(report.rejected().stream()
            .anyMatch(r -> r.reason().contains("expected 6 columns, got 1")));

        // every rejection keeps line number + raw row for the report
        LegacyImportService.RejectedRow first = report.rejected().get(0);
        assertEquals(3, first.lineNumber());
        assertEquals("NO_SUCH_CROP,30.0,LOW,1.0,5.0,0.9", first.rawLine());
    }

    @Test
    void runner_doesNothingAndNeverThrows_whenDisabled() throws Exception {
        // default-off gate: run() must be a no-op, not an import and not an error
        assertDoesNotThrow(() -> service(false).run());
    }

    @Test
    void runner_completesWithoutAborting_onInvalidRows_whenEnabled() throws Exception {
        // startup must continue despite the deliberately invalid fixture rows
        assertDoesNotThrow(() -> service(true).run());
    }
}
