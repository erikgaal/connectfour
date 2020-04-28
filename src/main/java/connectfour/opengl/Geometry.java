package connectfour.opengl;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class Geometry {

    /**
     * Draws a circle according to the given parameters
     * @param x position
     * @param y position
     * @param z position
     * @param size radius
     * @param color colour
     * @param quality amount of steps for the circle
     */
    public static void drawCircle(float x, float y, float z, float size, Color color, int quality) {
        glBegin(GL_POLYGON);
        glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        for (int i = 0; i < quality; i++) {
            double r = 2 * Math.PI * i / quality;
            glVertex3f((float) (x + .5 * size * (1 + Math.cos(r))), (float) (y + .5 * size * (1 + Math.sin(r))), z);
        }
        glEnd();
    }

    /**
     * Draws a disk, two circles and a strip around it.
     * @param x position
     * @param y position
     * @param z position
     * @param size radius
     * @param depth depth
     * @param color colour
     * @param quality amount of steps for the circle
     */
    public static void drawDisk(float x, float y, float z, float size, float depth, Color color, int quality) {
        drawCircle(x, y, 0, size, color.brighter(), quality);
        glBegin(GL_QUAD_STRIP);
        for (int i = 0; i < quality; i++) {
            double r = 2 * Math.PI * i / quality;
            glColor3f(color.brighter().getRed() / 255f, color.brighter().getGreen() / 255f, color.brighter().getBlue() / 255f);
            glVertex3f((float) (x + .5 * size * (1 + Math.cos(r))), (float) (y + .5 * size * (1 + Math.sin(r))), z);
            glColor3f(color.darker().getRed() / 255f, color.darker().getGreen() / 255f, color.darker().getBlue() / 255f);
            glVertex3f((float) (x + .5 * size * (1 + Math.cos(r))), (float) (y + .5 * size * (1 + Math.sin(r))), z + depth);
        }
        glEnd();
        drawCircle(x, y, z + depth, size, color.darker(), quality);
    }

    /**
     * Draws a quare with a hole.
     * @param x position
     * @param y position
     * @param z position
     * @param size size
     * @param radius radius of the hole
     * @param color colour
     * @param quality amount of steps for the circle
     */
    public static void drawQuadHole(float x, float y, float z, float size, float radius, Color color, int quality) {
        glBegin(GL_TRIANGLE_STRIP);
        glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        for (int i = 0; i <= quality; i++) {
            double r = 2 * Math.PI * i / quality;
            glVertex3f((float) (x + .5 * size + Math.cos(r) * .5 * radius), (float) (y + .5 * size + Math.sin(r) * .5 * radius), z);
            if (r < Math.PI * 1 / 2) glVertex3f(x + size, y + size, z);
            else if (r < Math.PI * 2 / 2) glVertex3f(x, y + size, z);
            else if (r < Math.PI * 3 / 2) glVertex3f(x, y, z);
            else if (r < Math.PI * 4 / 2) glVertex3f(x + size, y, z);
        }
        glVertex3f(x + size, y + size, z);
        glEnd();
    }

    /**
     * Draws a tube
     * @param x position
     * @param y position
     * @param z position
     * @param size size of square
     * @param radius radius of hole
     * @param depth depth
     * @param color colour
     * @param quality amount of steps for the circle
     */
    public static void drawTube(float x, float y, float z, float size, float radius, float depth, Color color, int quality) {
        drawQuadHole(x, y, z, size, radius, color.brighter(), quality);
        glBegin(GL_QUAD_STRIP);
        for (int i = 0; i <= quality; i++) {
            double r = 2 * Math.PI * i / quality;
            glColor3f(color.brighter().getRed() / 255f, color.brighter().getGreen() / 255f, color.brighter().brighter().getBlue() / 255f);
            glVertex3f((float) (x + .5 * size + Math.cos(r) * .5 * radius), (float) (y + .5 * size + Math.sin(r) * .5 * radius), z);
            glColor3f(color.darker().getRed() / 255f, color.darker().getGreen() / 255f, color.darker().darker().getBlue() / 255f);
            glVertex3f((float) (x + .5 * size + Math.cos(r) * .5 * radius), (float) (y + .5 * size + Math.sin(r) * .5 * radius), z + depth);
        }
        glEnd();
        glBegin(GL_TRIANGLE_STRIP);
        glVertex3f(x, y, z);
        glVertex3f(x, y, z + depth);
        glVertex3f(x + size, y, z);
        glVertex3f(x + size, y, z + depth);
        glVertex3f(x + size, y + size, z);
        glVertex3f(x + size, y + size, z + depth);
        glVertex3f(x, y + size, z);
        glVertex3f(x, y + size, z + depth);
        glEnd();
        drawQuadHole(x, y, z + depth, size, radius, color.darker(), quality);
    }
}