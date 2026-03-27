package SeqFiles;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.util.*;

public class SequenceAlignmentComparator extends JFrame {

    private JComboBox<String> pairSelector;
    private JTextArea seq1Area, seq2Area;
    private JSpinner matchSpinner, mismatchSpinner, gapSpinner;
    private JTextArea nwResultArea, swResultArea;
    private JTable comparisonTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private List<DataExporter.ComparisonResult> batchResults;


    private Map<String, String[]> sequencePairs;

    public SequenceAlignmentComparator() {
        super("Sequence Alignment Algorithm Comparator");
        batchResults = new ArrayList<>();
        initializeSequencePairs();
        initializeUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
    }


    private void initializeSequencePairs() {
        sequencePairs = new LinkedHashMap<>();

        String csvPath = "src/SeqFiles/GSD.csv";  // change if your CSV is elsewhere

        try {
            List<String[]> pairs = DNAReader.loadSequencePairs(csvPath);

            int index = 1;
            for (String[] pair : pairs) {
                String name = "Pair " + index;
                sequencePairs.put(name, pair);
                index++;
            }

            // Update selector dropdown
            if (pairSelector != null) {
                pairSelector.setModel(new DefaultComboBoxModel<>(sequencePairs.keySet().toArray(new String[0])));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load sequence pairs from file:\n" + csvPath +
                            "\n\nError: " + e.getMessage(),
                    "File Load Error",
                    JOptionPane.ERROR_MESSAGE);

            // Fallback: keep demo pairs
            sequencePairs.put("Demo Pair 1", new String[]{"ACGT", "ACGT"});
            sequencePairs.put("Demo Pair 2", new String[]{"ACGTACGT", "GGGGACGT"});
        }
    }


