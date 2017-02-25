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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import static org.wkh.swarmscale.physics.PhysicalSystemRenderer.SCALE;

/**
 * Custom Body class to add drawing functionality.
 *
 * @author William Bittle
 * @version 3.0.2
 * @since 3.0.0
 */
public class RenderedBody extends Body {

    /**
     * The color of the object
     */
    protected final Color color;

    private final BufferedImage image;

    /**
     * Default constructor.
     */
    public RenderedBody(Color color) {
        this.color = color;
        this.image = null;
    }

    public RenderedBody(Color color, BufferedImage image) {
        this.color = color;
        this.image = image;
    }

    /**
     * Draws the body.
     * <p>
     * Only coded for polygons and circles.
     *
     * @param g the graphics object to render to
     */
    public void render(Graphics2D g) {
        // save the original transform
        AffineTransform ot = g.getTransform();

        // transform the coordinate system from world coordinates to local coordinates
        AffineTransform lt = new AffineTransform();
        lt.translate(transform.getTranslationX() * SCALE, transform.getTranslationY() * SCALE);
        lt.rotate(transform.getRotation());

        // apply the transform
        g.transform(lt);

        /* If an image is set, assume that only one fixture exists and that an image should be drawn in its place */
        if (image != null) {
            BodyFixture singleFixture = fixtures.get(0);
            Convex convex = singleFixture.getShape();
            // check the shape type
            if (convex instanceof Rectangle) {
                Rectangle r = (Rectangle) convex;
                Vector2 c = r.getCenter();
                double w = r.getWidth();
                double h = r.getHeight();
                g.drawImage(image,
                        (int) Math.ceil((c.x - w / 2.0) * SCALE),
                        (int) Math.ceil((c.y - h / 2.0) * SCALE),
                        (int) Math.ceil(w),
                        (int) Math.ceil(h),
                        null);
            } else if (convex instanceof Circle) {
                // cast the shape to get the radius
                Circle c = (Circle) convex;
                double r = c.getRadius();
                Vector2 cc = c.getCenter();
                int x = (int) Math.ceil((cc.x - r) * SCALE);
                int y = (int) Math.ceil((cc.y - r) * SCALE);
                int w = (int) Math.ceil(r * 2 * SCALE);
                // lets us an image instead
                g.drawImage(image, x, y, w, w, null);
            }
        } else {
            // loop over all the body fixtures for this body
            for (BodyFixture fixture : fixtures) {
                // get the shape on the fixture
                Convex shape = fixture.getShape();
                Graphics2DRenderer.render(g, shape, SCALE, color);
            }
        }

        // set the original transform
        g.setTransform(ot);
    }
}
