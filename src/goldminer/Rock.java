package goldminer;

import util.ImageTools;

import java.awt.image.BufferedImage;

public class Rock extends Entity<Rock> {
    static final int RADIUS = 70;
    static final BufferedImage IMAGE = ImageTools.shrinkTo(ImageTools.getImageFromRes("/rock.png"), 140, 100);

    Rock(int x, int y) {
        super(x, y, Rock.RADIUS);
    }

    private Rock() {

    }

    @Override
    public Rock copy() {
        Rock result = new Rock();
        result.x = super.x;
        result.y = super.y;
        result.radius = super.radius;
        return result;
    }
}
