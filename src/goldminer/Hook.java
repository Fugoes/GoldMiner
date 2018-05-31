package goldminer;

import util.Coordinate;
import util.ResTools;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
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
            = ResTools.shrinkTo(ResTools.getImageFromRes("/hook.png"), 70, 50);
    static final BufferedImage IMAGE_BOY
            = ResTools.shrinkTo(ResTools.getImageFromRes("/boy.png"), 60, 130);
    static final BufferedImage IMAGE_GIRL
            = ResTools.shrinkTo(ResTools.getImageFromRes("/girl.png"), 60, 130);
    static final BufferedImage IMAGE_BASE
            = ResTools.shrinkTo(ResTools.getImageFromRes("/base.png"), 100, 50);
    static final BufferedImage IMAGE_GROUND
            = ResTools.shrinkTo(ResTools.getImageFromRes("/ground.png"), 1920, 10);
    static final double DOWN_SPEED = 0.5;

    int playerID;
    int x;
    int y;
    long pendingBeginTime;
    long pendingIntersectTime;
    long pendingEndTime;
    double pendingRad;

    int pendingEntityId;

    Hook(int playerID, int x, int y) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
        this.pendingBeginTime = 0;
        this.pendingIntersectTime = 0;
        this.pendingEndTime = -200;
        this.pendingEntityId = -1;
    }

    @Override
    protected Hook clone() throws CloneNotSupportedException {
        Hook result = (Hook) super.clone();
        result.playerID = this.playerID;
        result.x = this.x;
        result.y = this.y;
        result.pendingBeginTime = this.pendingBeginTime;
        result.pendingIntersectTime = this.pendingIntersectTime;
        result.pendingEndTime = this.pendingEndTime;
        result.pendingRad = this.pendingRad;
        result.pendingEntityId = this.pendingEntityId;
        return result;
    }

    @Override
    public void paint(Graphics g, State state, long time) {
        if (this.playerID == 0) {
            g.drawImage(
                    Hook.IMAGE_BOY,
                    this.x - Hook.IMAGE_BOY.getWidth(),
                    this.y - Hook.IMAGE_BOY.getHeight(),
                    null
            );
            g.drawImage(
                    Hook.IMAGE_GROUND,
                    0,
                    214,
                    null
            );
        } else {
            g.drawImage(
                    Hook.IMAGE_GIRL,
                    this.x + Hook.IMAGE_GIRL.getWidth() / 2 - 10,
                    this.y - Hook.IMAGE_GIRL.getHeight(),
                    null
            );
        }
        g.drawImage(
                Hook.IMAGE_BASE,
                this.x - Hook.IMAGE_BASE.getWidth() / 2,
                this.y - Hook.IMAGE_BASE.getHeight() + 20,
                null
        );
        if (time < this.pendingBeginTime) {
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ResTools.rotateByRad(Hook.IMAGE, this.pendingRad, c);
            g.drawImage(image, this.x - c.x, this.y - c.y, null);
        } else if (time >= this.pendingEndTime + 200) {
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ResTools.rotateByRad(Hook.IMAGE, this.getRadByTime(time), c);
            g.drawImage(image, this.x - c.x, this.y - c.y, null);
        } else {
            double rad = this.pendingRad;
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ResTools.rotateByRad(Hook.IMAGE, rad, c);
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
                    BufferedImage entityImage = state.entities.get(this.pendingEntityId).getImage(time);
                    g.drawImage(
                            entityImage,
                            this.x + deltaX - entityImage.getWidth() / 2,
                            this.y + deltaY,
                            null
                    );
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
                    BufferedImage entityImage = state.entities.get(this.pendingEntityId).getImage(time);
                    g.drawImage(
                            entityImage,
                            this.x + deltaX - entityImage.getWidth() / 2,
                            this.y + deltaY,
                            null
                    );
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
            r = Integer.min((int) (this.x / Math.cos(rad)), (int) ((1920 - this.x) / Math.cos(rad)));
        } else if (rad >= rad0) {
            r = (int) (this.x / Math.sin(rad));
        } else {
            r = (int) ((1920 - this.x) / Math.sin(-rad));
        }
        return r - Hook.IMAGE.getHeight() * 3 / 2;
    }
}
