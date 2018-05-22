package goldminer;

import util.ResTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GoldMid extends Entity {
    static final int RADIUS = 50;
    static final int SPEED_FACTOR = 2;
    static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes(
            "/gold.png"), 100, 100);

    GoldMid(int x, int y) {
        super(x, y, GoldMid.SPEED_FACTOR, GoldMid.RADIUS);
    }

    private GoldMid() {
    }

    @Override
    protected GoldMid clone() throws CloneNotSupportedException {
        return (GoldMid) super.clone();
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.takenTime) {
            g.drawImage(GoldMid.IMAGE,
                    this.x - GoldMid.IMAGE.getWidth() / 2, this.y - GoldMid.IMAGE.getHeight() / 2, null);
        }
    }

    @Override
    public BufferedImage getImage(long time) {
        return GoldMid.IMAGE;
    }
}
