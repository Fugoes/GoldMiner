package goldminer;

import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Pocket extends Entity {
    static final int RADIUS = 50;
    static final int SPEED_FACTOR = 2;
    static final BufferedImage IMAGE = ImageTools.shrinkTo(ImageTools.getImageFromRes(
            "/pocket.png"), 100, 100);

    Pocket(int x, int y) {
        super(x, y, Pocket.SPEED_FACTOR, Pocket.RADIUS);
    }

    private Pocket() {
    }

    @Override
    protected Entity clone() throws CloneNotSupportedException {
        return (Pocket) super.clone();
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.takenTime) {
            g.drawImage(Pocket.IMAGE,
                    this.x - GoldMid.IMAGE.getWidth() / 2, this.y - GoldMid.IMAGE.getHeight() / 2, null);
        }
    }

    @Override
    public BufferedImage getImage(long time) {
        return Pocket.IMAGE;
    }
}
