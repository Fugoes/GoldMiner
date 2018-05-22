package goldminer;

import util.FP;

import java.util.Calendar;

public class Main {
    public static void main(String[] argv) {
        int playerID;
        Connections.ConnectionBase connection;
        switch (argv.length) {
            case 1:
                connection = new Connections.TCPServer(Integer.valueOf(argv[0]), );
                playerID = 0;
                break;
            case 2:
                connection = new Connections.TCPClient(argv[0], Integer.valueOf(argv[1]), System.out::println);
                playerID = 1;
                break;
            default:
                System.exit(-1);
                return;
        }

        State.getInstance().init();
        GUI gui = new GUI();
        gui.startTimerTask(30);

        long time = Calendar.getInstance().getTimeInMillis();
        gui.beginWelcomeScreen();
        if (connection.waitForUp(4000)) {
            FP.liftExp(() -> Thread.sleep(4000 - (Calendar.getInstance().getTimeInMillis() - time))).run();
        } else {
            gui.beginWaitingConnectionScreen();
            while (!connection.waitForUp(100)) ;
        }

        connection.send("PING," + Calendar.getInstance().getTimeInMillis());

        State.getInstance().start();
        gui.beginGameScreen();
    }
}
