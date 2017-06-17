package de.piegames.swinggl.demo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JFrame;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import de.piegames.swinggl.GLPanel;

/**
 * Renders a red quad onto the screen, constantly rotating over time. Rendering happens in a loop on the main Thread with maximum fps.
 */
public class PanelTestActive {

	private JFrame				frame;
	private GLPanel				panel;

	// We need to strongly reference callback instances.
	private GLFWErrorCallback	errorCallback;
	private Callback			debugProc;

	// The window handle
	private long				window;
	private int					width, height;

	private QuadRenderer		renderer;

	public void run() {
		try {
			init();
			loop();

			// Release window and window callbacks
			// glfwDestroyWindow(window);
			if (debugProc != null)
				debugProc.free();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Terminate GLFW and release the GLFWerrorfun
			// glfwTerminate();
			errorCallback.free();
			// Force exit because somehow it does not do that after errors
			System.exit(0);
		}
	}

	private void init() {
		frame = new JFrame("OpenGL canvas test");
		frame.setDefaultCloseOperation(3);
		frame.setPreferredSize(new Dimension(420, 300));
		panel = new GLPanel(() -> render(), false);
		frame.add(panel);
		frame.pack();

		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure our window
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		// Create the window
		window = glfwCreateWindow(1, 1, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		panel.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				width = e.getComponent().getWidth();
				height = e.getComponent().getHeight();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

		glfwSetWindowPos(window, 0, 0);

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);

		// Make the window visible
		// glfwShowWindow(window);
		Thread.yield();
		// GL.create();

		GL.createCapabilities();
		debugProc = GLUtil.setupDebugMessageCallback();

		renderer = new QuadRenderer();
		renderer.init();

		frame.setVisible(true);
		frame.setVisible(false);
		frame.setVisible(true);
		frame.requestFocus();
		width = panel.getWidth();
		height = panel.getHeight();

		// Set the clear color
		glClearColor(0.2f, 0.2f, 0.6f, 1.0f);
	}

	private void loop() {
		while (!glfwWindowShouldClose(window) && frame.isVisible()) {
			panel.render();
			panel.repaint();
			Thread.yield();
		}
	}

	protected void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();
		glViewport(0, 0, width, height);
		glOrtho(-1, 1, -1, 1, -1, 1);
		renderer.render((System.nanoTime() / 1000f));
	}

	public static void main(String[] args) {
		new PanelTestActive().run();
	}

}