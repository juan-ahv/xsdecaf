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

import io.github.valters.xsdiff.app.Main;
import io.github.valters.xsdiff.report.XmlDomUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;

/**
 * Service class to integrate GUI with XsDiff core functionality
 */
public class XsDiffService {
    
    private static final DateTimeFormatter MINUTESTAMP = DateTimeFormatter.ofPattern("HHmm");
    
    /**
     * Compare two XSD files and generate HTML report
     * 
     * @param file1 First XSD file
     * @param file2 Second XSD file
     * @param outputDir Output directory for reports
     * @param logger Function to receive log messages
     * @throws Exception if comparison fails
     */
    public void compareFiles(File file1, File file2, File outputDir, Consumer<String> logger) throws Exception {
        if (file1 == null || file2 == null || outputDir == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        
        if (!file1.exists()) {
            throw new IllegalArgumentException("First file does not exist: " + file1.getAbsolutePath());
        }
        
        if (!file2.exists()) {
            throw new IllegalArgumentException("Second file does not exist: " + file2.getAbsolutePath());
        }
        
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IllegalArgumentException("Could not create output directory: " + outputDir.getAbsolutePath());
        }
        
        if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException("Output path is not a directory: " + outputDir.getAbsolutePath());
        }
        
        logger.accept("Validating input files...");
        
        // Validate XSD files
        if (!file1.getName().toLowerCase().endsWith(".xsd")) {
            logger.accept("Warning: First file does not have .xsd extension");
        }
        
        if (!file2.getName().toLowerCase().endsWith(".xsd")) {
            logger.accept("Warning: Second file does not have .xsd extension");
        }
        
        logger.accept("Input validation completed");
        logger.accept("First file: " + file1.getAbsolutePath());
        logger.accept("Second file: " + file2.getAbsolutePath());
        
        // Create a unique report folder name
        String reportFolderName = "report-" + 
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "-" + 
            LocalTime.now().format(MINUTESTAMP);
        
        File reportDir = new File(outputDir, reportFolderName);
        
        logger.accept("Report will be generated in: " + reportDir.getAbsolutePath());
        
        // Prepare arguments for Main.App
        String[] args = {
            file1.getAbsolutePath(),
            file2.getAbsolutePath(),
            reportDir.getAbsolutePath()
        };
        
        logger.accept("Starting XSD comparison...");
        
        // Capture output from Main.App
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream captureStream = new PrintStream(baos)) {
            
            // Redirect System.out to capture output
            System.setOut(captureStream);
            System.setErr(captureStream);
            
            // Run the comparison using Main.main
            Main.main(args);
            
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
            
            // Log captured output
            String capturedOutput = baos.toString();
            if (!capturedOutput.trim().isEmpty()) {
                String[] lines = capturedOutput.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        logger.accept("XsDiff: " + line.trim());
                    }
                }
            }
            
        } catch (Exception e) {
            // Restore original streams in case of error
            System.setOut(originalOut);
            System.setErr(originalErr);
            
            logger.accept("Error during comparison: " + e.getMessage());
            throw new Exception("XSD comparison failed: " + e.getMessage(), e);
        }
        
        // Verify that report was generated
        if (!reportDir.exists()) {
            throw new Exception("Report directory was not created: " + reportDir.getAbsolutePath());
        }
        
        // Check for generated HTML files
        File[] htmlFiles = reportDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".html"));
        if (htmlFiles == null || htmlFiles.length == 0) {
            logger.accept("Warning: No HTML report files were generated");
        } else {
            logger.accept("Generated " + htmlFiles.length + " HTML report file(s):");
            for (File htmlFile : htmlFiles) {
                logger.accept("  - " + htmlFile.getName());
            }
        }
        
        logger.accept("Comparison process completed successfully");
        
        // Generate Excel report
        logger.accept("Generating Excel comparison report...");
        try {
            generateExcelReport(file1, file2, reportDir, logger);
            logger.accept("Excel report generated successfully");
        } catch (Exception e) {
            logger.accept("Warning: Failed to generate Excel report: " + e.getMessage());
            // Don't fail the entire process if Excel generation fails
        }
    }
    
    /**
     * Generate Excel comparison report
     */
    private void generateExcelReport(File file1, File file2, File reportDir, Consumer<String> logger) throws Exception {
        logger.accept("Analyzing XSD schemas for Excel report...");
        
        // Create document builder
        DocumentBuilder docBuilder = XmlDomUtils.documentBuilder();
        
        // Analyze both schemas
        SchemaAnalyzer analyzer = new SchemaAnalyzer();
        Map<String, SchemaAnalyzer.ComplexTypeInfo> schema1 = analyzer.analyzeSchema(file1, docBuilder);
        Map<String, SchemaAnalyzer.ComplexTypeInfo> schema2 = analyzer.analyzeSchema(file2, docBuilder);
        
        logger.accept("Found " + schema1.size() + " complex types in first schema");
        logger.accept("Found " + schema2.size() + " complex types in second schema");
        
        // Compare schemas
        List<SchemaAnalyzer.ComparisonResult> comparisonResults = analyzer.compareSchemas(schema1, schema2);
        
        // Generate Excel file
        String excelFileName = "comparison-" + 
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "-" + 
            LocalTime.now().format(MINUTESTAMP) + ".xlsx";
        File excelFile = new File(reportDir, excelFileName);
        
        ExcelReportGenerator excelGenerator = new ExcelReportGenerator();
        excelGenerator.generateExcelReport(
            comparisonResults,
            excelFile,
            file1.getName(),
            file2.getName()
        );
        
        logger.accept("Excel report saved as: " + excelFile.getName());
        
        // Log summary of differences
        long typesWithDifferences = comparisonResults.stream()
            .mapToLong(r -> r.hasDifferences() ? 1 : 0)
            .sum();
        long typesWithAdditions = comparisonResults.stream()
            .mapToLong(r -> r.hasAdditions() ? 1 : 0)
            .sum();
            
        logger.accept("Excel summary: " + comparisonResults.size() + " complex types analyzed, " +
                     typesWithDifferences + " with differences, " + 
                     typesWithAdditions + " with additions");
    }
    
    /**
     * Run XsDiff comparison using the main method
     * @param args Command line arguments for XsDiff
     */
    public void runXsDiffMain(String[] args) {
        Main.main(args);
    }
}
