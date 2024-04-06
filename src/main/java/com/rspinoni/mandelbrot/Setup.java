package com.rspinoni.mandelbrot;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.rspinoni.mandelbrot.math.Vector4;
import com.rspinoni.mandelbrot.render.Mesh;
import com.rspinoni.mandelbrot.render.MeshLoader;
import com.rspinoni.mandelbrot.render.shader.Shader;

public class Setup {
  // The window handle
  private long window;

  private float zoom = 1.0f;

  private float center_x = 0.0f;

  private float center_y = 0.0f;

  private float initial_x = 0.0f;

  private float initial_y = 0.0f;

  private int height;

  private int width;

  boolean mousePressed = false;

  Vector4 ranges = new Vector4(0.10f, 0.5f, 0.9f, 1.00f);

  public void run() {
    System.out.println("Hello LWJGL " + Version.getVersion() + "!");

    init();
    loop();

    // Free the window callbacks and destroy the window
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);

    // Terminate GLFW and free the error callback
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  private void init() {

    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    width = gd.getDisplayMode().getWidth();
    height = gd.getDisplayMode().getHeight();

    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if ( !glfwInit() )
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure GLFW
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

    // Create the window
    window = glfwCreateWindow(width, height, "Mandelbrot Visualizer", NULL, NULL);
    if ( window == NULL )
      throw new RuntimeException("Failed to create the GLFW window");

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
        glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
    });

    // Get the thread stack and push a new frame
    try ( MemoryStack stack = stackPush() ) {
      IntBuffer pWidth = stack.mallocInt(1); // int*
      IntBuffer pHeight = stack.mallocInt(1); // int*

      // Get the window size passed to glfwCreateWindow
      glfwGetWindowSize(window, pWidth, pHeight);

      // Get the resolution of the primary monitor
      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      // Center the window
      glfwSetWindowPos(
          window,
          (vidmode.width() - pWidth.get(0)) / 2,
          (vidmode.height() - pHeight.get(0)) / 2
      );
    } // the stack frame is popped automatically

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);
  }

  private void loop() {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();

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
    Mesh mesh = MeshLoader.createMesh(vertices,indices);
    Shader shader = new Shader("mandelbrot.vert", "mandelbrot.frag");
    shader.start();
    glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
      updateZoom((float) yoffset);
    });
    glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
      if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        mousePressed = true;
      }
      if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
        mousePressed = false;
        initial_x = 0.0f;
        initial_y = 0.0f;
      }
    });
    glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
      if (mousePressed) {
        updatePosition((float) xpos, (float) ypos);
      }
    });
    // Set the clear color
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while ( !glfwWindowShouldClose(window) ) {
      render(mesh, shader);
    }
  }

  public void render(Mesh mesh, Shader shader) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
    shader.loadFloat("zoom", zoom);
    shader.loadFloat("center_x", center_x);
    shader.loadFloat("center_y", center_y);
    shader.loadVector("color_ranges", ranges);
    //System.out.println(ranges);
    GL30.glBindVertexArray(mesh.getVaoID());
    GL20.glEnableVertexAttribArray(0);
    GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT,0);
    GL20.glDisableVertexAttribArray(0);
    GL30.glBindVertexArray(0);
    FloatBuffer pixel_data = FloatBuffer.allocate(width * height);
    glfwSwapBuffers(window);
    glfwPollEvents();
    glReadPixels(0, 0, width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixel_data);
    ranges = findRanges(pixel_data);
  }

  private void updateZoom(float yoffset) {
    zoom -= yoffset / 10;
  }

  private void updatePosition(float xpos, float ypos) {
    if (initial_x == 0.0f && initial_y == 0.0f) {
      initial_x = xpos;
      initial_y = ypos;
    }
    center_x -= (xpos - initial_x) / (1000 / zoom);
    center_y += (ypos - initial_y) / (1000 / zoom);
    initial_x = xpos;
    initial_y = ypos;
  }

  private Vector4 findRanges(FloatBuffer pixelData) {
    float[] arrayPixelData = pixelData.array();
    for (float f : arrayPixelData) {
      if (f != 0.0f) {
        System.out.println(f);
      }
    }
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
