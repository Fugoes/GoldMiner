package goldminer;

import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GoldMax extends Entity {
    static final int RADIUS = 75;
    static final int SPEED_FACTOR = 3;
    static final BufferedImage IMAGE = ImageTools.shrinkTo(ImageTools.getImageFromRes(
            "/gold.png"), 150, 150);

    GoldMax(int x, int y) {
        super(x, y, GoldMax.SPEED_FACTOR, GoldMax.RADIUS);
    }

    private GoldMax() {
    }

    @Override
    protected GoldMax clone() throws CloneNotSupportedException {
        return (GoldMax) super.clone();
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.takenTime) {
            g.drawImage(GoldMax.IMAGE,
                    this.x - GoldMax.IMAGE.getWidth() / 2, this.y - GoldMax.IMAGE.getHeight() / 2, null);
        }
    }

    @Override
    public BufferedImage getIMAGE() {
        return GoldMax.IMAGE;
    }
}
