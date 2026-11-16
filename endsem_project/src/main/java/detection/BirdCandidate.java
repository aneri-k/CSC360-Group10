package detection;

import java.awt.Rectangle;

/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  detection (Domain Value Object)
 * FILE:    BirdCandidate.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/candidate-domain-model)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial immutable representation of segmented regions.
 *     - v1.1.0: Added defensive cloning on bounds getter/constructor,
 *               extended JavaDoc with topological references (Burger Ch. 10).
 * =============================================================================
 * DESCRIPTION:
 *   Represents a single connected-component region identified by the detector.
 *   Encapsulates geographic bounding box (java.awt.Rectangle), pixel area,
 *   sequential identification index, and presentation label.
 * =============================================================================
 */

/**
 * Immutable domain representation of an identified bird candidate region.
 *
 * <p>A candidate corresponds to a connected component of foreground pixels
 * that satisfied user-configured spatial constraints (minimum/maximum area
 * and compactness/circularity). In the context of Burger &amp; Burge (2016,
 * Chapter 10), each candidate represents a distinct equivalence class
 * discovered via region labeling.</p>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public class BirdCandidate {

    /** Sequential 1-based index assigned to this candidate. */
    private final int id;

    /** Bounding rectangle coordinates in original image pixel space. */
    private final Rectangle bounds;

    /** Area in pixels (number of foreground pixels in this connected component). */
    private final int area;

    /**
     * Constructs a new immutable {@code BirdCandidate}.
     *
     * @param id      sequential identifier starting from 1
     * @param bounds  bounding box rectangle enclosing the connected component
     * @param area    total number of foreground pixels belonging to the region
     * @throws IllegalArgumentException if {@code bounds} is null or {@code area} is negative
     */
    public BirdCandidate(int id, Rectangle bounds, int area) {
        if (bounds == null) {
            throw new IllegalArgumentException("Bounding rectangle cannot be null.");
        }
        if (area < 0) {
            throw new IllegalArgumentException("Candidate area cannot be negative: " + area);
        }
        this.id     = id;
        this.bounds = new Rectangle(bounds); // defensive copy to preserve immutability
        this.area   = area;
    }

    /**
     * Returns the 1-based sequential identifier for this candidate.
     *
     * @return candidate index
     */
    public int getId() {
        return id;
    }

    /**
     * Returns a defensive copy of the bounding rectangle enclosing this candidate.
     *
     * @return a new {@link Rectangle} representing the boundary box
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    /**
     * Returns the pixel area of this candidate region.
     *
     * @return number of foreground pixels
     */
    public int getArea() {
        return area;
    }

    /**
     * Returns the formatted presentation label displayed above the bounding box.
     *
     * @return label string (e.g. "Bird 1", "Bird 2")
     */
    public String getLabel() {
        return "Bird " + id;
    }

    /**
     * Returns a string summary of this candidate's attributes.
     *
     * @return debug representation of candidate
     */
    @Override
    public String toString() {
        return "BirdCandidate{id=" + id + ", area=" + area + ", bounds=[x="
            + bounds.x + ", y=" + bounds.y + ", w=" + bounds.width + ", h=" + bounds.height + "]}";
    }
}
