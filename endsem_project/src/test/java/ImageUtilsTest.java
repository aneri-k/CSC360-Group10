/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  test (Automated Unit Testing Suite)
 * FILE:    ImageUtilsTest.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/tests)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.1.0: Initial unit test suite for ImageUtils validation and format
 *               conversion routines.
 * =============================================================================
 */

import ij.ImagePlus;
import ij.process.ByteProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ImageUtils}.
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
class ImageUtilsTest {

    @Test
    @DisplayName("loadImage with null file throws IOException")
    void loadImageNullThrows() {
        assertThrows(IOException.class, () -> ImageUtils.loadImage(null));
    }

    @Test
    @DisplayName("loadImage with non-existent file throws IOException")
    void loadImageMissingThrows() {
        File missing = new File("non_existent_file_xyz_123.png");
        assertThrows(IOException.class, () -> ImageUtils.loadImage(missing));
    }

    @Test
    @DisplayName("toBufferedImage with null ImagePlus throws IllegalArgumentException")
    void toBufferedImageNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> ImageUtils.toBufferedImage(null));
    }

    @Test
    @DisplayName("toBufferedImage converts ImagePlus to valid BufferedImage")
    void toBufferedImageValid() {
        ByteProcessor bp = new ByteProcessor(20, 20);
        ImagePlus img = new ImagePlus("test", bp);

        BufferedImage bImg = ImageUtils.toBufferedImage(img);
        assertNotNull(bImg, "Resulting BufferedImage must not be null");
        assertEquals(20, bImg.getWidth(), "Width must match");
        assertEquals(20, bImg.getHeight(), "Height must match");
    }

    @Test
    @DisplayName("savePng with null image throws IOException")
    void savePngNullImageThrows() {
        assertThrows(IOException.class, () -> ImageUtils.savePng(null, "output.png"));
    }

    @Test
    @DisplayName("savePng with invalid path throws IOException")
    void savePngInvalidPathThrows() {
        ByteProcessor bp = new ByteProcessor(10, 10);
        ImagePlus img = new ImagePlus("test", bp);
        assertThrows(IOException.class, () -> ImageUtils.savePng(img, ""));
    }
}

