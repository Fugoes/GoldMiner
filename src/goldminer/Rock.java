package goldminer;

import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Rock extends Entity {
    static final int RADIUS = 80;
    static final int SPEED_FACTOR = 5;
    static final BufferedImage IMAGE = ImageTools.shrinkTo(ImageTools.getImageFromRes("/rock.png"), 140, 100);

    Rock(int x, int y) {
        super(x, y, Rock.RADIUS, Rock.SPEED_FACTOR);
    }

    private Rock() {
    }

    @Override
    protected Rock clone() throws CloneNotSupportedException {
        return (Rock) super.clone();
    }

    @Override
    public void paint(Graphics g, long time) {
        g.drawImage(Rock.IMAGE, this.x - Rock.IMAGE.getWidth() / 2, this.y - Rock.IMAGE.getHeight() / 2, null);
    }

    @Override
    int getDistance(Hook hook, double rad) {
        int deltaX = this.x - hook.x;
        int deltaY = this.y - hook.y;
        double distance = Math.abs((deltaX + Math.tan(rad) * deltaY) * Math.cos(rad));
        if (distance < this.radius + Hook.IMAGE.getWidth() / 2) {
            int r = (int) Math.sqrt((double) (deltaX * deltaX + deltaY * deltaY));
            return r - this.radius - Hook.IMAGE.getHeight() / 2;
        } else {
            return -1;
        }
    }
}
