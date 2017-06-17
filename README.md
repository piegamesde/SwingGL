# SwingGL
Swing to LWJGL compatibility inspired by [LWJGL-FX](https://github.com/Spasi/LWJGL-FX)

## How it works

 - The class GLPanel represents the OpenGL context in Swing as JPanel.
 - Rendering is done to a texture using a frame buffer object.
 - That rendered texture is read and written to a BufferedImage.
 - That BufferedImage is drawn when the component repaints (that might happen with some delay).

## How to use it

 - To create the OpenGL context, create a window using GLFW as usual and don't make it visible.
 - Create a GLPanel object and pass a Runnable responsible for rendering to it.
 - The panel takes care of handling the frame buffer object. Just pass a runnable that is called on every rendering and everything will be fine.
 - If you set up your OpenGL context on the AWT thread, you can set a flag on the panel that will render on every component repaint.
 - Alternatively, call render() every time you want draw something new. Changes to the image will only become visible when the component repaints, call repaint() to request this.
 - **Please don't use it.**

## Limitations/Help

 - This is slow, and performance decreases with increasing resolution. For some small static OpenGL in a larger GUI, it should be fine though.
 - At the moment, the bottleneck is the BufferedImage. We need an Image object to be able to draw images using Java2D (Graphics2D class), and BufferedImage seems the only way to achieve this. Having read the pixels from the texture (stored as int[]), calling setRGB on the image is the slow thing. BufferedImage loops throw every pixel (!), decodes its color components and reencodes them in the right order for internal storage (!), and then puts it pixel per pixel into some deeply abstract Raster thing.
 - If you know some faster way to draw pixels in form of an int array in Java, **please tell it**.
 - If you know a way that makes this project obsolete, **please tell it**. Ways that don't count: LWJGL 2, JFXGL, LWJGL-FX.