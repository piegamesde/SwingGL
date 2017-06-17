package de.piegames.swinggl.demo;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

/** Class that renders a red rotated quad for testing. */
public class QuadRenderer {

	private int vbo;

	public QuadRenderer() {
	}

	public void init() {
		glEnableClientState(GL_VERTEX_ARRAY);
		float[] vertices = {
				-1, -1,
				1, -1,
				1, 1,
				-1, 1 };
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);
		glVertexPointer(2, GL_FLOAT, 0, 0L);
	}

	public void render(double rotation) {
		glPushMatrix();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		glRotated(rotation, 0, 0, 1);
		glScaled(1 / 2d, 1 / 2d, 1 / 2d);

		glColor4f(1, 0, 0, 1);
		glDrawArrays(GL_QUADS, 0, 4);
		glPopMatrix();
	}
}