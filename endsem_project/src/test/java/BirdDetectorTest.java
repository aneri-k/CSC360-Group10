/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  test (Automated Unit Testing Suite)
 * FILE:    BirdDetectorTest.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/tests & feature/extended-test-suite)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial tests verifying basic thresholding, area filters,
 *               and candidate ID sequencing.
 *     - v1.1.0: Expanded with tests for Otsu auto-threshold, dual-polarity
 *               segmentation, Gaussian blur, circularity shape filtering,
 *               and binary mask generation.
 * =============================================================================
 * THEORETICAL FOUNDATIONS (Burger & Burge, 2016):
 *   - Chapter 10: Region Labeling & 4-Connectivity Verification
 *   - Chapters 10 & 11: Global & Otsu Variance Thresholding
 *   - Chapter 13: Morphological Circularity & Compactness Descriptors
 * =============================================================================
 */

import detection.BirdCandidate;
import detection.BirdDetector;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BirdDetector}.
 *
 * <p>Uses controlled synthetic test images so that theoretical expectations
 * can be verified rigorously without external file dependencies.</p>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
class BirdDetectorTest {

    private BirdDetector detector;

    @BeforeEach
    void setUp() {
        // Use permissive area filters so synthetic test images pass through
        detector = new BirdDetector(128, 1, 100_000);
    }

    // -------------------------------------------------------------------------
    // 1. Basic Threshold Logic Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("All-black image produces zero candidates in default bright mode")
    void allBlackImageYieldsNoCandidates() {
        ImagePlus black = solidGrayImage(0, 10, 10);
        List<BirdCandidate> result = detector.detect(black);
        assertEquals(0, result.size(), "Expected no candidates in an all-black image");
    }

    @Test
    @DisplayName("All-white image produces at least one candidate in default bright mode")
    void allWhiteImageYieldsCandidates() {
        ImagePlus white = solidGrayImage(255, 10, 10);
        List<BirdCandidate> result = detector.detect(white);
        assertTrue(result.size() >= 1, "Expected at least one candidate in an all-white image");
    }

    @Test
    @DisplayName("Pixels at or below threshold are treated as background")
    void pixelAtThresholdIsBackground() {
        ImagePlus atThreshold = solidGrayImage(128, 8, 8);
        List<BirdCandidate> result = detector.detect(atThreshold);
        assertEquals(0, result.size(), "Pixels at threshold value should be background");
    }

    @Test
    @DisplayName("Pixels strictly above threshold become foreground")
    void pixelAboveThresholdIsForeground() {
        ImagePlus bright = solidGrayImage(129, 8, 8);
        List<BirdCandidate> result = detector.detect(bright);
        assertTrue(result.size() >= 1, "Pixels above threshold should produce foreground candidates");
    }

    // -------------------------------------------------------------------------
    // 2. Dual Polarity Tests (Dark Silhouettes vs Bright Objects)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Dark mode detects pixels strictly below threshold")
    void darkModeDetectsDarkObjects() {
        detector.setDetectDark(true);
        detector.setThreshold(128);

        // A dark image (intensity 50) with dark mode active should become foreground
        ImagePlus dark = solidGrayImage(50, 10, 10);
        List<BirdCandidate> result = detector.detect(dark);
        assertTrue(result.size() >= 1, "Dark image should be detected as foreground in dark mode");

        // A bright image (intensity 200) should be treated as background
        ImagePlus bright = solidGrayImage(200, 10, 10);
        List<BirdCandidate> brightResult = detector.detect(bright);
        assertEquals(0, brightResult.size(), "Bright pixels should be background in dark mode");
    }

    // -------------------------------------------------------------------------
    // 3. Area Filter Constraints
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Component smaller than minArea is filtered out")
    void smallComponentFilteredOut() {
        detector = new BirdDetector(128, 500, 100_000);
        ImagePlus small = solidGrayImage(255, 5, 5); // 25 pixels total
        List<BirdCandidate> result = detector.detect(small);
        assertEquals(0, result.size(), "Component smaller than minArea should be ignored");
    }

    @Test
    @DisplayName("Component larger than maxArea is filtered out")
    void largeComponentFilteredOut() {
        detector = new BirdDetector(128, 1, 5);
        ImagePlus large = solidGrayImage(255, 10, 10); // 100 pixels total
        List<BirdCandidate> result = detector.detect(large);
        assertEquals(0, result.size(), "Component larger than maxArea should be ignored");
    }

