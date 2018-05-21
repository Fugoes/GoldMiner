package goldminer;

import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Pig extends Entity {
    static final int RADIUS = 45;
    static final int SPEED_FACTOR = 1;
    static final BufferedImage IMAGE = ImageTools.shrinkTo(ImageTools.getImageFromRes(
            "/pig.png"), 90, 60);

    Pig(int x, int y) {
        super(x, y, Pig.SPEED_FACTOR, Pig.RADIUS);
    }

    private Pig() {
    }

    @Override
    protected Pig clone() throws CloneNotSupportedException {
        return (Pig) super.clone();
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.takenTime) {
            g.drawImage(Pig.IMAGE,
                    this.x - Pig.IMAGE.getWidth() / 2, this.y - Pig.IMAGE.getHeight() / 2, null);
        }
    }

    @Override
    public BufferedImage getIMAGE() {
        return Pig.IMAGE;
    }
}
