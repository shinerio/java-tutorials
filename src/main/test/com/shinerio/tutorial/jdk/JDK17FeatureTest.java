package com.shinerio.tutorial.jdk;

import org.junit.jupiter.api.Test;

public class JDK17FeatureTest {

    @Test
    public void testSwitch() {
        System.out.println(formatterPatternSwitch("a"));
    }

    // Old code
    static String formatter(Object o) {
        String formatted = "unknown";
        if (o == null) {
            formatted = "null";
        } else if (o instanceof String s) {
            formatted = String.format("String %s", s);
        } else if (o instanceof Integer i) {
            formatted = String.format("int %d", i);
        } else if (o instanceof Long l) {
            formatted = String.format("long %d", l);
        } else if (o instanceof Double d) {
            formatted = String.format("double %f", d);
        } else {
            formatted = o.toString();
        }
        if (o instanceof Integer i) {
            formatted = String.format("int %d", i);
        } else if (o instanceof Long l) {
            formatted = String.format("long %d", l);
        } else if (o instanceof Double d) {
            formatted = String.format("double %f", d);
        } else if (o instanceof String s) {
            formatted = String.format("String %s", s);
        }
        return formatted;
    }

    // New code
    static String formatterPatternSwitch(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i -> String.format("int %d", i);
            case Long l    -> String.format("long %d", l);
            case Double d  -> String.format("double %f", d);
            case String s  -> String.format("String %s", s);
            default        -> o.toString();
        };
    }
}
