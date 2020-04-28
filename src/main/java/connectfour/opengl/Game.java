package connectfour.opengl;

import connectfour.Controller;
import connectfour.util.Timer;
import connectfour.ai.HumanPlayer;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * Creates an openGL context
 */
public class Game extends Thread {

    private boolean running = true;
    private boolean playing = true;
    private static final int width = 640;
    private static final int height = 360;

    private final Controller controller;

    private Camera camera;
    private Cursor cursor;

    private Timer timer;

    public Game(Controller controller) {
        this.controller = controller;
    }

    /**
     * Starts the openGL context
     */
    public void run() {
        init();
        while (running) {
            logic();
            render();
        }
        Display.destroy();
    }

    /**
     * Stops the OpenGL context safely.
     */
    public void gameEnd() {
        running = false;
    }

    /**
     * Initialises the OpenGL context.
     */
    public void init() {
        timer = new Timer();
        timer.init();

        camera = new Camera();
        cursor = new Cursor();

        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, (float) width / (float) height, 0.1f, 100f);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    /**
     * Called every tick to process the mouse and keyboard input
     */
    public void logic() {
        if (Display.isCloseRequested()) {
            running = false;
        }

        while (Keyboard.next() && Keyboard.getEventKeyState() && controller.getPlayer() instanceof HumanPlayer) {
            if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
                cursor.moveLeft();
            } else if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
                cursor.moveRight();
            }

            if (playing && Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                ((HumanPlayer) controller.getPlayer()).makeMove(cursor.getPosition());
            }
        }

        camera.processMouse();
        timer.update();
    }

    /**
     * Called every tick to render the frame.
     */
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        camera.applyTranslations();

        if (controller.getBoard() != null) {
            RenderHelper.drawBoard(controller.getBoard());

            if (controller.getPlayer() instanceof HumanPlayer) {
                RenderHelper.drawCursor(cursor, controller.getPlayer().getColour() == 1 ? Color.YELLOW : Color.RED);
                RenderHelper.drawHint(controller.getPlayer().getHint());
            }
        }

        Display.update();
    }
}
