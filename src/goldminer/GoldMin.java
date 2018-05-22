package goldminer;

import util.ResTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GoldMin extends Entity {
    static final int RADIUS = 25;
    static final int SPEED_FACTOR = 1;
    static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes(
            "/gold.png"), 50, 50);

    GoldMin(int x, int y) {
        super(x, y, GoldMin.SPEED_FACTOR, GoldMin.RADIUS);
    }

    private GoldMin() {
    }

    @Override
    protected GoldMin clone() throws CloneNotSupportedException {
        return (GoldMin) super.clone();
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.takenTime) {
            g.drawImage(GoldMin.IMAGE,
                    this.x - GoldMin.IMAGE.getWidth() / 2, this.y - GoldMin.IMAGE.getHeight() / 2, null);
        }
    }

    @Override
    public BufferedImage getImage(long time) {
        return GoldMin.IMAGE;
    }
}
