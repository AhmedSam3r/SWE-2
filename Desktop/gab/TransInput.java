package mychain;
public class TransInput {
    public String transactionOutputId; //Reference to TransactionOutputs -> transactionId
    public TransOutput UTXO; //Contains the Unspent transaction output

    public TransInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
