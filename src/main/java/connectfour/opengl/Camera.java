package connectfour.opengl;

import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

/**
 * Camera used by the OpenGL to process mouse.
 */
public class Camera {
    private float rotation;

    private static int DISTANCE = 32;

    public Camera() {
        Mouse.setGrabbed(true);
    }

    /**
     * Processes the mouse input to change the rotation.
     */
    public void processMouse() {
        float mouseDX = Mouse.getDX() * 0.16f;
        if (rotation + mouseDX >= 360) {
            rotation = rotation + mouseDX - 360;
        } else if (rotation + mouseDX < 0) {
            rotation = 360 - rotation + mouseDX;
        } else {
            rotation += mouseDX;
        }
    }

    /**
     * Applies the rotation to the model matrix
     */
    public void applyTranslations() {
        glTranslatef(0, 0, -DISTANCE);
        glRotatef(rotation, 0, 1, 0);
    }
}
