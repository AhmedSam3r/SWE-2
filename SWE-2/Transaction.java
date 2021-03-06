package mychain;
import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId; // this is also the hash of the transaction.
    public PublicKey sender; // senders address/public key.
    public PublicKey reciepient; // Recipients address/public key.
    public float value;
    public byte[] signature; // this is to prevent anybody else from spending funds in our wallet.

    public ArrayList<TransInput> inputs = new ArrayList<TransInput>();
    public ArrayList<TransOutput> outputs = new ArrayList<TransOutput>();

    private static int sequence = 0; // a rough count of how many transactions have been generated.

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransInput> inputs) {
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // This Calculates the transaction hash (which will be used as its Id)
    private String calulateHash() {
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
        return ApplySha256.applySha256
                (
                        ApplySha256.getStringFromKey(sender) +
                                ApplySha256.getStringFromKey(reciepient) +
                        Float.toString(value) + sequence
                );
    }
    public void generateSignature(PrivateKey privateKey) {
        String data = ApplySha256.getStringFromKey(sender) + mychain.ApplySha256.getStringFromKey(reciepient) + Float.toString(value)	;
        signature = ApplySha256.applyECDSASig(privateKey,data);
    }
    //Verifies the data we signed hasnt been tampered with
    public boolean verifiySignature() {
        String data = mychain.ApplySha256.getStringFromKey(sender) + mychain.ApplySha256.getStringFromKey(reciepient) + Float.toString(value)	;
        return mychain.ApplySha256.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {

        if(verifiySignature() == false) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent):
        for(mychain.TransInput i : inputs) {
            i.UTXO = MyChain.UTXOs.get(i.transactionOutputId);
        }

        //check if transaction is valid:
        if(getInputsValue() < MyChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        //generate transaction outputs:
        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calulateHash();
        outputs.add(new TransOutput( this.reciepient, value,transactionId)); //send value to recipient
        outputs.add(new TransOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

        //add outputs to Unspent list
        for(TransOutput o : outputs) {
            MyChain.UTXOs.put(o.id , o);
        }

        //remove transaction inputs from UTXO lists as spent:
        for(TransInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it 
            MyChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    //returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for(TransInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it 
            total += i.UTXO.value;
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for(mychain.TransOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
}
