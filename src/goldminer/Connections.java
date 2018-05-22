package goldminer;

import util.FP;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

public class Connections {
    public interface ConnectionBase {
        void send(String string);

        String recv();

        boolean isUp();

        boolean waitForUp(int timeout);
    }

    public static class TCPServer implements ConnectionBase {
        ServerSocket serverSocket;
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        boolean isUp = false;

        TCPServer(int portNumber) {
            try {
                this.serverSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                System.exit(-1);
            }
        }

        @Override
        public void send(String string) {
            this.out.write(string + "\n");
        }

        @Override
        public String recv() {
            try {
                return this.in.readLine();
            } catch (IOException e) {
                System.exit(-1);
                return null;
            }
        }

        @Override
        public boolean waitForUp(int timeout) {
            try {
                this.serverSocket.setSoTimeout(timeout);
            } catch (IOException e) {
                System.exit(-1);
            }
            try {
                this.clientSocket = this.serverSocket.accept();
                this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (InterruptedIOException e) {
                return false;
            } catch (IOException e) {
                System.exit(-1);
            }
            this.isUp = true;
            return true;
        }

        @Override
        public boolean isUp() {
            return this.isUp;
        }
    }

    public static class TCPClient implements ConnectionBase {
        String hostname;
        Socket clientSocket;
        int port;
        PrintWriter out;
        BufferedReader in;
        boolean isUp = false;

        TCPClient(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        @Override
        public boolean waitForUp(int timeout) {
            long beginTime = Calendar.getInstance().getTimeInMillis();
            try {
                this.clientSocket = new Socket();
                this.clientSocket.connect(new InetSocketAddress(this.hostname, this.port), timeout);
                this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (InterruptedIOException e) {
                return false;
            } catch (ConnectException e) {
                FP.liftExp(() -> Thread.sleep(timeout - (Calendar.getInstance().getTimeInMillis() - beginTime)))
                        .run();
                return false;
            } catch (IOException e) {
                System.exit(-1);
            }
            this.isUp = true;
            return true;
        }

        @Override
        public void send(String string) {
            this.out.write(string + "\n");
        }

        @Override
        public String recv() {
            try {
                return this.in.readLine();
            } catch (IOException e) {
                System.exit(-1);
                return null;
            }
        }

        @Override
        public boolean isUp() {
            return this.isUp;
        }
    }
}
