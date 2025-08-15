# Test XSD Files - Expected Differences

These test files (`first.xsd` and `second.xsd`) are designed to demonstrate various types of schema changes that the XsDiff tool can detect.

## Expected Differences Between first.xsd and second.xsd

### 1. **Type Changes**
- `customer/id`: Changed from `xs:int` to `xs:long`
- `customer/email`: Changed from `xs:string` to custom `EmailType` with pattern validation

### 2. **Element Modifications**
- `customer/name`: **REMOVED** - Split into separate fields
- `customer/firstName`: **ADDED** - New field replacing name
- `customer/lastName`: **ADDED** - New field replacing name
- `customer/dateOfBirth`: **ADDED** - New optional field

### 3. **Address Changes**
- `address/country`: **ADDED** - New field with default value "USA"

### 4. **Enumeration Changes**
- `StatusType`: **ADDED** new value "suspended"

### 5. **New Types Added**
- `EmailType`: New simple type with email pattern validation
- `MembershipType`: New enumeration type for membership levels
- `CategoryType`: New enumeration type for product categories

### 6. **Attribute Changes**
- `customer/@membershipLevel`: **ADDED** - New optional attribute
- `product/@discontinued`: **ADDED** - New attribute with default value

### 7. **Product Schema Changes**
- `product/description`: **ADDED** - New optional field
- `product/category`: **ADDED** - New required field

### 8. **Completely New Elements**
- `order`: **ADDED** - Entirely new complex element with nested structure
- `OrderType`, `OrderItemsType`, `OrderItemType`: **ADDED** - Supporting types for orders

## How to Test

1. **Start the XsDiff GUI application**
2. **Select first.xsd as the first file**
3. **Select second.xsd as the second file**
4. **Choose an output directory**
5. **Click "Compare Files"**
6. **Review the generated reports:**
   - **HTML Report**: Visual diff with detailed changes
   - **Excel Report**: Structured comparison table

## Expected Excel Output

The Excel file will contain a table with three columns:

### Column 1: NAME
Complex type names found in either schema:
- `CustomerType`
- `AddressType` 
- `ProductType`
- `OrderType` (only in second.xsd)
- `OrderItemsType` (only in second.xsd)
- `OrderItemType` (only in second.xsd)

### Column 2: first.xsd (Red highlighting for differences)
Elements/attributes that exist in first.xsd but not in second.xsd:
- `CustomerType`: `element: name (xs:string)`
- `ProductType`: (empty - no unique elements)

### Column 3: second.xsd (Green highlighting for additions)  
Elements/attributes that exist in second.xsd but not in first.xsd:
- `CustomerType`: `element: firstName (xs:string), element: lastName (xs:string), element: email (EmailType), element: dateOfBirth (xs:date), attribute: @membershipLevel (MembershipType)`
- `AddressType`: `element: country (xs:string) default='USA'`
- `ProductType`: `element: description (xs:string), element: category (CategoryType), attribute: @discontinued (xs:boolean) default='false'`
- `OrderType`: `ENTIRE TYPE: [all elements and attributes]`
- `OrderItemsType`: `ENTIRE TYPE: [all elements and attributes]`
- `OrderItemType`: `ENTIRE TYPE: [all elements and attributes]`

The HTML report should clearly show:
- Elements that were added, removed, or modified
- Type changes and their implications
- New enumerations and their values
- Structural changes in the schema hierarchy

## Test Scenarios

### Scenario 1: Basic Comparison
Compare the two files as-is to see all differences.

### Scenario 2: Error Handling
Try selecting non-XSD files or invalid paths to test error handling.

### Scenario 3: Output Directory
Test different output directories including ones that don't exist yet.

These test files provide a comprehensive example of the types of schema evolution that are common in real-world scenarios.
