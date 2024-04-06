package com.rspinoni.mandelbrot.render;

@FunctionalInterface
public interface Render<Mesh, Shader> {
  void apply(Mesh m, Shader s);
}
