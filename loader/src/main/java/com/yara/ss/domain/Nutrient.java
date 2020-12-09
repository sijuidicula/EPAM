package com.yara.ss.domain;

import java.util.Objects;

public class Nutrient extends Thing {

    private String name;
    private String elementalName;
    private String nutrientOrdinal;

    public Nutrient(String source, String className, String id, String name, String elementalName, String nutrientOrdinal) {
        super(source, className, id);
        this.name = name;
        this.elementalName = elementalName;
        this.nutrientOrdinal = nutrientOrdinal;
    }

    public String getName() {
        return name;
    }

    public String getElementalName() {
        return elementalName;
    }

    public String getNutrientOrdinal() {
        return nutrientOrdinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nutrient nutrient = (Nutrient) o;
        return Objects.equals(name, nutrient.name) &&
                Objects.equals(elementalName, nutrient.elementalName) &&
                Objects.equals(nutrientOrdinal, nutrient.nutrientOrdinal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementalName, nutrientOrdinal);
    }

    @Override
    public String toString() {
        return "Nutrient{" +
                "name='" + name + '\'' +
                ", elementalName='" + elementalName + '\'' +
                ", nutrientOrdinal='" + nutrientOrdinal + '\'' +
                '}';
    }
}
