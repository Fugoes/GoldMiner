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
    protected Entity clone() throws CloneNotSupportedException {
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
    int getDistance(Hook hook, double rad) {
        int deltaX = this.x - hook.x;
        int deltaY = this.y - hook.y;
        double distance = Math.abs((deltaX + Math.tan(rad) * deltaY) * Math.cos(rad));
        if (distance < this.radius + Hook.IMAGE.getWidth() / 3) {
            int r = (int) Math.sqrt((double) (deltaX * deltaX + deltaY * deltaY));
            return r - Hook.IMAGE.getHeight() / 2;
        } else {
            return -1;
        }
    }

    @Override
    public BufferedImage getIMAGE() {
        return GoldMax.IMAGE;
    }
}
