package com.shinerio.tutorial.jdk;

public sealed class ColorfulShape implements Shape {
    static final class CircleColorfulShape extends ColorfulShape {
    }
}

final class RectangleColorfulShape extends ColorfulShape {
}