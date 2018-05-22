package goldminer;

import util.FP;

public class Main {
    public static void main(String[] argv) {
        State.getInstance().init();
        GUI gui = new GUI();
        gui.startTimerTask(30);
        gui.beginWelcomeScreen();
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        gui.beginGameScreen();
    }
}
