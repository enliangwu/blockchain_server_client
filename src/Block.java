/**
 * name: Enliang Wu
 * email: enliangw@andrew.cmu.edu
 */

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;


/**
 * This class represents a simple Block
 */
public class Block {

    /** index - the position of the block on the chain. The first block (the so called Genesis block) has an index of 0 */
    private int index;

    /** timestamp - a Java Timestamp object, it holds the time of the block's creation */
    private Timestamp created;

    /** data - a String holding the block's single transaction details */
    private String data;

    /** previousHash - the SHA256 hash of a block's parent. This is also called a hash pointer */
    private String previousHash = "";

    /** nonce - a BigInteger value determined by a proof of work routine.
     * This has to be found by the proof of work logic.
     * It has to be found so that this block has a hash of the proper difficulty.
     * The difficulty is specified by a small integer representing
     * the minimum number of leading hex zeroes the hash must have
     * */
    private BigInteger nonce;

    /** difficulty - it is an int that specifies the minimum number of left most hex digits needed by a proper hash.
     * The hash is represented in hexadecimal.
     * If, for example, the difficulty is 3, the hash must have at least three leading hex 0's (or,1 and 1/2 bytes).
     * Each hex digit represents 4 bits
     * */
    private int difficulty;

    /** This the Block constructor
     * @param index - This is the position within the chain. Genesis is at 0.
     * @param created - This is the time this block was added.
     * @param data - This is the transaction to be included on the blockchain.
     * @param difficulty - This is the number of leftmost nibbles that need to be 0.
     * */
    public Block(int index, Timestamp created, String data, int difficulty) {
        this.index = index;
        this.created = created;
        this.data = data;
        this.difficulty = difficulty;
        nonce = new BigInteger("0");
    }

    /**
     * Simple getter method
     * @return index of block
     * */
    public int getIndex() {
        return index;
    }

    /**
     * Simple getter method
     * @return created - timestamp of this block
     * */
    public Timestamp getCreated() {
        return created;
    }

    /**
     * Simple getter method
     * @return this block's transaction
     */
    public String getData() {
        return data;
    }

    /**
     * Simple getter method
     * @return previous hash
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * This method returns the nonce for this block.
     * The nonce is a number that has been found to cause the hash of this block
     * to have the correct number of leading hexadecimal zeroes
     * @return a BigInteger representing the nonce for this block
     * */
    public BigInteger getNonce() {
        return nonce;
    }

    /**
     * Simple getter method
     * @return difficulty
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Simple getter method
     * @param index
     * return index of block
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Simple setter method
     * @param created
     */
    public void setCreated(Timestamp created) {
        this.created = created;
    }

    /**
     * Simple setter method
     * @param data - represents the transaction held by this block
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Simple setter method
     * previousHash - a hashpointer to this block's parent
     * @param previousHash
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    /**
     * Simple setter method
     * @param difficulty
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * This method computes a SHA-256 hash of
     * the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty
     * @return a String holding Hexadecimal characters
     * https://www.baeldung.com/sha-256-hashing-java
     */
    public String calculateHash() {
        String input = index + created.getTime() + data + previousHash + nonce + difficulty;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                // convert hex num to string
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method calls calculateHash() to compute a hash of
     * the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty.
     * If the hash has the appropriate number of leading hex zeroes, it is done and returns that proper hash.
     * If the hash does not have the appropriate number of leading hex zeroes,
     * it increments the nonce by 1 and tries again.
     * It continues this process, burning electricity and CPU cycles, until it gets lucky and finds a good hash
     * @return a String with a hash that has the appropriate number of leading hex zeroes.
     * The difficulty value is already in the block. This is the minimum number of hex 0's a proper hash must have.
     * */
    public String proofOfWork() {
        // difficulty means the required number of leading zeros in hash
        String target = new String(new char[difficulty]).replace('\0', '0');

        while (true) {
            String hexHash = calculateHash();
            // if hexHash doesn't have enough leading zeros, we need increase nonce by one and calculate hash again
            if (hexHash.substring(0, difficulty).equals(target)) {
                return hexHash.toUpperCase();
            }
            nonce = nonce.add(new BigInteger("1"));
        }
    }

    /**
     * Override Java's toString method
     * @return A JSON representation of all of this block's data is returned.
     * */
    @Override
    public String toString() {
        return "{\"index\" : " + index +
                ",\"time stamp \" : \"" + created + "\", \"Tx \": \"" + data + "\",\"PrevHash\" : \"" + previousHash
                + "\",\"nonce\" : " + nonce + ",\"difficulty\": " + difficulty + "}";
    }
}
