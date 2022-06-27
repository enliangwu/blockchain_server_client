import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 *
 * This is a TCP Client.
 * After the client receive user's request, it will send the request to the server
 * After server processing the request, the client will receive the response from server
 * and display the result to the console
 *
 * @author Enliang Wu
 * email: enliangw@andrew.cmu.edu
 */


public class Client {
    private static Socket clientSocket;
    private static InputStream in;
    private static PrintStream out;

    public static void main(String args[]) {
        // args give message contents and server hostname
        System.out.println("The client is running.");
        Scanner scanner = new Scanner(System.in);

        try {
            clientSocket = new Socket("localhost", 6789);
            in = clientSocket.getInputStream();
            out = new PrintStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            System.out.println("0. View basic blockchain status.\n"
                    + "1. Add a transaction to the blockchain.\n"
                    + "2. Verify the blockchain.\n"
                    + "3. View the blockchain.\n"
                    + "4. Corrupt the chain.\n"
                    + "5. Hide the corruption by repairing the chain. \n"
                    + "6. Exit.");
            int select = scanner.nextInt();
            scanner.nextLine();
            RequestMessage req = null;
            switch (select) {
                case 0:
                    req = new RequestMessage(0);
                    break;
                case 1:
                    System.out.println("Enter difficulty > 0");
                    int difficulty = Integer.parseInt(scanner.nextLine());
                    System.out.println("Enter transaction");
                    String data = scanner.nextLine();
                    req = new RequestMessage(1, difficulty, data, 0);
                    break;
                case 2:
                    req = new RequestMessage(2);
                    break;
                case 3:
                    req = new RequestMessage(3);
                    break;
                case 4:
                    System.out.println("corrupt the Blockchain");
                    System.out.println("Enter block ID of block to corrupt");
                    int index = Integer.parseInt(scanner.nextLine());
                    System.out.println("Enter new data for block " + index);
                    String newData = scanner.nextLine();
                    req = new RequestMessage(4, 0, newData, index);
                    break;
                case 5:
                    req = new RequestMessage(5);
                    break;
                case 6:
                    req = new RequestMessage(6);
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
            if (req != null) {
                ResponseMessage res = sendAndReceive(req);
                if (select != 6) {
                    System.out.println("Got server response");
                    if (res.isSuccess()) {
                        System.out.println(res.getData());
                    } else {
                        System.out.println(res.getErrMsg());
                    }
                }
            }

            if (select == 6) {
                try {
                    clientSocket.close();
                    break;
                } catch (Exception e) {
                }
            }
        }
    }

    private static ResponseMessage sendAndReceive(RequestMessage requestMessage) {
        if (requestMessage == null) {
            return null;
        }

        try {
            out.write(JSONObject.toJSONBytes(requestMessage));
            out.flush();

            byte[] data = new byte[1024 * 1024 * 64];
            byte[] buffer = new byte[1024];
            int total = 0;
            int len = in.read(buffer);
            if (len > 0) {
                System.arraycopy(buffer, 0, data, total, len);
                total += len;
            }
            while (in.available() > 0) {
                len = in.read(buffer);
                if (len > 0) {
                    System.arraycopy(buffer, 0, data, total, len);
                    total += len;
                }
            }

            byte[] correct = new byte[total];
            System.arraycopy(data, 0, correct, 0, total);

            return JSONObject.parseObject(new String(correct), ResponseMessage.class);
        } catch (SocketException e) {
            if (!e.getMessage().equals("Connection reset")) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}