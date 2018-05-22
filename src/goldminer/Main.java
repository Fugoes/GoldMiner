package goldminer;

import java.util.Calendar;

public class Main {
    public static void main(String[] argv) {
        Connections.ConnectionBase connection;
        switch (argv.length) {
            case 1:
                connection = new Connections.TCPServer(Integer.valueOf(argv[0]));
                break;
            case 2:
                connection = new Connections.TCPClient(argv[0], Integer.valueOf(argv[1]));
                break;
            default:
                return;
        }
        State.getInstance().init();
        GUI gui = new GUI();
        gui.startTimerTask(30);
        long time = Calendar.getInstance().getTimeInMillis();
        gui.beginWelcomeScreen();
        if (connection.waitForUp(4000)) {
            try {
                Thread.sleep(4000 - (Calendar.getInstance().getTimeInMillis() - time));
            } catch (Exception e) {
            }
            State.getInstance().start();
            gui.beginGameScreen();
        } else {
            gui.beginWaitingConnectionScreen();
            while (!connection.waitForUp(100)) ;
            State.getInstance().start();
            gui.beginGameScreen();
        }
    }
}
