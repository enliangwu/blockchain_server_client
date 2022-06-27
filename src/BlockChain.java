import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * name: Enliang Wu
 * email: enliangw@andrew.cmu.edu
 * main routine
 * some experiments for different difficulty of blocks, detail as follows
 * we add 5 blocks with same data("data") and different difficulty from 1 to 5, and let's see the processing time
 * difficulty 1: Total execution time to add this block was 6 milliseconds
 * difficulty 2: Total execution time to add this block was 4 milliseconds
 * difficulty 3: Total execution time to add this block was 58 milliseconds
 * difficulty 4: Total execution time to add this block was 283 milliseconds
 * difficulty 5: Total execution time to add this block was 1588 milliseconds
 * as we can see, after difficulty is greater than 2, the processing time increases by several times for each additional difficulty of 1
 */
public class BlockChain {
    private ArrayList<Block> blocks;
    private String chainHash;
    private int hashesPerSecond;

    public BlockChain() {
        blocks = new ArrayList<>();
        chainHash = "";
        hashesPerSecond = 0;
    }

    /**
     * A new Block is being added to the BlockChain.
     * This new block's previous hash must hold the hash of the most recently added block.
     * After this call on addBlock, the new block becomes the most recently added block on the BlockChain.
     * The SHA256 hash of every block must exhibit proof of work,
     * i.e., have the requisite number of leftmost 0's defined by its difficulty. Suppose our new block is x.
     * And suppose the old blockchain was a <-- b <-- c <-- d then the chain after addBlock completes is a <-- b <-- c <-- d <-- x.
     * Within the block x, there is a previous hash field. This previous hash field holds the hash of the block d.
     * The block d is called the parent of x. The block x is the child of the block d.
     * It is important to also maintain a hash of the most recently added block in a chain hash.
     * Let's look at our two chains again. a <-- b <-- c <-- d. The chain hash will hold the hash of d.
     * After adding x, we have a <-- b <-- c <-- d <-- x. The chain hash now holds the hash of x.
     * The chain hash is not defined within a block but is defined within the block chain.
     * The arrows are used to describe these hash pointers. If b contains the hash of a then we write a <-- b
     * @param block
     */
    public void addBlock(Block block) {
        if (block == null) {
            return;
        }

        // if there is block in the chain, we need use hash of last block as previousHash
        Block latestBlock = getLatestBlock();
        if (latestBlock != null) {
            block.setPreviousHash(latestBlock.proofOfWork());
        }
        blocks.add(block);

        // the chain's hash will be the hash of new added block
        chainHash = block.proofOfWork();
    }

    /**
     * Compute and return the total difficulty of all blocks on the chain. Each block knows its own difficulty
     * sum difficulty of all blocks
     * @return total
     */
    public int getTotalDifficulty() {
        int total = 0;
        for (Block block: blocks) {
            total += block.getDifficulty();
        }
        return total;
    }

