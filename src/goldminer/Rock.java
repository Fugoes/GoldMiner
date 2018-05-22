package goldminer;

import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Rock extends Entity {
    static final int RADIUS = 60;
    static final int SPEED_FACTOR = 2;
    static final BufferedImage IMAGE = ImageTools.shrinkTo(ImageTools.getImageFromRes("/rock.png"), 140, 100);

    Rock(int x, int y) {
        super(x, y, Rock.SPEED_FACTOR, Rock.RADIUS);
    }

    private Rock() {
    }

    @Override
    protected Rock clone() throws CloneNotSupportedException {
        return (Rock) super.clone();
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.takenTime) {
            g.drawImage(Rock.IMAGE, this.x - Rock.IMAGE.getWidth() / 2, this.y - Rock.IMAGE.getHeight() / 2, null);
        }
    }

    @Override
    public BufferedImage getImage(long time) {
        return Rock.IMAGE;
    }
}
