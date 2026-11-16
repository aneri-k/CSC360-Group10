/**
 * =============================================================================
 * PROJECT: Counting Birds in the Garden
 * COURSE:  CSC 360 - Computer Graphics & Image Processing
 * MODULE:  Application Entry Point
 * FILE:    Main.java
 * =============================================================================
 * GIT REPOSITORY METADATA:
 *   Branch:    main (merged from feature/gui-application-shell)
 *   Author:    CSC 360 Project Team
 *   Version:   1.1.0
 *   Changelog:
 *     - v1.0.0: Initial application bootstrap on Swing EDT with System L&F.
 *     - v1.1.0: Added swing.disableWin32ShellFolder property to prevent Windows
 *               COM/ShellFolder enumeration deadlocks; added safe L&F fallback.
 * =============================================================================
 * DESCRIPTION:
 *   Bootstrap entry point for the "Counting Birds in the Garden" desktop tool.
 *   Initializes cross-platform desktop UI parameters and schedules GUI
 *   creation safely onto the Swing Event Dispatch Thread (EDT).
 * =============================================================================
 */

import ui.BirdCountingGUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the "Counting Birds in the Garden" desktop application.
 *
 * <p>Initializes platform look-and-feel safely (with safeguards against
 * Windows COM shell folder hangs) and instantiates the main application
 * frame on the Swing Event Dispatch Thread (EDT).</p>
 *
 * @author CSC 360 Project Team
 * @version 1.1.0
 */
public class Main {

    /**
     * Application entry method.
     *
     * @param args command-line arguments (optional, not currently utilized)
     */
    public static void main(String[] args) {
        // Prevent Windows ShellFolder COM deadlocks in sandboxed/restricted environments
        System.setProperty("swing.disableWin32ShellFolder", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Metal Look and Feel fallback
            }
        }

        // All Swing UI construction and manipulation must occur on the EDT
        SwingUtilities.invokeLater(BirdCountingGUI::new);
    }
}
