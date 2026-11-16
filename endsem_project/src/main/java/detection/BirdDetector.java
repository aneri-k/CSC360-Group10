package detection;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  detection (Core Image Segmentation Engine)
 * FILE:    BirdDetector.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/detection-pipeline & bugfix/threshold-inversion)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial implementation with static T=128 global thresholding and
 *               connected-component region analysis via ImageJ ParticleAnalyzer.
 *     - v1.1.0: CRITICAL FIX: Registered explicit threshold state on ImageProcessor
 *               to eliminate ParticleAnalyzer background-inversion bug.
 *               Added dual-polarity detection (dark silhouettes vs bright plumage).
 *               Added Gaussian pre-filtering (sigma=1.5) to suppress foliage texture.
 *               Added automatic Otsu threshold calculation (Burger & Burge Ch. 10/11).
 *               Added morphological circularity filtering (Burger & Burge Ch. 13).
 *               Added binary mask generation helper for qualitative inspection.
 * =============================================================================
 * THEORETICAL FOUNDATIONS (Burger & Burge, 2016):
 *   - Chapter 14: Luminance Conversion: Y = 0.299R + 0.587G + 0.114B
 *   - Chapter 4 & 5: Linear Spatial Filtering: Gaussian low-pass smoothing
 *   - Chapters 10 & 11: Thresholding & Optimal Clustering: Otsu's variance maximization
 *   - Chapter 10: Region Labeling & 4-Connectivity: Particle Analyzer flood-fill
 *   - Chapter 13: Morphological Descriptors: Area and Compactness/Circularity
 * =============================================================================
 */

