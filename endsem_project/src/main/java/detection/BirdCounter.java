package detection;

import ij.ImagePlus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  detection (Visual Annotation & Aggregation)
 * FILE:    BirdCounter.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/bounding-box-overlay)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial implementation of count tally and Java2D box rendering.
 *     - v1.1.0: Added translucent text backplate for improved label legibility,
 *               enhanced null-safety, and added Java2D anti-aliasing hints.
 * =============================================================================
 * DESCRIPTION:
 *   Aggregates detected bird candidate counts and generates an annotated output
 *   image displaying vibrant green bounding boxes (stroke=2px) and high-contrast
 *   "Bird N" tags over an RGB copy of the original source frame.
 * =============================================================================
 */

/**
 * Counts bird candidates and draws labelled bounding boxes on the output image.
 *
 * <p>This class implements two core responsibilities in the pipeline:
 * <ol>
 *   <li><b>Count Aggregation:</b> Safely reports the total number of candidate regions.</li>
 *   <li><b>Visual Annotation:</b> Overlays green rectangular contours and header badges
 *       onto an isolated copy of the original photograph without mutating source buffers.</li>
 * </ol>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public class BirdCounter {

    /** Stroke color for the candidate bounding box (vibrant emerald green). */
    private static final Color BOX_COLOR = new Color(0, 220, 60);

    /** Text color for the "Bird N" header tag. */
    private static final Color LABEL_COLOR = Color.WHITE;

    /** Background fill color for the label tag (translucent black for contrast). */
    private static final Color TAG_BG_COLOR = new Color(0, 0, 0, 165);

    /** Default font size for candidate labels in points. */
    private static final int FONT_SIZE = 13;

    /**
     * Default constructor.
     */
    public BirdCounter() {}

    /**
     * Returns the total count of candidates in the provided list.
     *
     * @param candidates list produced by {@link BirdDetector} (may be null or empty)
     * @return non-negative candidate count (0 if list is null or empty)
     */
    public int count(List<BirdCandidate> candidates) {
        return (candidates == null) ? 0 : candidates.size();
    }

    /**
     * Draws green bounding boxes and "Bird N" labels onto an RGB copy of the given image.
     *
     * <p>The original image is never modified. An independent copy is created in
     * {@link BufferedImage#TYPE_INT_RGB} to guarantee that green outlines and white
     * labels render in full color even when the input source is 8-bit grayscale.</p>
     *
     * @param source     the original input image (color or grayscale)
     * @param candidates list of detected candidate regions to annotate
     * @return a new {@link ImagePlus} containing the original image with drawn annotations
     * @throws IllegalArgumentException if {@code source} is null
     */
    public ImagePlus drawDetections(ImagePlus source, List<BirdCandidate> candidates) {
        if (source == null) {
            throw new IllegalArgumentException("Source image must not be null.");
        }

        BufferedImage original  = source.getBufferedImage();
        BufferedImage annotated = toColorCopy(original);

        Graphics2D g = annotated.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, FONT_SIZE));

        if (candidates != null) {
            for (BirdCandidate candidate : candidates) {
                drawBox(g, candidate);
            }
        }

        g.dispose();

        return new ImagePlus("detection_output", annotated);
    }

    /**
     * Renders a single candidate's bounding rectangle and label tag.
     *
     * @param g         graphics context
     * @param candidate candidate containing boundaries and label
     */
    private void drawBox(Graphics2D g, BirdCandidate candidate) {
        Rectangle b = candidate.getBounds();

        // 1. Draw green rectangular contour
        g.setColor(BOX_COLOR);
        g.setStroke(new BasicStroke(2.0f));
        g.drawRect(b.x, b.y, b.width, b.height);

        // 2. Compute label coordinates (keep label within image boundaries)
        String label = candidate.getLabel();
        int labelY = Math.max(FONT_SIZE + 2, b.y - 3);

        // 3. Render dark backing badge behind the text for guaranteed contrast
        int textWidth = g.getFontMetrics().stringWidth(label);
        g.setColor(TAG_BG_COLOR);
        g.fillRect(b.x, labelY - FONT_SIZE + 1, textWidth + 6, FONT_SIZE + 3);

        // 4. Render label text in bold white
        g.setColor(LABEL_COLOR);
        g.drawString(label, b.x + 3, labelY);
    }

    /**
     * Converts any BufferedImage format into an RGB color image.
     *
     * @param src source image
     * @return a duplicate BufferedImage in {@link BufferedImage#TYPE_INT_RGB}
     */
    private BufferedImage toColorCopy(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }
}
