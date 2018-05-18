package goldminer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GUI {
    final Dimension vDim = new Dimension(1920, 1080);
    final Dimension rDim = new Dimension(1920, 1080);
    final BufferedImage image = new BufferedImage(vDim.width, vDim.height, BufferedImage.TYPE_INT_ARGB);

    class Frame extends JFrame {
        Frame() {
            super("Gold Miner");

        }

        @Override
        public void paint(Graphics g) {
        }
    }
}