    /**
     * This method computes exactly 2 million hashes and times how long that process takes.
     * So, hashes per second is approximated as (2 million / number of seconds).
     * It is run on start up and sets the instance variable hashesPerSecond.
     * It uses a simple string - "00000000" to hash
     */
    public void computeHashesPerSecond() {
        int cnt = 2000000;
        int i = 0;
        long start = System.currentTimeMillis();
        while (i < cnt) {
            String input = "00000000";
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(input.getBytes("UTF-8"));
                StringBuffer hexString = new StringBuffer();
                for (int j = 0; j < hash.length; j++) {
                    // convert hex num to string
                    String hex = Integer.toHexString(0xff & hash[j]);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            i++;
        }
        long duration = (System.currentTimeMillis() - start) / 1000;
        hashesPerSecond = (int) (cnt / duration);
    }

    /**
     * Get block at position i
     * @param i
     * @return Block
     */
    public Block getBlock(int i) {
        if (i < 0 || i >= blocks.size()) {
            return null;
        }
        return blocks.get(i);
    }

    public String getChainHash() {
        return chainHash;
    }

    public int getChainSize() {
        return blocks.size();
    }

    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    public Block getLatestBlock() {
        return blocks.isEmpty() ? null : blocks.get(blocks.size() - 1);
    }

    public int getHashesPerSecond() {
        return hashesPerSecond;
    }

    /**
     * Compute and return the expected number of hashes required for the entire chain
     * @return total
     */

    public double getTotalExpectedHashes() {
        double total = 0;
        for (Block block: blocks) {
            total += Math.pow(16, block.getDifficulty());
        }
        return total;
    }

    /**
     * If the chain only contains one block, the genesis block at position 0,
     * this routine computes the hash of the block and checks that the hash has
     * the requisite number of leftmost 0's (proof of work) as specified in the difficulty field.
     * It also checks that the chain hash is equal to this computed hash. If either check fails, return an error message.
     * Otherwise, return the string "TRUE". If the chain has more blocks than one, begin checking from block one.
     * Continue checking until you have validated the entire chain.
     * The first check will involve a computation of a hash in Block 0 and a comparison with the hash pointer in Block 1.
     * If they match and if the proof of work is correct, go and visit the next block in the chain.
     * At the end, check that the chain hash is also correct.
     *
     * @return string - TRUE or FALSE with error massage
     */
    public String isChainValid() {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            String blockHash = block.calculateHash();
            String target = new String(new char[block.getDifficulty()]).replace('\0', '0');
            if (!blockHash.substring(0, block.getDifficulty()).equals(target)) {
                return "FALSE\nImproper hash on node " + block.getIndex() + " Does not begin with " + target;
            }

            if (i == blocks.size() - 1 && !blockHash.equals(chainHash)) {
                return "FALSE\nBlock chain's hash not equal to hash of block";
            }

            if (i > 0 && !blocks.get(i - 1).calculateHash().equals(block.getPreviousHash())) {
                return "Block's hash doesn't equal to hash of parent";
            }
        }
        return "TRUE";
    }