/**
 * Core image segmentation and candidate localization engine for bird counting.
 *
 * <p>Processes input garden imagery through a sequential 5-stage computer vision pipeline:
 * <ol>
 *   <li><b>Grayscale Reduction:</b> Converts 24-bit RGB pixels into 8-bit luminance intensities
 *       using standard CIE human spectral sensitivity weighting (Burger &amp; Burge Ch. 14).</li>
 *   <li><b>Gaussian Pre-Filtering:</b> Applies Gaussian spatial smoothing ($\sigma = 1.5$)
 *       to attenuate high-frequency texture noise from leaves, grass, and bark.</li>
 *   <li><b>Intensity Segmentation:</b> Binarizes the smoothed grayscale image using either
 *       manual global thresholding or automated Otsu variance maximization. Supports dual polarity:
 *       dark silhouettes against daylight skies or bright plumage on dark ground.</li>
 *   <li><b>Connected-Component Analysis:</b> Employs ImageJ's {@link ParticleAnalyzer} to discover
 *       contiguous foreground pixel regions via 4-connectivity flood fill (Burger &amp; Burge Ch. 10).</li>
 *   <li><b>Morphological Filtering:</b> Discards artifacts based on 0th order moment (area)
 *       and compactness circularity ($C = \frac{4\pi \cdot \text{Area}}{\text{Perimeter}^2}$)
 *       to reject long linear branches and fences (Burger &amp; Burge Ch. 13).</li>
 * </ol>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public class BirdDetector {

    // --- Configurable Detection Parameters ---

    /** Pixel intensity threshold (0-255). */
    private int threshold;

    /** Minimum area in pixels for a component to be accepted as a candidate. */
    private int minArea;

    /** Maximum area in pixels for a component to be accepted as a candidate. */
    private int maxArea;

    /** Minimum circularity (0.0 to 1.0) to filter out long skinny branches and lines. */
    private double minCircularity;

    /** Maximum circularity (0.0 to 1.0). */
    private double maxCircularity;

    /**
     * Whether to detect dark objects on a lighter background (true),
     * or bright objects on a darker background (false).
     */
    private boolean detectDark;

    /** Whether to apply Gaussian blur before thresholding to suppress noise. */
    private boolean useGaussianBlur;

    /** Sigma value for Gaussian blur smoothing kernel. Default is 1.5. */
    private double blurSigma;

    /**
     * Creates a {@code BirdDetector} configured with default parameters:
     * <ul>
     *   <li>Threshold: 128 (mid-intensity)</li>
     *   <li>Min Area: 80 pixels</li>
     *   <li>Max Area: 500,000 pixels (supports modern multi-megapixel photos)</li>
     *   <li>Min Circularity: 0.0 (permissive, backward compatible with tests)</li>
     *   <li>Detect Dark: false (bright objects default)</li>
     *   <li>Gaussian Blur: enabled ($\sigma = 1.5$)</li>
     * </ul>
     */
    public BirdDetector() {
        this.threshold       = 128;
        this.minArea         = 80;
        this.maxArea         = 500000;
        this.minCircularity  = 0.0;
        this.maxCircularity  = 1.0;
        this.detectDark      = false;
        this.useGaussianBlur = true;
        this.blurSigma       = 1.5;
    }

    /**
     * Creates a {@code BirdDetector} with custom threshold and area limits.
     *
     * @param threshold pixel intensity threshold (0-255)
     * @param minArea   minimum region area in pixels
     * @param maxArea   maximum region area in pixels
     */
    public BirdDetector(int threshold, int minArea, int maxArea) {
        this();
        this.threshold = threshold;
        this.minArea   = minArea;
        this.maxArea   = maxArea;
    }

    /**
     * Creates a {@code BirdDetector} with full manual control over segmentation parameters.
     *
     * @param threshold       pixel intensity threshold (0-255)
     * @param minArea         minimum region area in pixels
     * @param maxArea         maximum region area in pixels
     * @param detectDark      true if birds are darker than the background (silhouettes)
     * @param useGaussianBlur true to apply spatial Gaussian smoothing before thresholding
     */
    public BirdDetector(int threshold, int minArea, int maxArea, boolean detectDark, boolean useGaussianBlur) {
        this.threshold       = threshold;
        this.minArea         = minArea;
        this.maxArea         = maxArea;
        this.minCircularity  = 0.0;
        this.maxCircularity  = 1.0;
        this.detectDark      = detectDark;
        this.useGaussianBlur = useGaussianBlur;
        this.blurSigma       = 1.5;
    }

    // --- Parameter Accessors and Mutators ---

    public int getThreshold()                     { return threshold; }
    public void setThreshold(int t)               { this.threshold = Math.max(0, Math.min(255, t)); }
    public int getMinArea()                       { return minArea; }
    public void setMinArea(int a)                 { this.minArea = Math.max(1, a); }
    public int getMaxArea()                       { return maxArea; }
    public void setMaxArea(int a)                 { this.maxArea = Math.max(minArea, a); }
    public double getMinCircularity()             { return minCircularity; }
    public void setMinCircularity(double c)       { this.minCircularity = Math.max(0.0, Math.min(1.0, c)); }
    public double getMaxCircularity()             { return maxCircularity; }
    public void setMaxCircularity(double c)       { this.maxCircularity = Math.max(0.0, Math.min(1.0, c)); }
    public boolean isDetectDark()                 { return detectDark; }
    public void setDetectDark(boolean d)          { this.detectDark = d; }
    public boolean isUseGaussianBlur()            { return useGaussianBlur; }
    public void setUseGaussianBlur(boolean b)     { this.useGaussianBlur = b; }
    public double getBlurSigma()                  { return blurSigma; }
    public void setBlurSigma(double s)            { this.blurSigma = Math.max(0.1, s); }

    /**
     * Computes the recommended intensity threshold for an image using Otsu's clustering method.
     *
     * <p>Maximizes the between-class variance of the gray-level histogram (Burger &amp; Burge Ch. 10/11)
     * after applying Gaussian blur if enabled.</p>
     *
     * @param source the input image to evaluate
     * @return recommended threshold value in the range [0, 255]
     */
    public int calculateAutoThreshold(ImagePlus source) {
        if (source == null) return 128;
        ImageProcessor proc = source.getProcessor().duplicate().convertToByteProcessor();
        if (useGaussianBlur && proc.getWidth() > 30 && proc.getHeight() > 30) {
            proc.blurGaussian(blurSigma);
        }
        return proc.getAutoThreshold();
    }

    /**
     * Generates a binary mask image showing the thresholded foreground regions.
     *
     * <p>Useful for visual inspection and debugging: foreground pixels are white (255)
     * and background pixels are black (0).</p>
     *
     * @param source the input image
     * @return a new {@link ImagePlus} containing the thresholded binary mask
     */
    public ImagePlus createBinaryMask(ImagePlus source) {
        if (source == null) return null;
        ImagePlus copy = source.duplicate();
        ImageProcessor proc = copy.getProcessor().convertToByteProcessor();
        if (useGaussianBlur && proc.getWidth() > 30 && proc.getHeight() > 30) {
            proc.blurGaussian(blurSigma);
        }
        applyThreshold(proc, threshold, detectDark);
        return new ImagePlus("Binary Mask", proc);
    }

    /**
     * Executes the detection pipeline on the provided image and returns all localized candidates.
     *
     * @param source input image (color or grayscale)
     * @return list of localized {@link BirdCandidate} objects
     * @throws IllegalArgumentException if {@code source} is null
     */
    public List<BirdCandidate> detect(ImagePlus source) {
        if (source == null) {
            throw new IllegalArgumentException("Source image must not be null.");
        }

        // Step 1: Work on an isolated copy to prevent mutating the user's original image
        ImagePlus workCopy = source.duplicate();

        // Step 2: Convert to 8-bit grayscale using luminance weighting
        ImageProcessor proc = workCopy.getProcessor().convertToByteProcessor();

        // Step 3: Gaussian blur smoothing (suppresses fine leaf/twig texture noise)
        // Guard check prevents blurring tiny synthetic test images (e.g. 10x10)
        if (useGaussianBlur && proc.getWidth() > 30 && proc.getHeight() > 30) {
            proc.blurGaussian(blurSigma);
        }

        // Step 4: Apply intensity thresholding with polarity support
        applyThreshold(proc, threshold, detectDark);

        // Step 5: CRITICAL ImageJ State Registration
        // Inform ImageProcessor that foreground particle intensity range is [255, 255]
        proc.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
        workCopy.setProcessor(proc);

        // Step 6: Run ImageJ Particle Analyzer with Area and Circularity constraints
        ResultsTable results = new ResultsTable();
        int options = ParticleAnalyzer.CLEAR_WORKSHEET;
        int measurements = Measurements.AREA | Measurements.RECT | Measurements.CIRCULARITY;

        ParticleAnalyzer analyzer = new ParticleAnalyzer(
            options, measurements, results, minArea, maxArea, minCircularity, maxCircularity
        );
        analyzer.analyze(workCopy);

        // Step 7: Transform ResultsTable records into domain BirdCandidate objects
        return buildCandidates(results);
    }

    /**
     * Applies binary thresholding in place to an 8-bit image processor.
     *
     * @param proc        the 8-bit image processor to modify
     * @param t           the intensity threshold (0-255)
     * @param darkObjects true if dark pixels are foreground, false if bright pixels are foreground
     */
    private void applyThreshold(ImageProcessor proc, int t, boolean darkObjects) {
        int width  = proc.getWidth();
        int height = proc.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = proc.getPixel(x, y) & 0xFF;
                boolean isForeground = darkObjects ? (pixel < t) : (pixel > t);
                proc.putPixel(x, y, isForeground ? 255 : 0);
            }
        }
    }

    /**
     * Converts results from ImageJ's {@link ResultsTable} into structured {@link BirdCandidate} instances.
     *
     * @param results the particle analyzer measurement table
     * @return list of candidates with 1-based sequential indices
     */
    private List<BirdCandidate> buildCandidates(ResultsTable results) {
        List<BirdCandidate> candidates = new ArrayList<>();
        int rowCount = results.size();

        for (int i = 0; i < rowCount; i++) {
            int area = (int) results.getValue("Area", i);
            int bx   = (int) results.getValue("BX",   i);
            int by   = (int) results.getValue("BY",   i);
            int bw   = (int) results.getValue("Width", i);
            int bh   = (int) results.getValue("Height", i);

            Rectangle bounds = new Rectangle(bx, by, bw, bh);
            candidates.add(new BirdCandidate(i + 1, bounds, area));
        }

        return candidates;
    }
}
