package ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  ui (High-Performance Image Canvas)
 * FILE:    ImagePanel.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/image-display-panel)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial proportional aspect-ratio scaling canvas.
 *     - v1.1.0: Added bilinear interpolation rendering hints for smooth
 *               downsampling of multi-megapixel garden photography.
 * =============================================================================
 * DESCRIPTION:
 *   Custom Swing component dedicated to rendering BufferedImage instances.
 *   Guarantees proportional aspect-ratio preservation, centered rendering,
 *   bilinear interpolation, and an informative empty placeholder state.
 * =============================================================================
 */

/**
 * Custom Swing component that displays an image scaled to fit its bounds while preserving aspect ratio.
 *
 * <p>Features:
 * <ul>
 *   <li>High-quality bilinear interpolation scaling via Java2D {@link RenderingHints}.</li>
 *   <li>Aspect-ratio preservation with automatic vertical and horizontal centering.</li>
 *   <li>Header banner displaying panel role (e.g., "Original Image", "Detection Output").</li>
 *   <li>Fallback "No image loaded" placeholder display when unpopulated.</li>
 * </ul>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public class ImagePanel extends JPanel {

    /** Current image buffer to render (null if empty). */
    private BufferedImage image;

    /** Title banner displayed at the top-left of the canvas. */
    private final String title;

    private static final Color BG_COLOR          = new Color(30, 30, 30);
    private static final Color TITLE_COLOR        = new Color(200, 200, 200);
    private static final Color PLACEHOLDER_COLOR  = new Color(110, 110, 110);
    private static final Color BORDER_COLOR       = new Color(60, 60, 60);

    /**
     * Constructs a new {@code ImagePanel}.
     *
     * @param title label displayed at the top of the canvas
     */
    public ImagePanel(String title) {
        this.title = title;
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        setPreferredSize(new Dimension(420, 420));
    }

    /**
     * Sets or replaces the displayed image and triggers an asynchronous repaint.
     *
     * @param image image to display (or null to clear)
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    /**
     * Clears the current image and renders the default placeholder state.
     */
    public void clearImage() {
        setImage(null);
    }

    /**
     * Paints the background, header title, and centered scaled image.
     *
     * @param g graphics rendering context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Render Header Title
        g2.setColor(TITLE_COLOR);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g2.drawString(title, 8, 16);

        // 2. Handle empty state
        if (image == null) {
            g2.setColor(PLACEHOLDER_COLOR);
            g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String msg = "No image loaded";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            return;
        }

        // 3. Calculate aspect-ratio scaling metrics
        int drawableH = h - 22; // reserve top 22 pixels for title
        double scaleX = (double) w / image.getWidth();
        double scaleY = (double) drawableH / image.getHeight();
        double scale  = Math.min(scaleX, scaleY);

        int drawW = (int) (image.getWidth() * scale);
        int drawH = (int) (image.getHeight() * scale);
        int drawX = (w - drawW) / 2;
        int drawY = 22 + (drawableH - drawH) / 2;

        // 4. Render scaled image
        g2.drawImage(image, drawX, drawY, drawW, drawH, null);
    }
}