    /**
     * repair the chain by re calculate hash of all blocks
     */
    public void repairChain() {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (i > 0) {
                block.setPreviousHash(blocks.get(i - 1).calculateHash());
            }
            String hash = block.proofOfWork();

            if (i == blocks.size() - 1) {
                chainHash = hash;
            }
        }
    }

    /**
     * @return a String representation of the entire chain
     */
    @Override
    public String toString() {
        return "Current size of chain: " + getChainSize() + "\n"
                + "Difficulty of most recent block: " + getLatestBlock().getDifficulty() + "\n"
                + "Total difficulty for all blocks: " + getTotalDifficulty() + "\n"
                + "Approximate hashes per second on this machine: " + hashesPerSecond + "\n"
                + "Expected total hashes required for the whole chain: " + getTotalExpectedHashes() + "\n"
                + "Nonce for most recent block: " + getLatestBlock().getNonce() + "\n"
                + "Chain hash: " + chainHash;
    }

    /**
     * This routine acts as a test driver for Blockchain.
     * It will begin by creating a BlockChain object and then adding the Genesis block to the chain.
     * The Genesis block will be created with an empty string as the previous hash and a difficulty of 2.
     * On start up, this routine will also establish the hashes per second instance member.
     * All blocks added to the Blockchain will have a difficulty passed in to the program by the user at run time.
     * All hashes will have the proper number of zero hex digits representing the most significant nibbles in the hash.
     * A nibble is 4 bits. If the difficulty is specified as three,
     * then all hashes will begin with 3 or more zero hex digits (or 3 nibbles, or 12 zero bits).
     * @param args
     */
    public static void main(String[] args) {
        BlockChain blockChain = new BlockChain();

        // add a genesis block when initialize
        Block genesisBlock = new Block(0, blockChain.getTime(), "", 2);
        blockChain.addBlock(genesisBlock);

        blockChain.computeHashesPerSecond();

        Scanner scanner = new Scanner(System.in);
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
            long start;
            long duration;
            switch (select) {
                case 0:
                    /**
                     * the program will display:
                     * The number of blocks on the chain
                     * Difficulty of most recent block.
                     * The total difficulty for all blocks Approximate hashes per second on this machine.
                     * Expected total hashes required for the whole chain.
                     * The computed nonce for most recent block.
                     * The chain hash (hash of the most recent block).
                     */
                    System.out.println(blockChain);
                    break;
                case 1:
                    /**
                     * option 1, the program will prompt for and then read the difficulty level for this block.
                     * It will then prompt for and then read a line of data from the user (representing a transaction).
                     * The program will then add a block containing that transaction to the block chain.
                     * The program will display the time it took to add this block.
                     */
                    System.out.println("Enter difficulty > 0");
                    int difficulty = Integer.parseInt(scanner.nextLine());
                    System.out.println("Enter transaction");
                    String data = scanner.nextLine();
                    start = System.currentTimeMillis();
                    blockChain.addBlock(new Block(blockChain.getChainSize(), blockChain.getTime(), data, difficulty));
                    duration = System.currentTimeMillis() - start;
                    System.out.println("Total execution time to add this block was " + duration + " milliseconds");
                    break;
                case 2:
                    /**
                     * option 2, then call the isChainValid method and display the results.
                     * It is important to note that this method will execute fast.
                     * Blockchains are easy to validate but time consuming to modify.
                     * program displays the number of milliseconds it took for validate to run.
                     */
                    start = System.currentTimeMillis();
                    System.out.println("Chain verification: " + blockChain.isChainValid());
                    duration = System.currentTimeMillis() - start;
                    System.out.println("Total execution time to verify the chain was " + duration + " milliseconds");
                    break;
                case 3:
                    /**
                     * option 3, display the entire Blockchain contents as a correctly formed JSON document.
                     */
                    System.out.println("View the Blockchain");
                    StringBuilder sb = new StringBuilder("{\"ds_chain\": [");
                    for (int i = 0; i < blockChain.getChainSize(); i++) {
                        if (i > 0) {
                            sb.append(",");
                        }
                        sb.append(blockChain.getBlock(i).toString());
                    }
                    sb.append("], \"chainHash\":\"" + blockChain.getChainHash() + "\"");
                    System.out.println(sb);
                    break;
                case 4:
                    /**
                     * option 4, she wants to corrupt the chain.
                     * Ask her for the block index (0..size-1) and ask her for the new data that will be placed in the block.
                     * Her new data will be placed in the block. At this point, option 2 (verify chain) should show false.
                     * In other words, she will be making a data change to a particular block and the chain itself will become invalid.
                     */
                    System.out.println("corrupt the Blockchain");
                    System.out.println("Enter block ID of block to corrupt");
                    int index = Integer.parseInt(scanner.nextLine());
                    if (index >= blockChain.getChainSize() || index < 0) {
                        System.out.println("Invalid block id, max index is " + (blockChain.getChainSize() - 1));
                    }
                    Block block = blockChain.getBlock(index);
                    System.out.println("Enter new data for block " + index);
                    String newData = scanner.nextLine();
                    if (newData.equals(block.getData())) {
                        System.out.println("New data is equal to old");
                    } else {
                        block.setData(newData);
                        System.out.println("Block " + index + " now holds " + newData);
                    }
                    break;
                case 5:
                    /**
                     * she wants to repair the chain.
                     * That is, she wants to recompute the proof of work for each node that has become invalid -
                     * due perhaps, to an earlier selection of option 4.
                     * The program begins at the Genesis block and checks each block in turn.
                     * If any block is found to be invalid, it executes repair logic.
                     */
                    start = System.currentTimeMillis();
                    blockChain.repairChain();
                    duration = System.currentTimeMillis() - start;
                    System.out.println("Total execution time required to repair the chain was " + duration + " milliseconds");
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        }
    }


}
