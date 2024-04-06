package com.rspinoni.mandelbrot.helper;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

public class MouseControlHelper {

  private boolean mousePressed = false;

  private float initial_x = 0.0f;

  private float initial_y = 0.0f;

  private float zoom = 1.0f;

  private float center_x = -0.5f;

  private float center_y = 0.0f;

  public void createMouseCallbacks(long window) {
    glfwSetScrollCallback(window, (_window, xoffset, yoffset) -> {
      updateZoom((float) yoffset);
    });
    glfwSetMouseButtonCallback(window, (_window, button, action, mods) -> {
      if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        mousePressed = true;
      }
      if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
        mousePressed = false;
        initial_x = 0.0f;
        initial_y = 0.0f;
      }
    });
    glfwSetCursorPosCallback(window, (_window, xpos, ypos) -> {
      if (mousePressed) {
        updatePosition((float) xpos, (float) ypos);
      }
    });
  }

  public float getZoom() {
    return zoom;
  }

  public float getCenterX() {
    return center_x;
  }

  public float getCenterY() {
    return center_y;
  }

  private void updateZoom(float yoffset) {
    if (zoom <= 1f && yoffset > 0 || zoom < 1f && yoffset < 0) {
      zoom = (float) Math.exp(-1 * ((-1 * Math.log(zoom)) + (yoffset / 10)));
    } else {
      zoom -= yoffset / 10;
    }
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
}
