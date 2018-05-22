package goldminer;

public class Main {
    public static void main(String[] argv) {
        State.getInstance().init();
        new GUI(30);
    }
}
