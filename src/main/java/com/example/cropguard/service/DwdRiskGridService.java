package com.example.cropguard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Service
public class DwdRiskGridService {

    private static final Logger log = LoggerFactory.getLogger(DwdRiskGridService.class);
    private int ncols, nrows;
    private double xllcorner, yllcorner, cellsize, nodata;
    private int[][] grid;
    private double xMin, xMax, yMin, yMax;

    @PostConstruct
    public void load() {
        try (InputStream raw = new ClassPathResource("dwd/drought_index_july_1991_2020.asc.gz").getInputStream();
             GZIPInputStream gz = new GZIPInputStream(raw);
             BufferedReader reader = new BufferedReader(new InputStreamReader(gz, StandardCharsets.US_ASCII))) {
            ncols = (int) parseHeader(reader.readLine());
            nrows = (int) parseHeader(reader.readLine());
            xllcorner = parseHeader(reader.readLine());
            yllcorner = parseHeader(reader.readLine());
            cellsize = parseHeader(reader.readLine());
            nodata = parseHeader(reader.readLine());
            xMin = xllcorner;
            xMax = xllcorner + ncols * cellsize;
            yMin = yllcorner;
            yMax = yllcorner + nrows * cellsize;
            grid = new int[nrows][ncols];
            for (int row = 0; row < nrows; row++) {
                String[] parts = reader.readLine().trim().split("\\s+");
                if (parts.length != ncols) {
                    throw new IOException("Row " + row + " has " + parts.length + " cells, expected " + ncols);
                }
                for (int col = 0; col < ncols; col++) {
                    grid[row][col] = (int) Double.parseDouble(parts[col]);
                }
            }
            log.info("DWD drought index grid loaded: {} x {} cells, 1km, X[{},{}], Y[{},{}], NODATA {}",
                ncols, nrows, (int) xMin, (int) xMax, (int) yMin, (int) yMax, (int) nodata);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load DWD drought index grid", e);
        }
    }

    public int getDroughtIndex(double coordinateE, double coordinateN) {
        if (coordinateE < xMin || coordinateE >= xMax || coordinateN < yMin || coordinateN >= yMax) {
            return 0;
        }
        int col = (int) ((coordinateE - xllcorner) / cellsize);
        int row = (int) (nrows - 1 - (coordinateN - yllcorner) / cellsize);
        if (row < 0 || row >= nrows || col < 0 || col >= ncols) {
            return 0;
        }
        int value = grid[row][col];
        return value == (int) nodata ? 0 : value;
    }

    public double getDroughtAdjustment(double coordinateE, double coordinateN) {
        int index = getDroughtIndex(coordinateE, coordinateN);
        if (index == 0) {
            return 1.0;
        }
        double adj = 1.0 + (index - 2) * 0.05;
        return Math.max(0.8, Math.min(1.5, adj));
    }

    public boolean isCovered(double coordinateE, double coordinateN) {
        return coordinateE >= xMin && coordinateE < xMax && coordinateN >= yMin && coordinateN < yMax;
    }

    /**
     * Downsampled grid for the frontend risk map: every {@code step}-th cell, same extent.
     * NODATA cells keep their value so the client can render them as background.
     */
    public RiskMapData sampled(int step) {
        // ponytail: clamp instead of validating — protects against step=1 full-res dumps (2+ MB)
        step = Math.max(2, Math.min(20, step));
        int cols = (ncols + step - 1) / step;
        int rows = (nrows + step - 1) / step;
        int[][] values = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                values[row][col] = grid[row * step][col * step];
            }
        }
        return new RiskMapData((int) xllcorner, (int) yllcorner, (int) yMax,
            cellsize, step, cols, rows, (int) nodata, values);
    }

    public record RiskMapData(int xllcorner, int yllcorner, int ymax, double cellsize,
                              int step, int ncols, int nrows, int nodata, int[][] values) {
    }

    private double parseHeader(String line) throws IOException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IOException("Invalid ASC header line: " + line);
        }
        return Double.parseDouble(parts[1]);
    }
}
