# Counting Birds in the Garden

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![ImageJ](https://img.shields.io/badge/ImageJ-1.54f-blue.svg)](https://imagej.net/)
[![Build](https://img.shields.io/badge/Build-Maven%203.9-green.svg)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/JUnit%205-32%20Passed-brightgreen.svg)](https://junit.org/junit5/)
[![Course](https://img.shields.io/badge/Course-CSC%20360%20(CGIP)-purple.svg)]()

An image processing application developed for the **Computer Graphics and Image Processing (CSC 360)** undergraduate course. 

The application loads garden photography (PNG / JPG), segments bird candidates using classical digital image processing techniques based on the **Burger & Burge (2016)** textbook, draws bounding boxes around detected birds, and reports the candidate count in an interactive desktop GUI.

---

## Key Features

- **Robust Candidate Detection:** Multi-stage computer vision pipeline combining luminance reduction, spatial smoothing, intensity thresholding, and 4-connected component analysis.
- **Dual-Polarity Segmentation:** Supports both **Dark Birds on Bright Backgrounds** (daylight sky silhouettes) and **Bright Birds on Dark Foliage** (shaded garden canopy).
- **Automated Otsu Thresholding:** Automatically evaluates image histograms to compute optimal between-class variance thresholds ($T$).
- **Foliage & Texture Noise Suppression:** Pre-processing Gaussian spatial filter ($\sigma = 1.5$) eliminates high-frequency noise from leaves, grass, and bark.
- **Morphological Shape & Circularity Filtering:** Filters out long skinny twigs, wires, and fence lines using compactness metrics ($C = \frac{4\pi \cdot \text{Area}}{\text{Perimeter}^2}$).
- **Interactive Swing GUI:** 
  - Dual comparative canvas: Original Image vs Detection Result.
  - Live Threshold Slider ($0 - 255$) and One-Click "⚡ Auto Threshold (Otsu)" button.
  - Real-time Binary Mask (B&W) preview for visual algorithmic verification.
  - Interactive Min-Size area spinner to adjust noise rejection.
- **Lossless PNG Export:** Save the annotated detection output directly to disk.
- **Automated Test Suite:** 32 comprehensive JUnit 5 unit tests covering algorithms, edge cases, invariants, and format utilities.

---

## Textbook & Theoretical Alignment

This implementation directly applies theoretical principles from **Wilhelm Burger & Mark J. Burge (2016)**, *Digital Image Processing: An Algorithmic Introduction Using Java*:

| Pipeline Stage | Textbook Chapter | Theoretical Principle / Formulation |
|---|---|---|
| **Luminance Conversion** | Chapter 14 (*Color Images*) | $Y = 0.299R + 0.587G + 0.114B$ (CIE spectral sensitivity) |
| **Gaussian Pre-Filtering** | Chapters 4 & 5 (*Spatial Filters*) | 2D Gaussian convolution to suppress foliage high-frequency noise |
| **Otsu Thresholding** | Chapters 10 & 11 (*Thresholding*) | Maximization of between-class variance: $\sigma_B^2(t) = \omega_0(t)\omega_1(t)[\mu_0(t) - \mu_1(t)]^2$ |
| **Region Labeling** | Chapter 10 (*Region Labeling*) | 4-connectivity flood fill equivalence clustering via ImageJ `ParticleAnalyzer` |
| **Morphological Descriptors** | Chapter 13 (*Shape Properties*) | 0th/1st order spatial moments (Area, Centroid, Bounding Box) and Compactness |

---

## Project Structure

```
endsem_project/
├── pom.xml                               # Maven project configuration and dependencies
├── .gitignore                            # Git exclusion rules (ignores build targets and IDE files)
├── README.md                             # Comprehensive project documentation
├── mvnw.cmd                              # Maven Wrapper script (Windows)
├── run.bat                               # One-click desktop launcher
│
└── src/
    ├── main/java/
    │   ├── Main.java                     # Application bootstrap and EDT schedule
    │   ├── detection/
    │   │   ├── BirdCandidate.java        # Immutable domain value object (ID, bounds, area)
    │   │   ├── BirdCounter.java          # Aggregation service and Java2D annotation overlay
    │   │   └── BirdDetector.java         # Core 5-stage image segmentation and Otsu engine
    │   ├── ui/
    │   │   ├── BirdCountingGUI.java      # Swing window, event handling, and parameter tuning
    │   │   └── ImagePanel.java           # Proportional aspect-ratio canvas with bilinear scaling
    │   └── utils/
    │       └── ImageUtils.java           # File I/O bridge between Java ImageIO and ImageJ
    │
    └── test/java/
        ├── BirdCandidateTest.java        # Invariant and defensive copy tests
        ├── BirdCounterTest.java          # Annotation and null-safety unit tests (10 tests)
        ├── BirdDetectorTest.java         # Segmentation, Otsu, blur, and circularity tests (16 tests)
        └── ImageUtilsTest.java           # File loading, saving, and conversion tests (6 tests)
```

---

## Test Images: Structure & Specifications

The project is designed to evaluate garden and outdoor avian imagery:

- **Supported File Formats:** PNG, JPG, JPEG, BMP (RGB or Grayscale).
- **Recommended Resolutions:** 
  - Standard Test Resolution: $640 \times 480$ up to $1920 \times 1080$ pixels.
  - High-Resolution Support: Handles multi-megapixel camera imagery (up to 24 MP) via dynamic area bounds.
- **Lighting & Exposure Profiles:**
  - **Sky & Daylight Silhouettes (e.g. `bird2.jpg`):** High background luminance ($> 200/255$). Set **Bird Tone** to `Dark birds (sky / bright BG)` with Otsu threshold ($\approx 190$).
  - **Canopy & Shade (e.g. `bird1.jpg`):** Medium background luminance. Set **Bird Tone** to `Dark birds` with threshold $\approx 145$ and Min-Size $\approx 600\text{ px}$.
  - **White Plumage / Dark Background:** Set **Bird Tone** to `Bright birds (dark / shade BG)`.

---

## Building and Running

### Prerequisites
- **Java 21 LTS** (or Java 17+)
- ImageJ dependency (`net.imagej:ij:1.54f`) resolved via Maven or local `.m2` repository

### 1. Run Directly (Fastest)
From the project folder:
```powershell
.\run.bat
```
*Or execute with Java:*
```powershell
java -cp "target\classes;C:\Users\HP\.m2\repository\net\imagej\ij\1.54f\ij-1.54f.jar" Main
```

### 2. Build with Maven
```bash
# Windows
mvnw.cmd clean package

# Linux / macOS
./mvnw clean package
```

### 3. Run Automated Tests
```powershell
# Compiles and runs all 32 automated JUnit 5 tests
mvn test
```

---

## GUI Usage Guide

1. Click **"📂 Upload Image"** to open any garden photograph.
2. The optimal **Otsu Threshold** is calculated and applied automatically.
3. Choose the appropriate **Bird Tone**:
   - `Dark birds (sky / bright BG)` for birds against daylight or sky.
   - `Bright birds (dark / shade BG)` for light birds in shaded foliage.
4. Fine-tune parameters using the sidebar:
   - **Threshold Slider ($0 - 255$):** Adjust sensitivity to expand or contract candidate boundaries.
   - **Smooth foliage noise (Blur):** Keeps Gaussian pre-filtering active to ignore leaf texture.
   - **Ignore skinny twigs (Circularity):** Rejects thin lines and branches ($C \ge 0.15$).
   - **Show Binary Mask (B&W):** Preview the segmented black-and-white mask directly on screen.
   - **Min Size (pixels):** Adjust minimum area cutoff (e.g., $300 - 1000\text{ px}$) to eliminate debris.
5. Click **"💾 Save Output"** to export the annotated detection result as a PNG.

---

## Changelog

### Version 1.1.0 (`updateversion1`)
- **CRITICAL FIX:** Resolved `ParticleAnalyzer` background inversion bug by registering explicit threshold state `proc.setThreshold(255, 255)`.
- **Dual-Polarity Mode:** Added support for dark silhouettes against daylight skies (`detectDark`).
- **Otsu Algorithm:** Integrated automatic histogram between-class variance maximization (`calculateAutoThreshold`).
- **Gaussian Pre-Filtering:** Added spatial smoothing ($\sigma = 1.5$) to remove foliage texture noise.
- **Circularity Filtering:** Implemented compactness shape descriptor ($C \ge 0.15$) to ignore linear twigs and fences.
- **UI Enhancements:** Added live threshold slider, Otsu button, binary mask toggle, and area spinner.
- **Windows L&F Fix:** Added `swing.disableWin32ShellFolder` property to eliminate Windows COM shell deadlocks.
- **Extended Test Suite:** Expanded unit test coverage to 32 passing tests.

### Version 1.0.0
- Initial release with baseline static thresholding ($T=128$), ImageJ `ParticleAnalyzer` integration, two-panel Swing viewer, and 22 tests.
