package goldminer;

import util.Coordinate;
import util.ImageTools;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * pendingBeginTime           ms ~ pendingBeginTime + 200     ms : freeze
 * pendingBeginTime + 200     ms ~ pendingIntersectTime       ms : move down
 * pendingIntersectTime       ms ~ pendingIntersectTime + 200 ms : freeze
 * pendingIntersectTime + 200 ms ~ pendingEndTime             ms : move up
 * pendingEndTime             ms ~ pendingEndTime + 200       ms : freeze
 * <p>
 * using pendingBeginTime as flag
 */
public class Hook implements Cloneable, GUI.Paintable {
    static final int MAX_DEGREE = 88;
    static final BufferedImage IMAGE
            = ImageTools.shrinkTo(ImageTools.getImageFromRes("/hook.png"), 70, 50);
    static final double DOWN_SPEED = 0.5;

    int x;
    int y;
    long pendingBeginTime;
    long pendingIntersectTime;
    long pendingEndTime;
    double pendingRad;

    int pendingEntityId;

    Hook(int x, int y) {
        this.x = x;
        this.y = y;
        this.pendingBeginTime = 0;
        this.pendingIntersectTime = 0;
        this.pendingEndTime = -200;
        this.pendingEntityId = -1;
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (time < this.pendingBeginTime) {
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ImageTools.rotateByRad(Hook.IMAGE, this.pendingRad, c);
            g.drawImage(image, this.x - c.x, this.y - c.y, null);
        } else if (time >= this.pendingEndTime + 200) {
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ImageTools.rotateByRad(Hook.IMAGE, this.getRadByTime(time), c);
            g.drawImage(image, this.x - c.x, this.y - c.y, null);
        } else {
            double rad = this.pendingRad;
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ImageTools.rotateByRad(Hook.IMAGE, rad, c);
            if (time < this.pendingBeginTime + 200) {
                g.drawImage(image, this.x - c.x, this.y - c.y, null);
            } else if (time < this.pendingIntersectTime) {
                int len = (int) ((time - this.pendingBeginTime - 200) * Hook.DOWN_SPEED);
                int deltaX = (int) (len * Math.sin(-rad));
                int deltaY = (int) (len * Math.cos(-rad));
                g.drawImage(image, this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(5));
                g2.draw(new Line2D.Float(this.x, this.y, this.x + deltaX, this.y + deltaY));
            } else if (time < this.pendingIntersectTime + 200) {
                int len = (int) ((this.pendingIntersectTime - this.pendingBeginTime - 200) * Hook.DOWN_SPEED);
                int deltaX = (int) (len * Math.sin(-rad));
                int deltaY = (int) (len * Math.cos(-rad));
                if (this.pendingEntityId >= 0) {
                    g.drawImage(state.entities.get(this.pendingEntityId).getIMAGE(),
                            this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                }
                g.drawImage(image, this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(5));
                g2.draw(new Line2D.Float(this.x, this.y, this.x + deltaX, this.y + deltaY));
            } else if (time < this.pendingEndTime) {
                int totalLen = (int) ((this.pendingIntersectTime - this.pendingBeginTime - 200) * Hook.DOWN_SPEED);
                int len = (int) (totalLen * (this.pendingEndTime - time)
                        / (this.pendingEndTime - 200 - this.pendingIntersectTime));
                int deltaX = (int) (len * Math.sin(-rad));
                int deltaY = (int) (len * Math.cos(-rad));
                if (this.pendingEntityId >= 0) {
                    g.drawImage(state.entities.get(this.pendingEntityId).getIMAGE(),
                            this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                }
                g.drawImage(image, this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(5));
                g2.draw(new Line2D.Float(this.x, this.y, this.x + deltaX, this.y + deltaY));
            } else if (time < this.pendingEndTime + 200) {
                g.drawImage(image, this.x - c.x, this.y - c.y, null);
            }
        }
    }

    @Override
    protected Hook clone() throws CloneNotSupportedException {
        Hook result = (Hook) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.pendingBeginTime = this.pendingBeginTime;
        result.pendingIntersectTime = this.pendingIntersectTime;
        result.pendingEndTime = this.pendingEndTime;
        result.pendingRad = this.pendingRad;
        return result;
    }

    double getRadByTime(long time) {
        assert time >= this.pendingBeginTime;
        if (time >= this.pendingBeginTime && time < this.pendingEndTime + 200) {
            return this.pendingRad;
        } else {
            time = time - this.pendingEndTime - 200 + 1000;
        }
        time %= 2000;
        time = time > 1000 ? 2000 - time : time;
        return ((Hook.MAX_DEGREE * time * time * (1500 - time) / 250000 - Hook.MAX_DEGREE * 1000) / 180000.0) * Math.PI;
    }

    public int getMaxDistance(Double rad) {
        double rad0 = Math.atan((double) this.x / (double) (1080 - this.y));
        double rad1 = Math.atan((double) (1920 - this.x) / (double) (1080 - this.y));
        int r;
        if (rad < rad0 && rad > -rad1) {
            r = (int) (this.x / Math.cos(rad));
        } else if (rad >= rad0) {
            r = (int) (this.x / Math.sin(rad));
        } else {
            r = (int) ((1920 - this.x) / Math.sin(-rad));
        }
        return r - Hook.IMAGE.getHeight() * 3 / 2;
    }
}
