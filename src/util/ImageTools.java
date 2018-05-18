package util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageTools {
    /**
     * Shrink src to new image with width x height.
     * @param src
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage shrinkTo(BufferedImage src, int width, int height) {
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        dst.getGraphics().drawImage(src, 0, 0, width, height, null);
        return dst;
    }

    /**
     * Rotate src by rad, and convert coo to corresponding coordinate in rotated image.
     *
     * @param src
     * @param rad
     * @param coo
     * @return
     */
    public static BufferedImage rotateByRad(BufferedImage src, double rad, Coordinate coo) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        // convert image
        int dstWidth = (int) Math.floor(srcWidth * cos + srcHeight * sin);
        int dstHeight = (int) Math.floor(srcWidth * sin + srcHeight * cos);
        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.translate((dstWidth - srcWidth) / 2, (dstHeight - srcHeight) / 2);
        g.rotate(rad, srcWidth / 2, srcHeight / 2);
        g.drawRenderedImage(src, null);
        // convert coordinate
        int newX;
        int newY;
        if (rad > 0) {
            newX = (int) (coo.x * cos + (srcHeight - coo.y) * sin);
            newY = (int) (coo.x * sin + coo.y * cos);
        } else if (rad < 0) {
            newX = (int) (coo.x * cos + coo.y * sin);
            newY = (int) ((srcWidth - coo.x) * sin + coo.y * cos);
        } else {
            newX = coo.x;
            newY = coo.y;
        }
        coo.x = newX;
        coo.y = newY;
        return dst;
    }
}
