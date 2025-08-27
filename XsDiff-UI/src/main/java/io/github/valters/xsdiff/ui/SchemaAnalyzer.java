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
        private final Set<String> annotations;
        private final Set<String> appinfos;
        
        public ComplexTypeInfo(String name) {
            this.name = name;
            this.elements = new LinkedHashSet<>();
            this.attributes = new LinkedHashSet<>();
            this.annotations = new LinkedHashSet<>();
            this.appinfos = new LinkedHashSet<>();
        }
        
        public String getName() { return name; }
        public Set<String> getElements() { return elements; }
        public Set<String> getAttributes() { return attributes; }
        public Set<String> getAnnotations() { return annotations; }
        public Set<String> getAppinfos() { return appinfos; }
        
        public void addElement(String element) { elements.add(element); }
        public void addAttribute(String attribute) { attributes.add(attribute); }
        public void addAnnotation(String annotation) { annotations.add(annotation); }
        public void addAppinfo(String appinfo) { appinfos.add(appinfo); }
        
        /**
         * Get all members (elements, attributes, annotations, and appinfos) as a formatted string
         */
        public String getAllMembers() {
            List<String> all = new ArrayList<>();
            elements.forEach(e -> all.add("element: " + e));
            attributes.forEach(a -> all.add("attribute: " + a));
            annotations.forEach(a -> all.add("annotation: " + a));
            appinfos.forEach(a -> all.add("appinfo: " + a));
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
        
        // Find all complexType elements with both xs: and xsd: prefixes
        NodeList complexTypeNodes = doc.getElementsByTagName("xs:complexType");
        NodeList complexTypeNodesXsd = doc.getElementsByTagName("xsd:complexType");
        
        // Process xs:complexType elements
        for (int i = 0; i < complexTypeNodes.getLength(); i++) {
            Element complexTypeElement = (Element) complexTypeNodes.item(i);
            String typeName = complexTypeElement.getAttribute("name");
            
            if (typeName != null && !typeName.isEmpty()) {
                ComplexTypeInfo typeInfo = new ComplexTypeInfo(typeName);
                analyzeComplexType(complexTypeElement, typeInfo);
                complexTypes.put(typeName, typeInfo);
            }
        }
        
        // Process xsd:complexType elements
        for (int i = 0; i < complexTypeNodesXsd.getLength(); i++) {
            Element complexTypeElement = (Element) complexTypeNodesXsd.item(i);
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
        // Find all element declarations within this complex type (both xs: and xsd: prefixes)
        NodeList elements = complexTypeElement.getElementsByTagName("xs:element");
        NodeList elementsXsd = complexTypeElement.getElementsByTagName("xsd:element");
        
        // Process xs:element elements
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
        
        // Process xsd:element elements
        for (int i = 0; i < elementsXsd.getLength(); i++) {
            Element element = (Element) elementsXsd.item(i);
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
        
        // Find all attribute declarations within this complex type (both xs: and xsd: prefixes)
        NodeList attributes = complexTypeElement.getElementsByTagName("xs:attribute");
        NodeList attributesXsd = complexTypeElement.getElementsByTagName("xsd:attribute");
        
        // Process xs:attribute elements
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
        
        // Process xsd:attribute elements
        for (int i = 0; i < attributesXsd.getLength(); i++) {
            Element attribute = (Element) attributesXsd.item(i);
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
        
        // Find all annotation elements within this complex type (both xs: and xsd: prefixes)
        NodeList annotations = complexTypeElement.getElementsByTagName("xs:annotation");
        NodeList annotationsXsd = complexTypeElement.getElementsByTagName("xsd:annotation");
        
        // Process xs:annotation elements
        for (int i = 0; i < annotations.getLength(); i++) {
            Element annotation = (Element) annotations.item(i);
            typeInfo.addAnnotation("annotation");
            
            // Look for appinfo within annotation
            NodeList appinfos = annotation.getElementsByTagName("xs:appinfo");
            for (int j = 0; j < appinfos.getLength(); j++) {
                Element appinfo = (Element) appinfos.item(j);
                String source = appinfo.getAttribute("source");
                String appinfoDesc = "appinfo";
                if (source != null && !source.isEmpty()) {
                    appinfoDesc += " source='" + source + "'";
                }
                typeInfo.addAppinfo(appinfoDesc);
            }
        }
        
        // Process xsd:annotation elements
        for (int i = 0; i < annotationsXsd.getLength(); i++) {
            Element annotation = (Element) annotationsXsd.item(i);
            typeInfo.addAnnotation("annotation");
            
            // Look for appinfo within annotation
            NodeList appinfos = annotation.getElementsByTagName("xsd:appinfo");
            for (int j = 0; j < appinfos.getLength(); j++) {
                Element appinfo = (Element) appinfos.item(j);
                String source = appinfo.getAttribute("source");
                String appinfoDesc = "appinfo";
                if (source != null && !source.isEmpty()) {
                    appinfoDesc += " source='" + source + "'";
                }
                typeInfo.addAppinfo(appinfoDesc);
            }
        }
        
        // Also look for appinfo elements directly within complex type (both xs: and xsd: prefixes)
        NodeList appinfos = complexTypeElement.getElementsByTagName("xs:appinfo");
        NodeList appinfosXsd = complexTypeElement.getElementsByTagName("xsd:appinfo");
        
        // Process xs:appinfo elements
        for (int i = 0; i < appinfos.getLength(); i++) {
            Element appinfo = (Element) appinfos.item(i);
            String source = appinfo.getAttribute("source");
            String appinfoDesc = "appinfo";
            if (source != null && !source.isEmpty()) {
                appinfoDesc += " source='" + source + "'";
            }
            typeInfo.addAppinfo(appinfoDesc);
        }
        
        // Process xsd:appinfo elements
        for (int i = 0; i < appinfosXsd.getLength(); i++) {
            Element appinfo = (Element) appinfosXsd.item(i);
            String source = appinfo.getAttribute("source");
            String appinfoDesc = "appinfo";
            if (source != null && !source.isEmpty()) {
                appinfoDesc += " source='" + source + "'";
            }
            typeInfo.addAppinfo(appinfoDesc);
        }
        
        // Find all minLength elements within this complex type (both xs: and xsd: prefixes)
        NodeList minLengths = complexTypeElement.getElementsByTagName("xs:minLength");
        NodeList minLengthsXsd = complexTypeElement.getElementsByTagName("xsd:minLength");
        
        // Process xs:minLength elements
        for (int i = 0; i < minLengths.getLength(); i++) {
            Element minLength = (Element) minLengths.item(i);
            String value = minLength.getAttribute("value");
            String minLengthDesc = "minLength";
            if (value != null && !value.isEmpty()) {
                minLengthDesc += " value='" + value + "'";
            }
            typeInfo.addElement(minLengthDesc);
        }
        
        // Process xsd:minLength elements
        for (int i = 0; i < minLengthsXsd.getLength(); i++) {
            Element minLength = (Element) minLengthsXsd.item(i);
            String value = minLength.getAttribute("value");
            String minLengthDesc = "minLength";
            if (value != null && !value.isEmpty()) {
                minLengthDesc += " value='" + value + "'";
            }
            typeInfo.addElement(minLengthDesc);
        }
        
        // Find all maxLength elements within this complex type (both xs: and xsd: prefixes)
        NodeList maxLengths = complexTypeElement.getElementsByTagName("xs:maxLength");
        NodeList maxLengthsXsd = complexTypeElement.getElementsByTagName("xsd:maxLength");
        
        // Process xs:maxLength elements
        for (int i = 0; i < maxLengths.getLength(); i++) {
            Element maxLength = (Element) maxLengths.item(i);
            String value = maxLength.getAttribute("value");
            String maxLengthDesc = "maxLength";
            if (value != null && !value.isEmpty()) {
                maxLengthDesc += " value='" + value + "'";
            }
            typeInfo.addElement(maxLengthDesc);
        }
        
        // Process xsd:maxLength elements
        for (int i = 0; i < maxLengthsXsd.getLength(); i++) {
            Element maxLength = (Element) maxLengthsXsd.item(i);
            String value = maxLength.getAttribute("value");
            String maxLengthDesc = "maxLength";
            if (value != null && !value.isEmpty()) {
                maxLengthDesc += " value='" + value + "'";
            }
            typeInfo.addElement(maxLengthDesc);
        }
        
        // Find all restriction elements within this complex type (both xs: and xsd: prefixes)
        NodeList restrictions = complexTypeElement.getElementsByTagName("xs:restriction");
        NodeList restrictionsXsd = complexTypeElement.getElementsByTagName("xsd:restriction");
        
        // Process xs:restriction elements
        for (int i = 0; i < restrictions.getLength(); i++) {
            Element restriction = (Element) restrictions.item(i);
            String base = restriction.getAttribute("base");
            String restrictionDesc = "restriction";
            if (base != null && !base.isEmpty()) {
                restrictionDesc += " base='" + base + "'";
            }
            typeInfo.addElement(restrictionDesc);
        }
        
        // Process xsd:restriction elements
        for (int i = 0; i < restrictionsXsd.getLength(); i++) {
            Element restriction = (Element) restrictionsXsd.item(i);
            String base = restriction.getAttribute("base");
            String restrictionDesc = "restriction";
            if (base != null && !base.isEmpty()) {
                restrictionDesc += " base='" + base + "'";
            }
            typeInfo.addElement(restrictionDesc);
        }
        
        // Find all documentation elements within this complex type (both xs: and xsd: prefixes)
        NodeList documentations = complexTypeElement.getElementsByTagName("xs:documentation");
        NodeList documentationsXsd = complexTypeElement.getElementsByTagName("xsd:documentation");
        
        // Process xs:documentation elements
        for (int i = 0; i < documentations.getLength(); i++) {
            Element documentation = (Element) documentations.item(i);
            String source = documentation.getAttribute("source");
            String documentationDesc = "documentation";
            if (source != null && !source.isEmpty()) {
                documentationDesc += " source='" + source + "'";
            }
            typeInfo.addAnnotation(documentationDesc);
        }
        
        // Process xsd:documentation elements
        for (int i = 0; i < documentationsXsd.getLength(); i++) {
            Element documentation = (Element) documentationsXsd.item(i);
            String source = documentation.getAttribute("source");
            String documentationDesc = "documentation";
            if (source != null && !source.isEmpty()) {
                documentationDesc += " source='" + source + "'";
            }
            typeInfo.addAnnotation(documentationDesc);
        }
        
        // Find all enumeration elements within this complex type (both xs: and xsd: prefixes)
        NodeList enumerations = complexTypeElement.getElementsByTagName("xs:enumeration");
        NodeList enumerationsXsd = complexTypeElement.getElementsByTagName("xsd:enumeration");
        
        // Process xs:enumeration elements
        for (int i = 0; i < enumerations.getLength(); i++) {
            Element enumeration = (Element) enumerations.item(i);
            String value = enumeration.getAttribute("value");
            String enumerationDesc = "enumeration";
            if (value != null && !value.isEmpty()) {
                enumerationDesc += " value='" + value + "'";
            }
            typeInfo.addElement(enumerationDesc);
        }
        
        // Process xsd:enumeration elements
        for (int i = 0; i < enumerationsXsd.getLength(); i++) {
            Element enumeration = (Element) enumerationsXsd.item(i);
            String value = enumeration.getAttribute("value");
            String enumerationDesc = "enumeration";
            if (value != null && !value.isEmpty()) {
                enumerationDesc += " value='" + value + "'";
            }
            typeInfo.addElement(enumerationDesc);
        }
        
        // Find all simpleContent elements within this complex type (both xs: and xsd: prefixes)
        NodeList simpleContents = complexTypeElement.getElementsByTagName("xs:simpleContent");
        NodeList simpleContentsXsd = complexTypeElement.getElementsByTagName("xsd:simpleContent");
        
        // Process xs:simpleContent elements
        for (int i = 0; i < simpleContents.getLength(); i++) {
            Element simpleContent = (Element) simpleContents.item(i);
            typeInfo.addElement("simpleContent");
        }
        
        // Process xsd:simpleContent elements
        for (int i = 0; i < simpleContentsXsd.getLength(); i++) {
            Element simpleContent = (Element) simpleContentsXsd.item(i);
            typeInfo.addElement("simpleContent");
        }
        
        // Find all sequence elements within this complex type (both xs: and xsd: prefixes)
        NodeList sequences = complexTypeElement.getElementsByTagName("xs:sequence");
        NodeList sequencesXsd = complexTypeElement.getElementsByTagName("xsd:sequence");
        
        // Process xs:sequence elements
        for (int i = 0; i < sequences.getLength(); i++) {
            Element sequence = (Element) sequences.item(i);
            String minOccurs = sequence.getAttribute("minOccurs");
            String maxOccurs = sequence.getAttribute("maxOccurs");
            String sequenceDesc = "sequence";
            if (!minOccurs.isEmpty() || !maxOccurs.isEmpty()) {
                sequenceDesc += " [" + (minOccurs.isEmpty() ? "1" : minOccurs) + 
                             ".." + (maxOccurs.isEmpty() ? "1" : maxOccurs) + "]";
            }
            typeInfo.addElement(sequenceDesc);
        }
        
        // Process xsd:sequence elements
        for (int i = 0; i < sequencesXsd.getLength(); i++) {
            Element sequence = (Element) sequencesXsd.item(i);
            String minOccurs = sequence.getAttribute("minOccurs");
            String maxOccurs = sequence.getAttribute("maxOccurs");
            String sequenceDesc = "sequence";
            if (!minOccurs.isEmpty() || !maxOccurs.isEmpty()) {
                sequenceDesc += " [" + (minOccurs.isEmpty() ? "1" : minOccurs) + 
                             ".." + (maxOccurs.isEmpty() ? "1" : maxOccurs) + "]";
            }
            typeInfo.addElement(sequenceDesc);
        }
        
        // Find all extension elements within this complex type (both xs: and xsd: prefixes)
        NodeList extensions = complexTypeElement.getElementsByTagName("xs:extension");
        NodeList extensionsXsd = complexTypeElement.getElementsByTagName("xsd:extension");
        
        // Process xs:extension elements
        for (int i = 0; i < extensions.getLength(); i++) {
            Element extension = (Element) extensions.item(i);
            String base = extension.getAttribute("base");
            String extensionDesc = "extension";
            if (base != null && !base.isEmpty()) {
                extensionDesc += " base='" + base + "'";
            }
            typeInfo.addElement(extensionDesc);
        }
        
        // Process xsd:extension elements
        for (int i = 0; i < extensionsXsd.getLength(); i++) {
            Element extension = (Element) extensionsXsd.item(i);
            String base = extension.getAttribute("base");
            String extensionDesc = "extension";
            if (base != null && !base.isEmpty()) {
                extensionDesc += " base='" + base + "'";
            }
            typeInfo.addElement(extensionDesc);
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
                members1.addAll(type1.getAnnotations());
                members1.addAll(type1.getAppinfos());
                
                Set<String> members2 = new LinkedHashSet<>();
                members2.addAll(type2.getElements());
                members2.addAll(type2.getAttributes());
                members2.addAll(type2.getAnnotations());
                members2.addAll(type2.getAppinfos());
                
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
