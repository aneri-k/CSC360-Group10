package utils;

import ij.ImagePlus;
import ij.io.FileSaver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  utils (I/O & Format Conversion Utilities)
 * FILE:    ImageUtils.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/image-loading)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial implementation of ImageIO reader and ImageJ FileSaver.
 *     - v1.1.0: Added explicit validation for null/empty files, detailed error
 *               diagnostics, and ImagePlus-to-BufferedImage conversion checks.
 * =============================================================================
 * DESCRIPTION:
 *   Static utility class providing robust image input/output handling.
 *   Bridges native Java {@link ImageIO} and ImageJ {@link FileSaver} to ensure
 *   lossless PNG/JPG loading, format conversion, and export.
 * =============================================================================
 */

/**
 * Static utility methods for image file I/O and format adaptation between Java and ImageJ.
 *
 * <p>Handles:
 * <ul>
 *   <li>File system image loading into ImageJ {@link ImagePlus} structures.</li>
 *   <li>Lossless PNG image export via ImageJ's {@link FileSaver}.</li>
 *   <li>Format conversion between {@link ImagePlus} and Swing-ready {@link BufferedImage}.</li>
 * </ul>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public final class ImageUtils {

    /** Private constructor to prevent instantiation of utility class. */
    private ImageUtils() {}

    /**
     * Loads an image file from disk and returns an ImageJ {@link ImagePlus} container.
     *
     * @param file the image file to load (PNG, JPG, BMP)
     * @return an {@link ImagePlus} containing the loaded pixel data
     * @throws IOException if the file does not exist, cannot be read, or is corrupt
     */
    public static ImagePlus loadImage(File file) throws IOException {
        if (file == null) {
            throw new IOException("Cannot load image: file reference is null.");
        }
        if (!file.exists()) {
            throw new IOException("Image file not found on disk: " + file.getAbsolutePath());
        }

        BufferedImage buffered = ImageIO.read(file);
        if (buffered == null) {
            throw new IOException("Unsupported or corrupt image format: " + file.getName());
        }

        return new ImagePlus(file.getName(), buffered);
    }

    /**
     * Saves an {@link ImagePlus} object to disk as a PNG file.
     *
     * @param image      the image to save
     * @param outputPath target file path on disk (should end with .png)
     * @throws IOException if saving fails or if image/path is invalid
     */
    public static void savePng(ImagePlus image, String outputPath) throws IOException {
        if (image == null) {
            throw new IOException("Cannot save image: ImagePlus reference is null.");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IOException("Cannot save image: output path is invalid.");
        }

        FileSaver saver = new FileSaver(image);
        boolean success = saver.saveAsPng(outputPath);
        if (!success) {
            throw new IOException("Failed to save image to disk at: " + outputPath);
        }
    }

    /**
     * Extracts a {@link BufferedImage} from an ImageJ {@link ImagePlus} for Swing rendering.
     *
     * @param imagePlus the ImageJ container
     * @return an equivalent {@link BufferedImage}
     * @throws IllegalArgumentException if {@code imagePlus} is null
     */
    public static BufferedImage toBufferedImage(ImagePlus imagePlus) {
        if (imagePlus == null) {
            throw new IllegalArgumentException("Cannot convert null ImagePlus to BufferedImage.");
        }
        return imagePlus.getBufferedImage();
    }
}