    private void exportBatchResults() {
        if (batchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No batch results to export! Run 'Run All Pairs' first.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Batch Results");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        fileChooser.setSelectedFile(new java.io.File("batch_results.json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filename.endsWith(".json")) {
                    filename += ".json";
                }
                DataExporter.exportBatchResults(filename, batchResults);
                statusLabel.setText("Batch results exported to: " + filename);
                JOptionPane.showMessageDialog(this,
                        "Data exported successfully to:\n" + filename + "\n\n" +
                                "Now click 'Generate Visualizations' to create graphs!",
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting data: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // Top Panel - Input Controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);


        // Center Panel - Split between sequences and results
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setTopComponent(createSequencePanel());
        centerSplit.setBottomComponent(createResultsPanel());
        centerSplit.setDividerLocation(200);
        add(centerSplit, BorderLayout.CENTER);

        // Bottom Panel - Comparison Table
        JPanel bottomPanel = createComparisonPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // Status Bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.PAGE_END);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pair selector
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.add(new JLabel("Select Sequence Pair:"));
        pairSelector = new JComboBox<>(sequencePairs.keySet().toArray(new String[0]));
        pairSelector.addActionListener(e -> loadSelectedPair());
        selectorPanel.add(pairSelector);

        // Scoring parameters
        JPanel scoringPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scoringPanel.add(new JLabel("Match:"));
        matchSpinner = new JSpinner(new SpinnerNumberModel(2, -10, 10, 1));
        scoringPanel.add(matchSpinner);

        scoringPanel.add(new JLabel("  Mismatch:"));
        mismatchSpinner = new JSpinner(new SpinnerNumberModel(-1, -10, 10, 1));
        scoringPanel.add(mismatchSpinner);

        scoringPanel.add(new JLabel("  Gap:"));
        gapSpinner = new JSpinner(new SpinnerNumberModel(-1, -10, 10, 1));
        scoringPanel.add(gapSpinner);

        // Buttons
        JButton runButton = new JButton("Run Comparison");
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
        runButton.addActionListener(e -> runComparison());
        scoringPanel.add(runButton);

        JButton runAllButton = new JButton("Run All Pairs");
        runAllButton.addActionListener(e -> runAllPairs());
        scoringPanel.add(runAllButton);

        JButton clearButton = new JButton("Clear Results");
        clearButton.addActionListener(e -> clearResults());
        scoringPanel.add(clearButton);

        JButton showMatrixButton = new JButton("Show Matrices");
        showMatrixButton.setFont(showMatrixButton.getFont().deriveFont(Font.ITALIC));
        showMatrixButton.setForeground(Color.red);
        showMatrixButton.addActionListener(e -> showMatrices());
        scoringPanel.add(showMatrixButton);

        // NEW: Export and Visualize buttons
        JButton exportBatchButton = new JButton("Export Batch Results");
        exportBatchButton.addActionListener(e -> exportBatchResults());
        scoringPanel.add(exportBatchButton);

        JButton visualizeButton = new JButton("Generate Visualizations");
        visualizeButton.setFont(visualizeButton.getFont().deriveFont(Font.BOLD));
        visualizeButton.setBackground(new Color(46, 204, 113));
        visualizeButton.setForeground(Color.BLUE);
        visualizeButton.addActionListener(e -> generateVisualizations());
        scoringPanel.add(visualizeButton);

        panel.add(selectorPanel, BorderLayout.NORTH);
        panel.add(scoringPanel, BorderLayout.CENTER);

        return panel;
    }

    private void generateVisualizations() {
        // Check if batch results file exists
        java.io.File jsonFile = new java.io.File("batch_results.json");

        if (!jsonFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "No batch results file found!\n\n" +
                            "Please:\n" +
                            "1. Click 'Run All Pairs'\n" +
                            "2. Click 'Export Batch Results'\n" +
                            "3. Then try again",
                    "No Data File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("Generating visualizations...");

        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            String errorMessage = null;

            @Override
            protected Integer doInBackground() {
                try {
                    // Execute Python script
                    ProcessBuilder pb = new ProcessBuilder(
                            "python", "visualization.py", "batch_results.json"
                    );
                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    // Read output
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream())
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    return process.waitFor();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return -1;
                }
            }

            @Override
            protected void done() {
                try {
                    int exitCode = get();
                    if (exitCode == 0) {
                        statusLabel.setText("Visualization complete! Check the 'visualizations' folder.");
                        JOptionPane.showMessageDialog(SequenceAlignmentComparator.this,
                                "Visualization complete! Check the 'visualizations' folder.\n\n" +
                                        "Check the 'visualizations' folder for:\n" +
                                        "• batch_analysis.png\n" +
                                        "• complexity_analysis.png\n" +
                                        "• summary_statistics.csv",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("Error generating visualizations");
                        JOptionPane.showMessageDialog(SequenceAlignmentComparator.this,
                                "Error running Python script!\n\n" +
                                        "Make sure:\n" +
                                        "1. Python is installed and in PATH\n" +
                                        "2. Required packages are installed:\n" +
                                        "   pip install pandas matplotlib seaborn\n" +
                                        "3. visualization.py is in the same directory\n\n" +
                                        (errorMessage != null ? "Error: " + errorMessage : ""),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private JPanel createSequencePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Input Sequences"));

        // Sequence 1
        JPanel seq1Panel = new JPanel(new BorderLayout());
        seq1Panel.add(new JLabel("Sequence 1:"), BorderLayout.NORTH);
        seq1Area = new JTextArea(5, 40);
        seq1Area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        seq1Area.setLineWrap(true);
        seq1Panel.add(new JScrollPane(seq1Area), BorderLayout.CENTER);

        // Sequence 2
        JPanel seq2Panel = new JPanel(new BorderLayout());
        seq2Panel.add(new JLabel("Sequence 2:"), BorderLayout.NORTH);
        seq2Area = new JTextArea(5, 40);
        seq2Area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        seq2Area.setLineWrap(true);
        seq2Panel.add(new JScrollPane(seq2Area), BorderLayout.CENTER);

        panel.add(seq1Panel);
        panel.add(seq2Panel);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Alignment Results"));

        // Needleman-Wunsch results
        JPanel nwPanel = new JPanel(new BorderLayout());
        nwPanel.add(new JLabel("Needleman-Wunsch (Global Alignment)"), BorderLayout.NORTH);
        nwResultArea = new JTextArea();
        nwResultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        nwResultArea.setEditable(false);
        nwPanel.add(new JScrollPane(nwResultArea), BorderLayout.CENTER);

        // Smith-Waterman results
        JPanel swPanel = new JPanel(new BorderLayout());
        swPanel.add(new JLabel("Smith-Waterman (Local Alignment)"), BorderLayout.NORTH);
        swResultArea = new JTextArea();
        swResultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        swResultArea.setEditable(false);
        swPanel.add(new JScrollPane(swResultArea), BorderLayout.CENTER);

        panel.add(nwPanel);
        panel.add(swPanel);

        return panel;
    }

    private JPanel createComparisonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Performance Comparison"));
        panel.setPreferredSize(new Dimension(0, 200));

        String[] columns = {"Pair", "Seq1 Len", "Seq2 Len", "NW Score", "NW Time (μs)",
                           "NW Memory (KB)", "SW Score", "SW Time (μs)", "SW Memory (KB)",
                           "Score Diff", "Time Diff"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        comparisonTable = new JTable(tableModel);
        comparisonTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        int[] widths = {120, 70, 70, 80, 100, 110, 80, 100, 110, 80, 80};
        for (int i = 0; i < widths.length; i++) {
            comparisonTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        panel.add(new JScrollPane(comparisonTable), BorderLayout.CENTER);

        return panel;
    }

    private void loadSelectedPair() {
        String selected = (String) pairSelector.getSelectedItem();
        String[] seqs = sequencePairs.get(selected);
        seq1Area.setText(seqs[0]);
        seq2Area.setText(seqs[1]);
    }

    private void runComparison() {
        String s1 = seq1Area.getText().trim().toUpperCase();
        String s2 = seq2Area.getText().trim().toUpperCase();

        if (s1.isEmpty() || s2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both sequences!",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int match = (Integer) matchSpinner.getValue();
        int mismatch = (Integer) mismatchSpinner.getValue();
        int gap = (Integer) gapSpinner.getValue();

        statusLabel.setText("Running alignment algorithms...");

        // Run in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            NeedlemanWunsch.Result nwResult;
            SmithWaterman.Result swResult;
            long nwTime, swTime;
            long nwMemory, swMemory;

            @Override
            protected Void doInBackground() {
                Runtime rt = Runtime.getRuntime();

                // Needleman-Wunsch
                rt.gc();
                long nwBefore = rt.totalMemory() - rt.freeMemory();
                long nwStart = System.nanoTime();
                nwResult = NeedlemanWunsch.needlemanWunsch(s1, s2, match, mismatch, gap);
                long nwEnd = System.nanoTime();
                long nwAfter = rt.totalMemory() - rt.freeMemory();
                nwTime = nwEnd - nwStart;
                nwMemory = nwAfter - nwBefore;

                // Smith-Waterman
                rt.gc();
                long swBefore = rt.totalMemory() - rt.freeMemory();
                long swStart = System.nanoTime();
                swResult = SmithWaterman.smithWaterman(s1, s2, match, mismatch, gap);
                long swEnd = System.nanoTime();
                long swAfter = rt.totalMemory() - rt.freeMemory();
                swTime = swEnd - swStart;
                swMemory = swAfter - swBefore;

                return null;
            }

            @Override
            protected void done() {
                displayResults(nwResult, swResult, nwTime, swTime, nwMemory, swMemory);

                // Add to comparison table
                String pairName = (String) pairSelector.getSelectedItem();
                addToComparisonTable(pairName, s1, s2, nwResult, swResult,
                                   nwTime, swTime, nwMemory, swMemory);

                statusLabel.setText("Comparison complete!");
            }
        };

        worker.execute();
    }

    private void displayResults(NeedlemanWunsch.Result nw, SmithWaterman.Result sw,
                               long nwTime, long swTime, long nwMem, long swMem) {
        // Needleman-Wunsch results
        StringBuilder nwText = new StringBuilder();
        nwText.append(String.format("Score: %d\n", nw.score));
        nwText.append(String.format("Time: %.2f μs\n", nwTime / 1000.0));
        nwText.append(String.format("Memory: %.2f KB\n\n", nwMem / 1024.0));
        nwText.append("Alignment:\n");
        nwText.append(formatAlignment(nw.alignedS1, nw.alignedS2));
        nwResultArea.setText(nwText.toString());
        nwResultArea.setCaretPosition(0);


        // Smith-Waterman results
        StringBuilder swText = new StringBuilder();
        swText.append(String.format("Score: %d\n", sw.maxScore));
        swText.append(String.format("Time: %.2f μs\n", swTime / 1000.0));
        swText.append(String.format("Memory: %.2f KB\n", swMem / 1024.0));
        swText.append(String.format("Position: (%d,%d) to (%d,%d)\n\n",
                                   sw.startI, sw.startJ, sw.endI, sw.endJ));
        swText.append("Alignment:\n");
        if (!sw.alignedS1.isEmpty()) {
            swText.append(formatAlignment(sw.alignedS1, sw.alignedS2));
        } else {
            swText.append("No significant local alignment found.\n");
        }
        swResultArea.setText(swText.toString());
        swResultArea.setCaretPosition(0);
    }

    private String formatAlignment(String s1, String s2) {
        StringBuilder result = new StringBuilder();
        StringBuilder middle = new StringBuilder();

        for (int i = 0; i < s1.length(); i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 == c2 && c1 != '-') {
                middle.append('|');
            } else if (c1 == '-' || c2 == '-') {
                middle.append(' ');
            } else {
                middle.append(':');
            }
        }

        int chunkSize = 60;
        for (int i = 0; i < s1.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, s1.length());
            result.append(s1.substring(i, end)).append("\n");
            result.append(middle.substring(i, end)).append("\n");
            result.append(s2.substring(i, end)).append("\n\n");
        }

        return result.toString();
    }

    private void addToComparisonTable(String pairName, String s1, String s2,
                                     NeedlemanWunsch.Result nw, SmithWaterman.Result sw,
                                     long nwTime, long swTime, long nwMem, long swMem) {
        Object[] row = {
            pairName,
            s1.length(),
            s2.length(),
            nw.score,
            String.format("%.2f", nwTime / 1000.0),
            String.format("%.2f", nwMem / 1024.0),
            sw.maxScore,
            String.format("%.2f", swTime / 1000.0),
            String.format("%.2f", swMem / 1024.0),
            nw.score - sw.maxScore,
            String.format("%.2f%%", ((swTime - nwTime) / (double) nwTime) * 100)
        };
        tableModel.addRow(row);
    }

    private void runAllPairs() {
        clearResults();
        batchResults.clear();
        statusLabel.setText("Running all pairs...");

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() {
                int count = 0;
                for (String pairName : sequencePairs.keySet()) {
                    String[] seqs = sequencePairs.get(pairName);
                    String s1 = seqs[0];
                    String s2 = seqs[1];

                    int match = (Integer) matchSpinner.getValue();
                    int mismatch = (Integer) mismatchSpinner.getValue();
                    int gap = (Integer) gapSpinner.getValue();

                    Runtime rt = Runtime.getRuntime();

                    // NW
                    rt.gc();
                    long nwBefore = rt.totalMemory() - rt.freeMemory();
                    long nwStart = System.nanoTime();
                    NeedlemanWunsch.Result nwResult =
                            NeedlemanWunsch.needlemanWunsch(s1, s2, match, mismatch, gap);
                    long nwEnd = System.nanoTime();
                    long nwAfter = rt.totalMemory() - rt.freeMemory();

                    // SW
                    rt.gc();
                    long swBefore = rt.totalMemory() - rt.freeMemory();
                    long swStart = System.nanoTime();
                    SmithWaterman.Result swResult =
                            SmithWaterman.smithWaterman(s1, s2, match, mismatch, gap);
                    long swEnd = System.nanoTime();
                    long swAfter = rt.totalMemory() - rt.freeMemory();

                    final String fname = pairName;
                    final String fs1 = s1;
                    final String fs2 = s2;
                    final NeedlemanWunsch.Result fnw = nwResult;
                    final SmithWaterman.Result fsw = swResult;
                    final long fnwTime = nwEnd - nwStart;
                    final long fswTime = swEnd - swStart;
                    final long fnwMem = nwAfter - nwBefore;
                    final long fswMem = swAfter - swBefore;

                    // Store result for export
                    DataExporter.ComparisonResult result = new DataExporter.ComparisonResult(
                            fname, fs1.length(), fs2.length(),
                            fnw.score, fnwTime, fnwMem,
                            fsw.maxScore, fswTime, fswMem
                    );
                    batchResults.add(result);

                    SwingUtilities.invokeLater(() -> {
                        addToComparisonTable(fname, fs1, fs2, fnw, fsw,
                                fnwTime, fswTime, fnwMem, fswMem);
                    });

                    publish(++count);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                statusLabel.setText(String.format("Processing pair %d of %d...",
                        latest, sequencePairs.size()));
            }

            @Override
            protected void done() {
                statusLabel.setText("All pairs processed! Ready to export and visualize.");
                JOptionPane.showMessageDialog(SequenceAlignmentComparator.this,
                        "Completed comparison of all " + sequencePairs.size() + " pairs!\n" +
                                "Click 'Export Batch Results' then 'Generate Visualizations' to create graphs.",
                        "Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }


    private void clearResults() {
        nwResultArea.setText("");
        swResultArea.setText("");
        tableModel.setRowCount(0);
        statusLabel.setText("Results cleared");
    }

    private String matrixToString(int[][] matrix, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");

        for (int[] row : matrix) {
            for (int val : row) {
                sb.append(String.format("%4d", val));
            }
            sb.append("\n");
        }

        return sb.toString();
    }



    private void showMatrices() {
        String s1 = seq1Area.getText().trim().toUpperCase();
        String s2 = seq2Area.getText().trim().toUpperCase();

        if (s1.isEmpty() || s2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both sequences first!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int match = (Integer) matchSpinner.getValue();
        int mismatch = (Integer) mismatchSpinner.getValue();
        int gap = (Integer) gapSpinner.getValue();

        // Compute matrices using your alignment algorithms
        NeedlemanWunsch.Result nwResult = NeedlemanWunsch.needlemanWunsch(s1, s2, match, mismatch, gap);

        SmithWaterman.Result swResult = SmithWaterman.smithWaterman(s1, s2, match, mismatch, gap);

        // Convert matrices to printable text
        String nwMatrixText = matrixToString(nwResult.scoringMatrix, "Needleman–Wunsch Scoring Matrix");
        String swMatrixText = matrixToString(swResult.scoringMatrix, "Smith–Waterman Scoring Matrix");

        // Show inside scrollable dialog
        JTextArea textArea = new JTextArea(nwMatrixText + "\n\n" + swMatrixText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(900, 600));

        JOptionPane.showMessageDialog(this, scrollPane, "Alignment Matrices", JOptionPane.PLAIN_MESSAGE);
    }








    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SequenceAlignmentComparator().setVisible(true);
        });
    }






}