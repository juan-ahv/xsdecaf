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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.*;

/**
 * Analyzes XSD schemas to extract complex types and their elements for comparison
 */
public class SchemaAnalyzer {
    
    /**
     * Represents a complex type with its elements and attributes
     */
    public static class ComplexTypeInfo {
        private final String name;
        private final Set<String> elements;
        private final Set<String> attributes;
        
        public ComplexTypeInfo(String name) {
            this.name = name;
            this.elements = new LinkedHashSet<>();
            this.attributes = new LinkedHashSet<>();
        }
        
        public String getName() { return name; }
        public Set<String> getElements() { return elements; }
        public Set<String> getAttributes() { return attributes; }
        
        public void addElement(String element) { elements.add(element); }
        public void addAttribute(String attribute) { attributes.add(attribute); }
        
        /**
         * Get all members (elements and attributes) as a formatted string
         */
        public String getAllMembers() {
            List<String> all = new ArrayList<>();
            elements.forEach(e -> all.add("element: " + e));
            attributes.forEach(a -> all.add("attribute: " + a));
            return String.join(", ", all);
        }
    }
    
    /**
     * Represents the comparison result between two complex types
     */
    public static class ComparisonResult {
        private final String complexTypeName;
        private final String onlyInFirst;
        private final String onlyInSecond;
        
        public ComparisonResult(String complexTypeName, String onlyInFirst, String onlyInSecond) {
            this.complexTypeName = complexTypeName;
            this.onlyInFirst = onlyInFirst;
            this.onlyInSecond = onlyInSecond;
        }
        
        public String getComplexTypeName() { return complexTypeName; }
        public String getOnlyInFirst() { return onlyInFirst; }
        public String getOnlyInSecond() { return onlyInSecond; }
        
        public boolean hasDifferences() {
            return !onlyInFirst.isEmpty() || !onlyInSecond.isEmpty();
        }
        
        public boolean hasAdditions() {
            return !onlyInSecond.isEmpty();
        }
    }
    
    /**
     * Analyze an XSD file and extract complex type information
     */
    public Map<String, ComplexTypeInfo> analyzeSchema(File xsdFile, DocumentBuilder docBuilder) throws Exception {
        Document doc = docBuilder.parse(xsdFile);
        Map<String, ComplexTypeInfo> complexTypes = new LinkedHashMap<>();
        
        // Find all complexType elements
        NodeList complexTypeNodes = doc.getElementsByTagName("xs:complexType");
        for (int i = 0; i < complexTypeNodes.getLength(); i++) {
            Element complexTypeElement = (Element) complexTypeNodes.item(i);
            String typeName = complexTypeElement.getAttribute("name");
            
            if (typeName != null && !typeName.isEmpty()) {
                ComplexTypeInfo typeInfo = new ComplexTypeInfo(typeName);
                analyzeComplexType(complexTypeElement, typeInfo);
                complexTypes.put(typeName, typeInfo);
            }
        }
        
        return complexTypes;
    }
    
    /**
     * Analyze a complex type element and extract its elements and attributes
     */
    private void analyzeComplexType(Element complexTypeElement, ComplexTypeInfo typeInfo) {
        // Find all element declarations within this complex type
        NodeList elements = complexTypeElement.getElementsByTagName("xs:element");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String elementName = element.getAttribute("name");
            String elementType = element.getAttribute("type");
            
            if (elementName != null && !elementName.isEmpty()) {
                String elementDesc = elementName;
                if (elementType != null && !elementType.isEmpty()) {
                    elementDesc += " (" + elementType + ")";
                }
                
                // Add minOccurs/maxOccurs info if present
                String minOccurs = element.getAttribute("minOccurs");
                String maxOccurs = element.getAttribute("maxOccurs");
                if (!minOccurs.isEmpty() || !maxOccurs.isEmpty()) {
                    elementDesc += " [" + (minOccurs.isEmpty() ? "1" : minOccurs) + 
                                 ".." + (maxOccurs.isEmpty() ? "1" : maxOccurs) + "]";
                }
                
                typeInfo.addElement(elementDesc);
            }
        }
        
        // Find all attribute declarations within this complex type
        NodeList attributes = complexTypeElement.getElementsByTagName("xs:attribute");
        for (int i = 0; i < attributes.getLength(); i++) {
            Element attribute = (Element) attributes.item(i);
            String attrName = attribute.getAttribute("name");
            String attrType = attribute.getAttribute("type");
            String defaultValue = attribute.getAttribute("default");
            
            if (attrName != null && !attrName.isEmpty()) {
                String attrDesc = "@" + attrName;
                if (attrType != null && !attrType.isEmpty()) {
                    attrDesc += " (" + attrType + ")";
                }
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    attrDesc += " default='" + defaultValue + "'";
                }
                
                typeInfo.addAttribute(attrDesc);
            }
        }
    }
    
    /**
     * Compare complex types between two schemas
     */
    public List<ComparisonResult> compareSchemas(Map<String, ComplexTypeInfo> schema1, 
                                               Map<String, ComplexTypeInfo> schema2) {
        List<ComparisonResult> results = new ArrayList<>();
        
        // Get all unique complex type names from both schemas
        Set<String> allTypeNames = new LinkedHashSet<>();
        allTypeNames.addAll(schema1.keySet());
        allTypeNames.addAll(schema2.keySet());
        
        for (String typeName : allTypeNames) {
            ComplexTypeInfo type1 = schema1.get(typeName);
            ComplexTypeInfo type2 = schema2.get(typeName);
            
            String onlyInFirst = "";
            String onlyInSecond = "";
            
            if (type1 == null) {
                // Type only exists in second schema
                onlyInSecond = "ENTIRE TYPE: " + type2.getAllMembers();
            } else if (type2 == null) {
                // Type only exists in first schema
                onlyInFirst = "ENTIRE TYPE: " + type1.getAllMembers();
            } else {
                // Type exists in both, compare members
                Set<String> members1 = new LinkedHashSet<>();
                members1.addAll(type1.getElements());
                members1.addAll(type1.getAttributes());
                
                Set<String> members2 = new LinkedHashSet<>();
                members2.addAll(type2.getElements());
                members2.addAll(type2.getAttributes());
                
                // Find members only in first
                Set<String> onlyIn1 = new LinkedHashSet<>(members1);
                onlyIn1.removeAll(members2);
                
                // Find members only in second
                Set<String> onlyIn2 = new LinkedHashSet<>(members2);
                onlyIn2.removeAll(members1);
                
                onlyInFirst = String.join(", ", onlyIn1);
                onlyInSecond = String.join(", ", onlyIn2);
            }
            
            results.add(new ComparisonResult(typeName, onlyInFirst, onlyInSecond));
        }
        
        return results;
    }
}
