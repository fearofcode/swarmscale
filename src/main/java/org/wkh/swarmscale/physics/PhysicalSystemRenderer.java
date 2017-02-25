/* Originally based on dyn4j code, hence the following license header */

 /*
 * Copyright (c) 2010-2016 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wkh.swarmscale.physics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;

public class PhysicalSystemRenderer extends JFrame {

    /**
     * The scale 45 pixels per meter
     */
    public static final double SCALE = 45.0;

    /**
     * The canvas to draw to
     */
    protected Canvas canvas;

    protected final PhysicalSystem physicalSystem;

    /**
     * Default constructor for the window
     *
     * @param system The physical system to simulate and visualize
     */
    public PhysicalSystemRenderer(final PhysicalSystem system) {
        super("Physical system renderer");

        physicalSystem = system;
        /* pass ourselves to the physical system so we will render the simulation */
        physicalSystem.setRenderer(this);
        physicalSystem.setDoRender(true);
        physicalSystem.setStopped(false);
        physicalSystem.initializeWorld();

        // setup the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add a window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // before we stop the JVM stop the example
                physicalSystem.stop();
                super.windowClosing(e);
            }
        });

        // create the size of the window
        Dimension size = new Dimension(800, 600);

        // create a canvas to paint to
        canvas = new Canvas();
        canvas.setPreferredSize(size);
        canvas.setMinimumSize(size);
        canvas.setMaximumSize(size);

        // add the canvas to the JFrame
        add(canvas);

        // make the JFrame not resizable
        // (this way I dont have to worry about resize events)
        setResizable(false);

        // size everything
        pack();
    }

    /**
     * Start active rendering the example.
     * <p>
     * This should be called after the JFrame has been shown.
     */
    public void start() {
        // don't allow AWT to paint the canvas since we are
        canvas.setIgnoreRepaint(true);
        // enable double buffering (the JFrame has to be
        // visible before this can be done)
        canvas.createBufferStrategy(2);
        // run a separate thread to do active rendering
        // because we don't want to do it on the EDT
        Thread thread = new Thread() {
            public void run() {
                /* run forever */
                physicalSystem.runSimulationLoop(-1);
            }
        };
        // set the game loop thread to a daemon thread so that
        // it cannot stop the JVM from exiting
        thread.setDaemon(true);
        // start the game loop
        thread.start();
    }

    public void renderSystem() {
        // get the graphics object to render to
        Graphics2D g = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();

        // before we render everything im going to flip the y axis and move the
        // origin to the center (instead of it being in the top left corner)
        AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
        AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
        g.transform(yFlip);
        g.transform(move);

        // now (0, 0) is in the center of the screen with the positive x axis
        // pointing right and the positive y axis pointing up
        // render anything about the Example (will render the World objects)
        render(g);

        // dispose of the graphics object
        g.dispose();

        // blit/flip the buffer
        BufferStrategy strategy = canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }

        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Renders the example.
     *
     * @param g the graphics object to render to
     */
    protected void render(Graphics2D g) {
        // lets draw over everything with a white background
        g.setColor(Color.WHITE);
        g.fillRect(-400, -300, 800, 600);

        // lets move the view up some
        g.translate(0.0, -1.0 * SCALE);

        // draw all the objects in the world
        for (int i = 0; i < physicalSystem.getWorld().getBodyCount(); i++) {
            // get the object
            RenderedBody rb = (RenderedBody) physicalSystem.getWorld().getBody(i);
            // draw the object
            rb.render(g);
        }
    }
}
