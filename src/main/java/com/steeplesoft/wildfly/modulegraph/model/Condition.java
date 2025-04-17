package com.steeplesoft.wildfly.modulegraph.model;

import java.util.Objects;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Condition {

    @JacksonXmlProperty(localName = "property-equal")
    public String propertyEqual;
    @JacksonXmlProperty(localName = "property-not-equal")
    public String propertyNotEqual;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return Objects.equals(propertyEqual, condition.propertyEqual) &&
            Objects.equals(propertyNotEqual, condition.propertyNotEqual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyEqual, propertyNotEqual);
    }

    @Override
    public String toString() {
        return "Condition{" +
            "propertyEqual='" + propertyEqual + '\'' +
            ", propertyNotEqual='" + propertyNotEqual + '\'' +
            '}';
    }

}
