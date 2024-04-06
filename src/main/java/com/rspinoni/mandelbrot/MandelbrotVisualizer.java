package com.rspinoni.mandelbrot;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Arrays;

import com.rspinoni.mandelbrot.helper.LWJGLHelper;
import com.rspinoni.mandelbrot.helper.MouseControlHelper;
import com.rspinoni.mandelbrot.render.Render;
import com.rspinoni.mandelbrot.math.Vector4;
import com.rspinoni.mandelbrot.render.Mesh;
import com.rspinoni.mandelbrot.render.MeshLoader;
import com.rspinoni.mandelbrot.render.shader.Shader;

public class MandelbrotVisualizer {

  private final LWJGLHelper lwjglHelper = new LWJGLHelper();

  private final MouseControlHelper mouseControlHelper = new MouseControlHelper();

  private int height;

  private int width;

  private Vector4 ranges = new Vector4(0.10f, 0.5f, 0.9f, 1.00f);

  private final Render<Mesh, Shader> render = (Mesh mesh, Shader shader) -> {
    loadUniforms(shader);
    lwjglHelper.renderTriangleMesh(mesh);
    ranges = findRanges(lwjglHelper.getPixelData(width, height));
  };

  public void run() {
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    width = gd.getDisplayMode().getWidth();
    height = gd.getDisplayMode().getHeight();
    long window = lwjglHelper.init(width, height);
    mouseControlHelper.createMouseCallbacks(window);
    Mesh mesh = createMesh();
    Shader shader = new Shader("mandelbrot.vert", "mandelbrot.frag");
    shader.start();

    lwjglHelper.renderLoop(window, render, mesh, shader);

    shader.stop();
    lwjglHelper.tearDown(window);
  }

  private Mesh createMesh() {
    float[] vertices = {
        -1.0f, -1.0f, -0.0f,
        1.0f,  1.0f, -0.0f,
        -1.0f,  1.0f, -0.0f,
        1.0f, -1.0f, -0.0f
    };
    int[] indices = {
        0, 1, 2,
        0, 3, 1
    };
    return MeshLoader.createMesh(vertices,indices);
  }

  private void loadUniforms(Shader shader) {
    shader.loadFloat("zoom", mouseControlHelper.getZoom());
    shader.loadFloat("center_x", mouseControlHelper.getCenterX());
    shader.loadFloat("center_y", mouseControlHelper.getCenterY());
    shader.loadVector("color_ranges", ranges);
  }

  private Vector4 findRanges(FloatBuffer pixelData) {
    float[] arrayPixelData = pixelData.array();
    Arrays.sort(arrayPixelData);
    int lowest = 0;
    while (arrayPixelData[lowest] == 0.0f && lowest < arrayPixelData.length - 1) {
      lowest++;
    }
    int length = arrayPixelData.length - lowest;
    return lowest < arrayPixelData.length - 1  ? new Vector4(
        arrayPixelData[lowest],
        arrayPixelData[lowest + length * 3 / 4 - 1],
        arrayPixelData[lowest + length * 7 / 8 - 1],
        arrayPixelData[arrayPixelData.length - 1]
    ) : ranges;
  }
}
