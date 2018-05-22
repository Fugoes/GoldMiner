package goldminer;

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
        State.getInstance().start();
        gui.beginGameScreen();
    }
}
