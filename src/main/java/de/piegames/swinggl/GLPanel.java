package de.piegames.swinggl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;

public class GLPanel extends JPanel implements ComponentListener {

	private static final long	serialVersionUID	= 1103705900601136733L;

	/**
	 * If the context was created on the AWT Event dispatch Thread, set this to true. Do this is you want to render only if needed (on frame repaint). If you
	 * are rendering from a loop, leave this to false.
	 */
	public final boolean		renderOnAWTThread;
	private final Runnable		renderRunnable;

	private AtomicBoolean		needsUpdate			= new AtomicBoolean(true);
	private int					framebufferID, colorTextureID;
	private BufferedImage		image;

	public GLPanel(Runnable renderRunnable, boolean renderOnAWTThread) {
		super(null, false);
		this.renderRunnable = Objects.requireNonNull(renderRunnable);
		this.renderOnAWTThread = renderOnAWTThread;
		addComponentListener(this);
		setDoubleBuffered(false);
	}

	/**
	 * Draws the cached {@link BufferedImage} from last rendering call, updating it before that if {@link #renderOnAWTThread} is set to true.
	 */
	@Override
	public void paintComponent(Graphics gr) {
		if (renderOnAWTThread)
			render();
		if (image != null)
			gr.drawImage(image, 0, 0, null);
	}

	/**
	 * Call this method for rendering. It will take care of handling the fbo and make sure that when calling {@code renderOnAWTThread.run()} everything will be
	 * rendered to a texture attached to that fbo. The texture will be read and written to a {@link BufferedImage} that will be used on
	 * {@link #paintComponent(Graphics)} rendering.
	 */
	public final void render() {
		if (needsUpdate == null)
			throw new IllegalStateException("Component got deinitialized and cannot be used anymore");
		if (needsUpdate.get()) {
			destroyFBO();
			createFBO();
			needsUpdate.set(false);
		}

		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureID, 0);

		renderRunnable.run();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, colorTextureID);
		// int format = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_INTERNAL_FORMAT);
		int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
		int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] buffer = new int[width * height * 4];

		glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
		image.setRGB(0, 0, width, height, buffer, 0, width);
	}

	/** Forces to recreate the frame buffer before next rendering. Called automatically if the component was resized. */
	public void update() {
		needsUpdate.set(true);
	}

	protected void createFBO() {
		// init our fbo
		framebufferID = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
		glDrawBuffer(GL_COLOR_ATTACHMENT0);

		colorTextureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, colorTextureID);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorTextureID, 0);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new IllegalStateException("An error occured during frame buffer creation, its status is " +
					Integer.toHexString(glCheckFramebufferStatus(GL_FRAMEBUFFER)) + ", not " + Integer.toHexString(GL_FRAMEBUFFER_COMPLETE));
	}

	protected void destroyFBO() {
		glDeleteFramebuffers(framebufferID);
		glDeleteTextures(colorTextureID);
	}

	/**
	 * Will destroy the frame buffer, flush all graphics resources and remove any listeners, thus cause any further rendering to throw exceptions. When
	 * deinitialiting this component, call this last.
	 */
	public void cleanup() {
		removeComponentListener(this);
		destroyFBO();
		image.flush();
		image = null;
		needsUpdate.set(false);
		needsUpdate = null;// Throw exceptions on rendering from here
	}

	@Override
	public void componentResized(ComponentEvent e) {
		update();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}