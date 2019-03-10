package mychain;
import java.util.*;
public class Block
{
    public String hash;
    public String prevHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;
    private int tries;



    Block(String PrevHash)
    {
        this.prevHash=PrevHash;
        timeStamp=new Date().getTime();
        this.hash=calcHash();
    }
    public String calcHash()
    {
        String calculatedhash = ApplySha256.applySha256(
                prevHash + Long.toString(timeStamp) + Integer.toString(tries) + merkleRoot);
        return calculatedhash;
    }

    public void miningBlock(int difficulty)
    {

        merkleRoot = ApplySha256.getMerkleRoot(transactions);
        String target = ApplySha256.getDificultyString(difficulty); //Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            tries ++;
            hash = calcHash();
        }
        System.out.println();
        System.out.println("Block Mined!!! : " + hash);
    }

    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((prevHash != "0")) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}