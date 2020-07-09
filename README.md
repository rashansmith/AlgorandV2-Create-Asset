## Algorand - Create an Asset

This repo updates this tutorial: https://developer.algorand.org/tutorials/create-asset-java/ to use the v2 version of the Algorand SDK in Java.


### Current Issue

Error: 
```Exception in thread "main" java.lang.IllegalArgumentException: digest wrong length
        at com.algorand.algosdk.crypto.Digest.<init>(Digest.java:30)
        at com.algorand.algosdk.builder.transaction.TransactionBuilder.genesisHash(TransactionBuilder.java:405)
        at com.example.javaAssetV2.JavaAssetV2Application.main(JavaAssetV2Application.java:175)
```
  
 This line: ```client.TransactionParams().execute().body().genesisId``` currently returns 12 bytes[], but Digest seems to be expecting 32 bytes[]?
 
 
 Link to Algorand Java SDK Digest code: https://github.com/algorand/java-algorand-sdk/blob/develop/src/main/java/com/algorand/algosdk/crypto/Digest.java
 Link to my code implementing Digest: https://github.com/rashansmith/AlgorandV2-Create-Asset/blob/master/src/main/java/com/example/javaAssetV2/JavaAssetV2Application.java
  

