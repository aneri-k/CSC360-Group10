package ui;

import detection.BirdCandidate;
import detection.BirdCounter;
import detection.BirdDetector;
import ij.ImagePlus;
import utils.ImageUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  ui (Desktop Swing Application Interface)
 * FILE:    BirdCountingGUI.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/gui & feature/interactive-tuning-controls)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial two-panel GUI (original and detection output) with Upload,
 *               Detect, and Save Output buttons.
 *     - v1.1.0: Added interactive detection tuning sidebar (polarity dropdown,
 *               live threshold slider, Auto Otsu button, blur checkbox, circularity
 *               shape filter, binary mask toggle, and min-area size spinner).
 *               Added safe default directory to JFileChooser to eliminate Windows
 *               COM ShellFolder deadlocks.
 * =============================================================================
 * DESCRIPTION:
 *   Main Swing window for the bird counting system. Provides side-by-side
 *   visual comparison of the original image and annotated detection results,
 *   coupled with real-time controls for tuning segmentation parameters.
 * =============================================================================
 */

/**
 * Main application window and user interface controller for bird counting.
 *
 * <p>Layout Architecture:
 * <ul>
 *   <li><b>West (Sidebar):</b> Action triggers (Upload, Detect, Save) and interactive
 *       detection tuning controls (Tone, Threshold slider, Otsu auto, Blur, Circularity, Mask toggle).</li>
 *   <li><b>Center:</b> Dual comparative image canvas showing the original image and
 *       the detection overlay (or threshold binary mask) side-by-side.</li>
 *   <li><b>South:</b> Status bar providing real-time operational feedback.</li>
 * </ul>
 *
 * <p>All file I/O and computer vision workloads are delegated to non-blocking
 * {@link SwingWorker} background threads to guarantee a smooth and responsive interface.</p>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public class BirdCountingGUI extends JFrame {

    // ---- Application State ----

    /** Original image loaded from disk. */
    private ImagePlus loadedImage;

    /** Annotated image with green bounding boxes and labels drawn. */
    private ImagePlus detectionImage;

    /** Binary threshold mask image (black and white) for visual debugging. */
    private ImagePlus maskImage;

    /** File handle of the currently opened image (used for saving next to source). */
    private File imageFile;

    /** Core image processing and detection engine. */
    private final BirdDetector detector = new BirdDetector();

    /** Annotation and tallying service. */
    private final BirdCounter counter = new BirdCounter();

    // ---- Action Buttons ----
    private JButton btnUpload;
    private JButton btnDetect;
    private JButton btnSave;

    // ---- Interactive Tuning Controls ----
    private JComboBox<String> cmbPolarity;
    private JSlider           sliderThreshold;
    private JLabel            lblThresholdVal;
    private JButton           btnAutoOtsu;
    private JCheckBox         chkBlur;
    private JCheckBox         chkShapeFilter;
    private JCheckBox         chkShowMask;
    private JSpinner          spnMinArea;

    // ---- Image Display Panels ----
    private ImagePanel originalPanel;
    private ImagePanel detectionPanel;

    // ---- Information Feedback Labels ----
    private JLabel countLabel;
    private JLabel statusLabel;

    /**
     * Initializes the desktop window, constructs UI components, and centers on screen.
     */
    public BirdCountingGUI() {
        super("Counting Birds in the Garden");
        buildUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null); // center on physical screen
        setVisible(true);
    }

    /**
     * Constructs the primary border layout and adds children panels.
     */
    private void buildUI() {
        setLayout(new BorderLayout(6, 6));

        // --- Left Control Sidebar ---
        add(buildControlPanel(), BorderLayout.WEST);

        // --- Center Comparative Panels ---
        JPanel imageArea = new JPanel(new GridLayout(1, 2, 8, 8));
        imageArea.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 6));
        originalPanel  = new ImagePanel("Original Image");
        detectionPanel = new ImagePanel("Detection Output");
        imageArea.add(originalPanel);
        imageArea.add(detectionPanel);
        add(imageArea, BorderLayout.CENTER);

        // --- Bottom Status Bar ---
        statusLabel = new JLabel("  Upload a garden image to start.");
        statusLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        add(statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Constructs the left sidebar containing action buttons, tuning parameters, and counters.
     *
     * @return populated JPanel sidebar
     */
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(250, 0));

        // --- Actions Group ---
        btnUpload = new JButton("📂  Upload Image");
        btnDetect = new JButton("🔍  Detect Birds");
        btnSave   = new JButton("💾  Save Output");

        btnDetect.setEnabled(false);
        btnSave  .setEnabled(false);

        Dimension btnSize = new Dimension(225, 34);
        btnUpload.setMaximumSize(btnSize);
        btnDetect.setMaximumSize(btnSize);
        btnSave  .setMaximumSize(btnSize);

        JPanel btnGroup = new JPanel();
        btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
        btnGroup.setBorder(new TitledBorder("Actions"));
        btnGroup.add(btnUpload);
        btnGroup.add(Box.createVerticalStrut(6));
        btnGroup.add(btnDetect);
        btnGroup.add(Box.createVerticalStrut(6));
        btnGroup.add(btnSave);
        btnGroup.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Settings Group ---
        JPanel settingsGroup = new JPanel();
        settingsGroup.setLayout(new BoxLayout(settingsGroup, BoxLayout.Y_AXIS));
        settingsGroup.setBorder(new TitledBorder("Detection Tuning"));
        settingsGroup.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 1. Bird Tone / Polarity
        JLabel lblTone = new JLabel("Bird Tone:");
        lblTone.setAlignmentX(Component.LEFT_ALIGNMENT);
        String[] toneOptions = {
            "Dark birds (sky / bright BG)",
            "Bright birds (dark / shade BG)"
        };
        cmbPolarity = new JComboBox<>(toneOptions);
        cmbPolarity.setMaximumSize(new Dimension(225, 26));
        cmbPolarity.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbPolarity.setSelectedIndex(0);
        detector.setDetectDark(true);

        // 2. Threshold Slider
        lblThresholdVal = new JLabel("Threshold: " + detector.getThreshold());
        lblThresholdVal.setAlignmentX(Component.LEFT_ALIGNMENT);

        sliderThreshold = new JSlider(0, 255, detector.getThreshold());
        sliderThreshold.setMaximumSize(new Dimension(225, 30));
        sliderThreshold.setAlignmentX(Component.LEFT_ALIGNMENT);
        sliderThreshold.addChangeListener(e -> {
            int val = sliderThreshold.getValue();
            detector.setThreshold(val);
            lblThresholdVal.setText("Threshold: " + val);
        });

        // 3. Auto Otsu Button
        btnAutoOtsu = new JButton("⚡ Auto Threshold (Otsu)");
        btnAutoOtsu.setMaximumSize(new Dimension(225, 28));
        btnAutoOtsu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAutoOtsu.setEnabled(false);

        // 4. Noise and Shape Filters
        chkBlur = new JCheckBox("Smooth foliage noise (Blur)", true);
        chkBlur.setAlignmentX(Component.LEFT_ALIGNMENT);
        detector.setUseGaussianBlur(true);

        chkShapeFilter = new JCheckBox("Ignore skinny twigs (Circularity)", true);
        chkShapeFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        detector.setMinCircularity(0.15);

        chkShowMask = new JCheckBox("Show Binary Mask (B&W)", false);
        chkShowMask.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 5. Min Area Filter
        JLabel lblMinArea = new JLabel("Min Size (pixels):");
        lblMinArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        detector.setMinArea(300); // 300px default eliminates leaf specks
        spnMinArea = new JSpinner(new SpinnerNumberModel(300, 10, 50000, 50));
        spnMinArea.setMaximumSize(new Dimension(225, 26));
        spnMinArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        spnMinArea.addChangeListener(e -> {
            int val = (Integer) spnMinArea.getValue();
            detector.setMinArea(val);
        });

        // Assemble settings inside settings container
        settingsGroup.add(lblTone);
        settingsGroup.add(Box.createVerticalStrut(3));
        settingsGroup.add(cmbPolarity);
        settingsGroup.add(Box.createVerticalStrut(8));
        settingsGroup.add(lblThresholdVal);
        settingsGroup.add(sliderThreshold);
        settingsGroup.add(Box.createVerticalStrut(4));
        settingsGroup.add(btnAutoOtsu);
        settingsGroup.add(Box.createVerticalStrut(8));
        settingsGroup.add(chkBlur);
        settingsGroup.add(Box.createVerticalStrut(4));
        settingsGroup.add(chkShapeFilter);
        settingsGroup.add(Box.createVerticalStrut(4));
        settingsGroup.add(chkShowMask);
        settingsGroup.add(Box.createVerticalStrut(8));
        settingsGroup.add(lblMinArea);
        settingsGroup.add(Box.createVerticalStrut(3));
        settingsGroup.add(spnMinArea);

        // Candidate Count Display
        countLabel = new JLabel("Birds found:  —");
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD, 15f));
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble left sidebar
        panel.add(btnGroup);
        panel.add(Box.createVerticalStrut(10));
        panel.add(settingsGroup);
        panel.add(Box.createVerticalStrut(12));
        panel.add(countLabel);
        panel.add(Box.createVerticalGlue());

        // Wire event listeners
        btnUpload.addActionListener(e -> doUpload());
        btnDetect.addActionListener(e -> doDetect());
        btnSave  .addActionListener(e -> doSave());

        cmbPolarity.addActionListener(e -> {
            boolean dark = (cmbPolarity.getSelectedIndex() == 0);
            detector.setDetectDark(dark);
            if (loadedImage != null) doDetect();
        });

        btnAutoOtsu.addActionListener(e -> {
            if (loadedImage != null) {
                int autoT = detector.calculateAutoThreshold(loadedImage);
                sliderThreshold.setValue(autoT);
                setStatus("Auto-calculated Otsu threshold: " + autoT);
                doDetect();
            }
        });

        chkBlur.addActionListener(e -> {
            detector.setUseGaussianBlur(chkBlur.isSelected());
            if (loadedImage != null) doDetect();
        });

        chkShapeFilter.addActionListener(e -> {
            detector.setMinCircularity(chkShapeFilter.isSelected() ? 0.15 : 0.0);
            if (loadedImage != null) doDetect();
        });

        chkShowMask.addActionListener(e -> updateDisplayPanel());

        return panel;
    }

    /**
     * Opens a file chooser dialog and reads the selected image asynchronously.
     */
    private void doUpload() {
        File initialDir = new File("../imaghe");
        if (!initialDir.exists()) initialDir = new File(".");
        JFileChooser chooser = new JFileChooser(initialDir);
        chooser.setDialogTitle("Select a Garden Image");
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Image files (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File chosen = chooser.getSelectedFile();
        setStatus("Loading " + chosen.getName() + " …");

        new SwingWorker<ImagePlus, Void>() {
            @Override
            protected ImagePlus doInBackground() throws Exception {
                return ImageUtils.loadImage(chosen);
            }
            @Override
            protected void done() {
                try {
                    loadedImage    = get();
                    imageFile      = chosen;
                    detectionImage = null;
                    maskImage      = null;

                    originalPanel .setImage(ImageUtils.toBufferedImage(loadedImage));
                    detectionPanel.clearImage();
                    countLabel.setText("Birds found:  —");

                    btnDetect.setEnabled(true);
                    btnSave  .setEnabled(false);
                    btnAutoOtsu.setEnabled(true);

                    // Compute Otsu threshold for the loaded image
                    int otsu = detector.calculateAutoThreshold(loadedImage);
                    sliderThreshold.setValue(otsu);

                    setStatus("Loaded: " + chosen.getName()
                        + "  (" + loadedImage.getWidth() + " × " + loadedImage.getHeight() + " px)"
                        + " — Suggested Threshold: " + otsu);

                    // Trigger detection immediately for fluid interaction
                    doDetect();
                } catch (Exception ex) {
                    showError("Could not load image", ex.getMessage());
                    setStatus("Error loading image.");
                }
            }
        }.execute();
    }

    /**
     * Executes the detection algorithm asynchronously and updates panels with annotations.
     */
    private void doDetect() {
        if (loadedImage == null) return;
        setStatus("Detecting birds …");
        btnDetect.setEnabled(false);

        // Synchronize UI parameters to detector instance
        detector.setThreshold(sliderThreshold.getValue());
        detector.setDetectDark(cmbPolarity.getSelectedIndex() == 0);
        detector.setUseGaussianBlur(chkBlur.isSelected());
        detector.setMinCircularity(chkShapeFilter.isSelected() ? 0.15 : 0.0);
        detector.setMinArea((Integer) spnMinArea.getValue());

        new SwingWorker<Void, Void>() {
            List<BirdCandidate> candidates;

            @Override
            protected Void doInBackground() {
                candidates     = detector.detect(loadedImage);
                detectionImage = counter.drawDetections(loadedImage, candidates);
                maskImage      = detector.createBinaryMask(loadedImage);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();

                    int found = counter.count(candidates);
                    updateDisplayPanel();
                    countLabel.setText("Birds found:  " + found);

                    btnDetect.setEnabled(true);
                    btnSave  .setEnabled(true);
                    setStatus("Complete: " + found + " candidate(s) found (Threshold: "
                        + detector.getThreshold() + ", MinSize: " + detector.getMinArea() + "px).");
                } catch (Exception ex) {
                    showError("Detection failed", ex.getMessage());
                    btnDetect.setEnabled(true);
                    setStatus("Detection error.");
                }
            }
        }.execute();
    }

    /**
     * Switches detection panel rendering between annotated detections and the threshold binary mask.
     */
    private void updateDisplayPanel() {
        if (chkShowMask.isSelected() && maskImage != null) {
            detectionPanel.setImage(ImageUtils.toBufferedImage(maskImage));
        } else if (detectionImage != null) {
            detectionPanel.setImage(ImageUtils.toBufferedImage(detectionImage));
        }
    }

    /**
     * Opens a save dialog and exports the annotated detection image to PNG.
     */
    private void doSave() {
        if (detectionImage == null) return;

        File saveDir = (imageFile != null) ? imageFile.getParentFile() : new File(".");
        JFileChooser chooser = new JFileChooser(saveDir);
        chooser.setDialogTitle("Save Detection Output");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG image", "png"));

        if (imageFile != null) {
            chooser.setSelectedFile(new File(imageFile.getParentFile(), "detection_output.png"));
        }

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dest = chooser.getSelectedFile();
        String path = dest.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".png")) path += ".png";
        final String finalPath = path;

        setStatus("Saving …");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ImageUtils.savePng(detectionImage, finalPath);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Saved: " + finalPath);
                    JOptionPane.showMessageDialog(BirdCountingGUI.this,
                        "Output saved to:\n" + finalPath,
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    showError("Save failed", ex.getMessage());
                    setStatus("Save error.");
                }
            }
        }.execute();
    }

    /**
     * Helper to set status message safely on the Swing EDT.
     *
     * @param msg status text
     */
    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText("  " + msg));
    }

    /**
     * Helper to display an error dialog safely on the Swing EDT.
     *
     * @param title  dialog title
     * @param detail error message
     */
    private void showError(String title, String detail) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, detail, title, JOptionPane.ERROR_MESSAGE));
    }
}
