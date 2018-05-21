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
        return Rock.IMAGE;
    }
}
