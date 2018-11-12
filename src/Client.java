import java.net.*;
import java.io.*;
import java.time.LocalTime;
import java.util.Scanner;

public class Client implements Runnable {

    int idsesji;
    Socket socket;
    DataInputStream sin;
    DataOutputStream sout;
    byte[] data = new byte[3];
    static boolean cond = true, connected = false;

    Client(String inet, int port) {
        try {
            System.out.println("Oczekiwanie na połączenie...");
            socket = new Socket(inet, port);
            if (socket != null) {
                System.out.println("Połączono z serwerem " + inet + ":" + port);
                sin = new DataInputStream(socket.getInputStream());
                sout = new DataOutputStream(socket.getOutputStream());
            } else System.out.println("Nie można połączyć: przekroczono czas oczekiwania.");
        } catch (IOException e) {

        }

    }

    void decode(byte[] data) {
        int odpowiedź, sesja, operacja;
        odpowiedź = (data[0] & 0b00111000) >> 3;
        operacja = data[0] & 0b00000111;
        sesja = data[1];
        switch (operacja) {
            case 0:
                connected = true;
                this.idsesji = sesja;
                System.out.println("Odp: " + odpowiedź + " op: " + operacja + " sid: " + sesja);
                break;
            case 2:
                System.out.println("Przegrałeś");
                break;
            case 3:
                System.out.println("Wygrałeś");
                break;
            default:
                break;
        }

    }

    void sendAns(int liczba) {
        byte[] packet = new byte[2];

        packet[0] = 7;

        packet[0] = (byte) (packet[0] | ((liczba & 0b00000111) << 3));

        packet[1] = (byte) (idsesji & 0b00011111);

        try {
            sout.write(packet);
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            if (client.socket != null) {
                client.run();
            } else {
                cond=false;
                return;
            }
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        int liczba;
        while (socket.isConnected()) {
            try {
                if (sin.available()>0){
                    sin.read(data);
                    decode(data);
                }
            }catch (IOException e){
                System.out.println("dupa");
            }
            if (scanner.hasNext()) {
                try {
                    liczba = scanner.nextInt();
                    sendAns(liczba);
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        try{
            socket.close();
        }
        catch (IOException e){}


    }

}
