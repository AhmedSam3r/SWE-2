package mychain;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

public class Wallet {
    public PublicKey publicKey;
    public PrivateKey privateKey;

     public HashMap<String, TransOutput> UTXOs = new HashMap<String,TransOutput>();

    public Wallet()
    {
        generateKeyPair();
    }

    public void generateKeyPair()
    {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();

             privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransOutput> item: MyChain.UTXOs.entrySet()){
            TransOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
                total += UTXO.value ;
            }
        }
        return total;
    }
    //Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey _recipient,float value ) {
        if(getBalance() < value) { //gather balance and check funds.
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        ArrayList<TransInput> inputs = new ArrayList<TransInput>();

        float total = 0;
        for (Map.Entry<String, TransOutput> item: UTXOs.entrySet()){
            TransOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransInput(UTXO.id));
            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransInput input: inputs){
            UTXOs.remove(input.transactionOutputId);
        }
        return newTransaction;
    }
}