    // -------------------------------------------------------------------------
    // 4. Input Validation & Edge Cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Null input throws IllegalArgumentException")
    void nullInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> detector.detect(null));
    }

    @Test
    @DisplayName("Clamping threshold value to [0, 255]")
    void thresholdClamping() {
        detector.setThreshold(-50);
        assertEquals(0, detector.getThreshold(), "Threshold below 0 should clamp to 0");

        detector.setThreshold(300);
        assertEquals(255, detector.getThreshold(), "Threshold above 255 should clamp to 255");
    }

    // -------------------------------------------------------------------------
    // 5. BirdCandidate Property Invariants
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Returned candidates have positive area")
    void candidatesHavePositiveArea() {
        ImagePlus white = solidGrayImage(255, 10, 10);
        List<BirdCandidate> result = detector.detect(white);
        for (BirdCandidate c : result) {
            assertTrue(c.getArea() > 0, "Every candidate must have positive area");
        }
    }

    @Test
    @DisplayName("Returned candidates have non-null bounds")
    void candidatesHaveBounds() {
        ImagePlus white = solidGrayImage(255, 10, 10);
        List<BirdCandidate> result = detector.detect(white);
        for (BirdCandidate c : result) {
            assertNotNull(c.getBounds(), "Every candidate must have a bounding rectangle");
        }
    }

    @Test
    @DisplayName("Candidate IDs start from 1 and are sequential")
    void candidateIdsAreSequential() {
        ImagePlus white = solidGrayImage(255, 10, 10);
        List<BirdCandidate> result = detector.detect(white);
        for (int i = 0; i < result.size(); i++) {
            assertEquals(i + 1, result.get(i).getId(),
                "Candidate ID should equal its 1-based index");
        }
    }

    // -------------------------------------------------------------------------
    // 6. Color Image & Conversion
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Colour (RGB) image is accepted and converted internally")
    void colorImageIsAccepted() {
        ColorProcessor cp = new ColorProcessor(10, 10);
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                cp.putPixel(x, y, 0xFF0000); // pure red
            }
        }
        ImagePlus colorImg = new ImagePlus("red", cp);
        assertDoesNotThrow(() -> detector.detect(colorImg));
    }

    // -------------------------------------------------------------------------
    // 7. Otsu Auto-Threshold & Mask Generation Tests (v1.1.0 Enhancements)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateAutoThreshold computes a valid threshold in range [0, 255]")
    void autoThresholdCalculation() {
        ByteProcessor bp = new ByteProcessor(50, 50);
        // Half dark (30), half bright (220)
        for (int y = 0; y < 50; y++) {
            for (int x = 0; x < 50; x++) {
                bp.putPixel(x, y, (x < 25) ? 30 : 220);
            }
        }
        ImagePlus bimodal = new ImagePlus("bimodal", bp);
        int autoT = detector.calculateAutoThreshold(bimodal);

        assertTrue(autoT >= 30 && autoT <= 220,
            "Otsu threshold should split bimodal histogram between 30 and 220");
    }

    @Test
    @DisplayName("createBinaryMask returns a non-null binary mask with matching dimensions")
    void binaryMaskGeneration() {
        ImagePlus src = solidGrayImage(200, 40, 30);
        ImagePlus mask = detector.createBinaryMask(src);

        assertNotNull(mask, "Binary mask must not be null");
        assertEquals(40, mask.getWidth(), "Mask width must match source");
        assertEquals(30, mask.getHeight(), "Mask height must match source");
    }

    @Test
    @DisplayName("Circularity filter rejects long thin lines while accepting compact squares")
    void circularityFiltering() {
        // Create an image with a long thin 1-pixel horizontal wire (very low circularity)
        ByteProcessor bp = new ByteProcessor(60, 60);
        for (int x = 5; x < 55; x++) {
            bp.putPixel(x, 30, 255); // 50x1 horizontal line
        }
        ImagePlus lineImage = new ImagePlus("line", bp);

        // Require circularity >= 0.2
        detector.setMinCircularity(0.2);
        detector.setMinArea(10);
        List<BirdCandidate> lineResult = detector.detect(lineImage);
        assertEquals(0, lineResult.size(), "Thin 1-pixel line should be rejected by circularity filter");

        // Now test a compact 20x20 square (circularity ~ 0.78)
        ByteProcessor bpSquare = new ByteProcessor(60, 60);
        for (int y = 20; y < 40; y++) {
            for (int x = 20; x < 40; x++) {
                bpSquare.putPixel(x, y, 255);
            }
        }
        ImagePlus squareImage = new ImagePlus("square", bpSquare);
        List<BirdCandidate> squareResult = detector.detect(squareImage);
        assertEquals(1, squareResult.size(), "Compact square should pass circularity filter");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /** Creates an 8-bit grayscale ImagePlus filled with a single intensity. */
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
