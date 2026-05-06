package com.steeplesoft.modulegraph.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record Condition(
    @JacksonXmlProperty(localName = "property-equal") String propertyEqual,
    @JacksonXmlProperty(localName = "property-not-equal") String propertyNotEqual) {
}
