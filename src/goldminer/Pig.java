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

    @Override
    int getDistance(Hook hook) {
        double rad = hook.getRadByTime(hook.pendingBeginTime);
        int deltaY = this.y - hook.y;
        double rad0 = Math.atan((double) (hook.x - 60) / (double) deltaY);
        double rad1 = Math.atan((double) (1800 - hook.x) / (double) deltaY);
        if (rad > rad0 || rad < -rad1) {
            return -1;
        } else {
            int deltaX = (int) (deltaY * Math.tan(rad));
            int x0 = hook.x - deltaX;
            int y0 = this.y;
            int distance = (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY) - Hook.IMAGE.getHeight() / 2;
            long timeDelta = (long) (distance / Hook.DOWN_SPEED);
            int x1 = this.getX(hook.pendingBeginTime + 200 + timeDelta);
            int y1 = this.getY(hook.pendingBeginTime + 200 + timeDelta);
            double flag = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
            if (flag < Pig.RADIUS + Hook.IMAGE.getWidth() / 3) {
                return distance;
            } else {
                return -1;
            }
        }
    }
}
