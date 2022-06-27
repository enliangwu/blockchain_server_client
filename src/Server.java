import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * This is a TCP server.
 * It receives requests from client side
 * After processing the request, it will send the response back to client
 *
 * @author Enliang Wu
 * email: enliangw@andrew.cmu.edu
 */
public class Server {
    private static BlockChain blockChain;

    public static void main(String args[]) {
        System.out.println("Blockchain server running");

        blockChain = new BlockChain();

        // add a genesis block when initialize
        Block genesisBlock = new Block(0, blockChain.getTime(), "", 2);
        blockChain.addBlock(genesisBlock);

        blockChain.computeHashesPerSecond();


        ServerSocket listenSocket = null;
        // create a byte array and use as a buffer
        byte[] buffer = new byte[1024];
        try {
            listenSocket = new ServerSocket(6789);
            // use a loop to listen for client requests
            while (true) {
                // get data from client
                Socket clientSocket = listenSocket.accept();
                System.out.println("We have a visitor");

                InputStream in = clientSocket.getInputStream();
                PrintStream out = new PrintStream(clientSocket.getOutputStream());

                while (true) {
                    try {
                        byte[] data = new byte[1024];
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
                        String requestString = new String(correct);
                        RequestMessage requestMessage = null;
                        ResponseMessage responseMessage;
                        try {
                            requestMessage = JSONObject.parseObject(requestString, RequestMessage.class);
                            responseMessage = getResponse(requestMessage);
                            if (requestMessage.getType() == 6) {
                                clientSocket.close();
                                break;
                            }
                        } catch (JSONException e) {
                            responseMessage = createFailureResponse("Invalid request");
                        }

                        System.out.println("Receive client request " + requestMessage);
                        System.out.println("Send back response " + responseMessage);
                        out.write(JSONObject.toJSONBytes(responseMessage));
                    } catch (SocketException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            // handle socket exception
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            // handle io exception
            System.out.println("IO: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close socket
            if (listenSocket != null) {
                try {
                    listenSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static ResponseMessage getResponse(RequestMessage message) {
        long start, duration;
        StringBuilder sb;
        switch (message.getType()) {
            case 0:
                System.out.println("View chain status");
                return createSuccessResponse(blockChain.toString(), 0);
            case 1:
                System.out.println("Adding a block");
                if (message.getDifficulty() <= 0 || message.getData() == null) {
                    return createFailureResponse("Invalid params");
                }
                start = System.currentTimeMillis();
                blockChain.addBlock(new Block(blockChain.getChainSize(), blockChain.getTime(), message.getData(), message.getDifficulty()));
                duration = System.currentTimeMillis() - start;
                return createSuccessResponse("Total execution time to add this block was " + duration + " milliseconds", duration);
            case 2:
                System.out.println("Verify entire chain");
                start = System.currentTimeMillis();
                sb = new StringBuilder("Chain verification: " + blockChain.isChainValid());
                System.out.println();
                duration = System.currentTimeMillis() - start;
                sb.append("\n").append("Total execution time to verify the chain was " + duration + " milliseconds");
                return createSuccessResponse(sb.toString(), duration);
            case 3:
                System.out.println("View the Blockchain");
                sb = new StringBuilder("{\"ds_chain\": [");
                for (int i = 0; i < blockChain.getChainSize(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(blockChain.getBlock(i).toString());
                }
                sb.append("], \"chainHash\":\"" + blockChain.getChainHash() + "\"");
                return createSuccessResponse(sb.toString(), 0);
            case 4:
                System.out.println("Corrupt the Blockchain");
                if (message.getIndex() < 0 || message.getIndex() >= blockChain.getChainSize() || message.getData() == null) {
                    return createFailureResponse("Invalid params");
                }

                Block block = blockChain.getBlock(message.getIndex());
                if (message.getData().equals(block.getData())) {
                    return createFailureResponse("New data is equal to old");
                } else {
                    block.setData(message.getData());
                    return createSuccessResponse("Block " + message.getIndex() + " now holds " + message.getData(), 0);
                }
            case 5:
                System.out.println("Repair chain");
                start = System.currentTimeMillis();
                blockChain.repairChain();
                duration = System.currentTimeMillis() - start;
                return createSuccessResponse(
                        "Total execution time required to repair the chain was " + duration + " milliseconds", 0);
            case 6:
                System.out.println("Visitor exit");
                return new ResponseMessage();
            default:
                return createFailureResponse("Invalid Option");
        }
    }

    private static ResponseMessage createSuccessResponse(String data, long duration) {
        return new ResponseMessage(true, data, duration, null);
    }

    private static ResponseMessage createFailureResponse(String errMsg) {
        return new ResponseMessage(false, null, 0, errMsg);
    }

}
