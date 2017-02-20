package org.wkh.swarmscale.physicalexample;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import static org.wkh.swarmscale.physicalexample.PhysicalSystemRenderer.SCALE;

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
    protected Color color;

    /**
     * Default constructor.
     */
    public RenderedBody(Color color) {
        // randomly generate the color
        this.color = color;
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

        // loop over all the body fixtures for this body
        for (BodyFixture fixture : fixtures) {
            // get the shape on the fixture
            Convex convex = fixture.getShape();
            Graphics2DRenderer.render(g, convex, SCALE, color);
        }

        // set the original transform
        g.setTransform(ot);
    }
}
