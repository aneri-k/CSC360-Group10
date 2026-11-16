/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  test (Automated Unit Testing Suite)
 * FILE:    BirdCounterTest.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/tests)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial tests verifying candidate counting and drawing sanity.
 *     - v1.1.0: Extended tests for label string formatting, candidate bounds
 *               immutability, and null/empty handling across boundary cases.
 * =============================================================================
 * THEORETICAL FOUNDATIONS (Burger & Burge, 2016):
 *   - Chapter 13: Geometric Properties & Bounding Box Overlays
 *   - Chapter 10: Topological Equivalence Count Verification
 * =============================================================================
 */

import detection.BirdCandidate;
import detection.BirdCounter;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BirdCounter}.
 *
 * <p>Verifies tally aggregation, bounding box annotation logic,
 * defensive copying, and null safety guarantees.</p>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
class BirdCounterTest {

    private BirdCounter counter;

    @BeforeEach
    void setUp() {
        counter = new BirdCounter();
    }

    // -------------------------------------------------------------------------
    // 1. Counting Logic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("count() returns 0 for null list")
    void countNullListIsZero() {
        assertEquals(0, counter.count(null));
    }

    @Test
    @DisplayName("count() returns 0 for empty list")
    void countEmptyListIsZero() {
        assertEquals(0, counter.count(Collections.emptyList()));
    }

    @Test
    @DisplayName("count() returns correct size for non-empty list")
    void countReturnsListSize() {
        List<BirdCandidate> candidates = List.of(
            new BirdCandidate(1, new Rectangle(0, 0, 10, 10), 100),
            new BirdCandidate(2, new Rectangle(50, 50, 20, 20), 300),
            new BirdCandidate(3, new Rectangle(100, 100, 5, 5), 20)
        );
        assertEquals(3, counter.count(candidates));
    }

    // -------------------------------------------------------------------------
    // 2. Annotation & Rendering
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("drawDetections() returns a non-null ImagePlus")
    void drawDetectionsReturnsImage() {
        ImagePlus source = solidGrayImage(200, 50, 50);
        List<BirdCandidate> candidates = List.of(
            new BirdCandidate(1, new Rectangle(5, 5, 20, 20), 400)
        );
        ImagePlus result = counter.drawDetections(source, candidates);
        assertNotNull(result, "drawDetections() must return a non-null ImagePlus");
    }

    @Test
    @DisplayName("drawDetections() result has the same dimensions as the source")
    void drawDetectionsSameDimensions() {
        ImagePlus source = solidGrayImage(200, 60, 40);
        List<BirdCandidate> candidates = List.of(
            new BirdCandidate(1, new Rectangle(0, 0, 10, 10), 100)
        );
        ImagePlus result = counter.drawDetections(source, candidates);
        assertEquals(60, result.getWidth(),  "Width must match source");
        assertEquals(40, result.getHeight(), "Height must match source");
    }

    @Test
    @DisplayName("drawDetections() with empty candidate list still returns an image")
    void drawDetectionsEmptyCandidates() {
        ImagePlus source = solidGrayImage(100, 30, 30);
        ImagePlus result = counter.drawDetections(source, Collections.emptyList());
        assertNotNull(result);
    }

    @Test
    @DisplayName("drawDetections() with null candidate list still returns an image")
    void drawDetectionsNullCandidates() {
        ImagePlus source = solidGrayImage(100, 30, 30);
        assertDoesNotThrow(() -> counter.drawDetections(source, null));
    }

    @Test
    @DisplayName("drawDetections() with null source image throws IllegalArgumentException")
    void drawDetectionsNullSourceThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            counter.drawDetections(null, Collections.emptyList()));
    }

    @Test
    @DisplayName("Candidate labels adhere to 'Bird N' formatting")
    void candidateLabelFormatting() {
        BirdCandidate c1 = new BirdCandidate(1, new Rectangle(10, 10, 20, 20), 400);
        BirdCandidate c2 = new BirdCandidate(42, new Rectangle(0, 0, 5, 5), 25);
        assertEquals("Bird 1", c1.getLabel());
        assertEquals("Bird 42", c2.getLabel());
    }

    @Test
    @DisplayName("BirdCandidate bounds returns a defensive copy")
    void candidateBoundsImmutability() {
        Rectangle orig = new Rectangle(5, 5, 20, 20);
        BirdCandidate c = new BirdCandidate(1, orig, 100);

        Rectangle fetched = c.getBounds();
        fetched.x = 999; // mutate fetched rectangle

        assertEquals(5, c.getBounds().x, "Mutating returned bounds must not alter candidate internal state");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static ImagePlus solidGrayImage(int intensity, int w, int h) {
        ByteProcessor bp = new ByteProcessor(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                bp.putPixel(x, y, intensity);
            }
        }
        return new ImagePlus("test", bp);
    }
}
