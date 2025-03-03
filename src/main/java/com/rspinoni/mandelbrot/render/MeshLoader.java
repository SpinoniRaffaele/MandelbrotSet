package com.rspinoni.mandelbrot.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class MeshLoader {
  private static List<Integer> vaos = new ArrayList<>();
  private static List<Integer> vbos = new ArrayList<>();

  public static Mesh createMesh(float[] positions, int[] indices) {
    int vao = genVAO();
    storeData(0,3,positions);
    bindIndices(indices);
    GL30.glBindVertexArray(0);
    return new Mesh(vao,indices.length);
  }

  private static FloatBuffer createFloatBuffer(float[] data) {
    FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
    buffer.put(data);
    buffer.flip();
    return buffer;
  }

  private static IntBuffer createIntBuffer(int[] data) {
    IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
    buffer.put(data);
    buffer.flip();
    return buffer;
  }

  private static void storeData(int attribute, int dimensions, float[] data) {
    int vbo = GL15.glGenBuffers();
    vbos.add(vbo);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
    FloatBuffer buffer = createFloatBuffer(data);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    GL20.glVertexAttribPointer(attribute, dimensions, GL11.GL_FLOAT, false, 0, 0);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }

  private static void bindIndices(int[] data) {
    int vbo = GL15.glGenBuffers();
    vbos.add(vbo);
    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
    IntBuffer buffer = createIntBuffer(data);
    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
  }

  private static int genVAO() {
    int vao = GL30.glGenVertexArrays();
    vaos.add(vao);
    GL30.glBindVertexArray(vao);
    return vao;
  }
}
