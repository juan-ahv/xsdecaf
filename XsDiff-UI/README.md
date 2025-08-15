# XsDiff GUI Application

A graphical user interface for the XsDiff XML Schema comparison tool.

## Features

- **File Selection**: Easy selection of two XSD files to compare
- **Output Directory**: Choose where to save the comparison reports
- **Progress Indication**: Visual feedback during comparison process
- **Error Handling**: User-friendly error messages and logging
- **Dual Report Generation**: 
  - HTML reports with detailed visual differences
  - Excel (.xlsx) comparison files with formatted complex type analysis

## How to Build

From the project root directory:

```bash
mvn clean package
```

This will create a JAR file: `XsDiff-UI/target/xsdiff-ui-1.2.0.jar`

## How to Run

### Option 1: Using Java directly
```bash
java -jar XsDiff-UI/target/xsdiff-ui-1.2.0.jar
```

### Option 2: Using Maven
```bash
cd XsDiff-UI
mvn exec:java -Dexec.mainClass="io.github.valters.xsdiff.ui.XsDiffGUI"
```

## Usage

1. **Launch the application** - The GUI window will appear
2. **Select First XSD File** - Click "Browse..." next to "First XSD File" to select your first schema file
3. **Select Second XSD File** - Click "Browse..." next to "Second XSD File" to select your second schema file
4. **Choose Output Directory** - Click "Browse..." next to "Output Directory" to select where reports will be saved (defaults to current directory)
5. **Compare Files** - Click "Compare Files" to start the comparison process
6. **View Results** - Once complete, you can choose to open the output directory to view the generated HTML reports

## Output

The tool generates:
- **HTML Reports**: Detailed diff reports showing visual differences between schemas
- **Excel Report**: Structured comparison file (.xlsx) with:
  - Complex type names in first column
  - Elements/attributes only in first file (highlighted in red)
  - Elements/attributes only in second file (highlighted in green)
  - Professional formatting with borders and proper column sizing
- **Supporting Files**: CSS and JavaScript files for HTML report styling and interactivity
- **Organized Output**: All files in a timestamped folder for easy organization

## Requirements

- Java 8 or higher
- XSD files to compare

## Dependencies

This module depends on:
- `xsdiff` (core comparison library)
- `xsdiff-app` (command-line application)
- Java Swing (included with Java)
- Apache POI (for Excel generation)

## Error Handling

The GUI includes comprehensive error handling for:
- Missing or invalid file selections
- File access errors
- Comparison processing errors
- Output directory creation issues

All errors are displayed in user-friendly dialog boxes and logged in the application log area.
