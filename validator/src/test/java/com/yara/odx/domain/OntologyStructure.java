package com.yara.odx.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class OntologyStructure {

    @JsonProperty("ClassNames")
    private List<String> classNames;
    @JsonProperty("RelationshipNames")
    private List<String> relationshipNames;
    @JsonProperty("AttributeNames")
    private Map<String, List<String>> attributesMap;

    public OntologyStructure() {
    }

    public OntologyStructure(List<String> classNames, List<String> relationshipNames, Map<String, List<String>> attributesMap) {
        this.classNames = classNames;
        this.relationshipNames = relationshipNames;
        this.attributesMap = attributesMap;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public void addClassName(String className) {
        classNames.add(className);
    }

    public List<String> getRelationshipNames() {
        return relationshipNames;
    }

    public void setRelationshipNames(List<String> relationshipNames) {
        this.relationshipNames = relationshipNames;
    }

    public void addRelationshipName(String relationshipName) {
        relationshipNames.add(relationshipName);
    }

    public Map<String, List<String>> getAttributesMap() {
        return attributesMap;
    }

    public void setAttributesMap(Map<String, List<String>> attributesMap) {
        this.attributesMap = attributesMap;
    }

    public void addAttributeName(String className, String attributeName) {
        attributesMap.get(className).add(attributeName);
    }

    @Override
    public String toString() {
        return "OntologyStructure{" +
                "classNames=" + classNames +
                ",\n relationshipNames=" + relationshipNames +
                ",\n attributesMap=" + attributesMap +
                '}';
    }
}
