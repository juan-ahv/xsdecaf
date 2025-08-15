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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generates Excel reports for XSD schema comparisons
 */
public class ExcelReportGenerator {
    
    /**
     * Generate an Excel file with the comparison results
     * 
     * @param comparisonResults List of comparison results
     * @param outputFile Output Excel file
     * @param firstName First XSD file name
     * @param secondName Second XSD file name
     * @throws IOException if file cannot be written
     */
    public void generateExcelReport(List<SchemaAnalyzer.ComparisonResult> comparisonResults,
                                  File outputFile,
                                  String firstName,
                                  String secondName) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("XSD Comparison");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "NAME", headerStyle);
            createCell(headerRow, 1, firstName, headerStyle);
            createCell(headerRow, 2, secondName, headerStyle);
            
            // Add data rows
            int rowNum = 1;
            for (SchemaAnalyzer.ComparisonResult result : comparisonResults) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Column 1: Complex Type Name
                createCell(dataRow, 0, result.getComplexTypeName(), normalStyle);
                
                // Column 2: What's in first file but not in second
                createCell(dataRow, 1, result.getOnlyInFirst(), normalStyle);
                
                // Column 3: What's in second file but not in first
                createCell(dataRow, 2, result.getOnlyInSecond(), normalStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width to ensure readability
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 3000) { // Minimum width in units
                    sheet.setColumnWidth(i, 3000);
                }
                // Set maximum width to prevent extremely wide columns
                if (currentWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Create a cell with value and style
     */
    private Cell createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
        return cell;
    }
    
    /**
     * Create header style - bold with background color
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Set background color (light blue)
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Set font - bold
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // Set borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Center alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Wrap text
        style.setWrapText(true);
        
        return style;
    }
    
    /**
     * Create normal cell style - used for all data cells (no highlighting)
     */
    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Set borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Set alignment
        style.setVerticalAlignment(VerticalAlignment.TOP);
        
        // Wrap text
        style.setWrapText(true);
        
        return style;
    }
    

}
