/*
  This file is licensed to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.github.valters.xsdiff.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import javax.swing.SwingWorker;

/**
 * GUI application for XSD schema comparison
 */
public class XsDiffGUI extends JFrame {
    
    private JTextField file1Field;
    private JTextField file2Field;
    private JTextField outputField;
    private JButton file1Button;
    private JButton file2Button;
    private JButton outputButton;
    private JButton compareButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    
    private File selectedFile1;
    private File selectedFile2;
    private File selectedOutputDir;
    
    public XsDiffGUI() {
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("XSD Schema Diff Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create input panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Create log panel
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set window properties
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set initial state
        updateCompareButtonState();
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("File Selection"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // File 1
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("First XSD File:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        file1Field = new JTextField();
        file1Field.setEditable(false);
        panel.add(file1Field, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        file1Button = new JButton("Browse...");
        file1Button.addActionListener(e -> browseFile1());
        panel.add(file1Button, gbc);
        
        // File 2
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Second XSD File:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        file2Field = new JTextField();
        file2Field.setEditable(false);
        panel.add(file2Field, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        file2Button = new JButton("Browse...");
        file2Button.addActionListener(e -> browseFile2());
        panel.add(file2Button, gbc);
        
        // Output Directory
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Output Directory:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        outputField = new JTextField();
        outputField.setEditable(false);
        // Set default output directory to current directory
        outputField.setText(System.getProperty("user.dir"));
        selectedOutputDir = new File(System.getProperty("user.dir"));
        panel.add(outputField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        outputButton = new JButton("Browse...");
        outputButton.addActionListener(e -> browseOutputDirectory());
        panel.add(outputButton, gbc);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setText("Welcome to XSD Schema Diff Tool\nSelect two XSD files to compare and an output directory.\nGenerates both HTML reports and Excel comparison files.\n");
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setVisible(false);
        panel.add(progressBar, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonSubPanel = new JPanel(new FlowLayout());
        
        compareButton = new JButton("Compare Files");
        compareButton.addActionListener(e -> performComparison());
        buttonSubPanel.add(compareButton);
        
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        buttonSubPanel.add(exitButton);
        
        panel.add(buttonSubPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void browseFile1() {
        File file = selectXSDFile("Select First XSD File");
        if (file != null) {
            selectedFile1 = file;
            file1Field.setText(file.getAbsolutePath());
            logMessage("Selected first file: " + file.getName());
            updateCompareButtonState();
        }
    }
    
    private void browseFile2() {
        File file = selectXSDFile("Select Second XSD File");
        if (file != null) {
            selectedFile2 = file;
            file2Field.setText(file.getAbsolutePath());
            logMessage("Selected second file: " + file.getName());
            updateCompareButtonState();
        }
    }
    
    private void browseOutputDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Output Directory");
        
        if (selectedOutputDir != null) {
            fileChooser.setCurrentDirectory(selectedOutputDir);
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedOutputDir = fileChooser.getSelectedFile();
            outputField.setText(selectedOutputDir.getAbsolutePath());
            logMessage("Selected output directory: " + selectedOutputDir.getName());
        }
    }
    
    private File selectXSDFile(String title) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XSD Files (*.xsd)", "xsd");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(title);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    private void updateCompareButtonState() {
        compareButton.setEnabled(selectedFile1 != null && selectedFile2 != null && selectedOutputDir != null);
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private void performComparison() {
        if (selectedFile1 == null || selectedFile2 == null || selectedOutputDir == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select both XSD files and an output directory.", 
                "Missing Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable UI during comparison
        setUIEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Comparing files...");
        
        // Perform comparison in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Starting comparison...");
                
                try {
                    XsDiffService service = new XsDiffService();
                    service.compareFiles(selectedFile1, selectedFile2, selectedOutputDir, this::publish);
                    publish("Comparison completed successfully!");
                } catch (Exception e) {
                    publish("Error during comparison: " + e.getMessage());
                    throw e;
                }
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    logMessage(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    progressBar.setString("Completed");
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    
                    // Show success message with option to open output
                    int choice = JOptionPane.showConfirmDialog(XsDiffGUI.this,
                        "Comparison completed successfully!\n" +
                        "Generated HTML reports and Excel comparison file.\n" +
                        "Would you like to open the output directory?",
                        "Comparison Complete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(selectedOutputDir);
                        } catch (Exception e) {
                            logMessage("Could not open output directory: " + e.getMessage());
                        }
                    }
                    
                } catch (Exception e) {
                    progressBar.setString("Error");
                    progressBar.setIndeterminate(false);
                    JOptionPane.showMessageDialog(XsDiffGUI.this,
                        "An error occurred during comparison:\n" + e.getMessage(),
                        "Comparison Error",
                        JOptionPane.ERROR_MESSAGE);
                    logMessage("Comparison failed: " + e.getMessage());
                } finally {
                    setUIEnabled(true);
                    
                    // Hide progress bar after a delay
                    Timer timer = new Timer(2000, e -> progressBar.setVisible(false));
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        };
        
        worker.execute();
    }
    
    private void setUIEnabled(boolean enabled) {
        file1Button.setEnabled(enabled);
        file2Button.setEnabled(enabled);
        outputButton.setEnabled(enabled);
        compareButton.setEnabled(enabled && selectedFile1 != null && selectedFile2 != null && selectedOutputDir != null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new XsDiffGUI().setVisible(true);
        });
    }
}
