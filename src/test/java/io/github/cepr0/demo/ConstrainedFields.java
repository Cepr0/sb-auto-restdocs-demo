package io.github.cepr0.demo;

import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.util.stream.Collectors;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

public class ConstrainedFields {

    private final ConstraintDescriptions constraintDescriptions;

    public ConstrainedFields(Class<?> input) {
        this.constraintDescriptions = new ConstraintDescriptions(input);
    }

    public FieldDescriptor name(String path) {
        return fieldWithPath(path).attributes(key("constraints").value(
                this.constraintDescriptions
                        .descriptionsForProperty(path).stream()
                        .collect(Collectors.joining(". +\n", "", "."))));
    }
}