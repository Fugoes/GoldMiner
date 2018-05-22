package goldminer;

import util.Coordinate;
import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Pig extends Entity {
    static final int RADIUS = 36;
    static final int SPEED_FACTOR = 1;
    static final BufferedImage IMAGE_LEFT = ImageTools.shrinkTo(ImageTools.getImageFromRes(
            "/pig.png"), 75, 50);
    static final BufferedImage IMAGE_RIGHT = ImageTools.flipByY(ImageTools.shrinkTo(ImageTools.getImageFromRes(
            "/pig.png"), 75, 50));
    static final int PERIOD = 8000;

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
            BufferedImage image = this.getImage(time);
            g.drawImage(
                    image,
                    this.getX(time) - image.getWidth() / 2,
                    this.getY(time) - image.getHeight() / 2,
                    null
            );
        }
    }

    @Override
    public BufferedImage getImage(long time) {
        time += this.x * Pig.PERIOD / 1800 / 2;
        time %= Pig.PERIOD;
        if (time > Pig.PERIOD / 2) {
            return Pig.IMAGE_LEFT;
        } else {
            return Pig.IMAGE_RIGHT;
        }
    }

    @Override
    int getX(long time) {
        time += this.x * Pig.PERIOD / 1800 / 2;
        time %= Pig.PERIOD;
        time = time > Pig.PERIOD / 2 ? Pig.PERIOD - time : time;
        return (int) (time * 1800 * 2 / Pig.PERIOD + 60);
    }

    @Override
    int getY(long time) {
        return this.y;
    }
}
