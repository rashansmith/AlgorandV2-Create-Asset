package com.example.javaAssetV2;

import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.account.Account;
import java.math.BigInteger;

import com.algorand.algosdk.algod.client.ApiException;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.crypto.Digest;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;

import com.algorand.algosdk.v2.client.algod.*;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.PendingTransactionResponse;

public class CreateAssetTutorial {

	// public AlgodApi algodApiInstance = null;
	public static AlgodClient client;

	// utility function to connect to a node
	AlgodClient connectToNetwork() {

		// sandbox
		final String ALGOD_API_ADDR = "http://localhost";
		final int ALGOD_PORT = 4001;
		final String ALGOD_API_TOKEN = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

		client = (AlgodClient) new AlgodClient(ALGOD_API_ADDR, ALGOD_PORT, ALGOD_API_TOKEN);

		return client;
	}

	// Inline class to handle changing block parameters
	// Throughout the example
	public class ChangingBlockParms {
		public Long fee;
		public Long firstRound;
		public Long lastRound;
		public String genID;
		public Digest genHash;

		public ChangingBlockParms() {
			this.fee = Long.valueOf(0);
			this.firstRound = Long.valueOf(0);
			this.lastRound = Long.valueOf(0);
			this.genID = "";
			this.genHash = null;
		}
	}

	// Utility function to wait on a transaction to be confirmed
	public void waitForConfirmation(String txID) throws Exception {
		if (client == null)
			CreateAssetTutorial.client = connectToNetwork();
		Long lastRound = client.GetStatus().execute().body().lastRound;
		while (true) {
			try {
				// Check the pending transactions
				Response<PendingTransactionResponse> pendingInfo = client.PendingTransactionInformation(txID).execute();
				if (pendingInfo.body().confirmedRound != null && pendingInfo.body().confirmedRound > 0) {
					// Got the completed Transaction
					System.out.println(
							"Transaction " + txID + " confirmed in round " + pendingInfo.body().confirmedRound);
					break;
				}
				lastRound++;
				client.WaitForBlock(lastRound).execute();
			} catch (Exception e) {
				throw (e);
			}
		}
	}

	// Utility function to update changing block parameters
	public ChangingBlockParms getChangingParms(AlgodClient client) throws Exception {
		ChangingBlockParms cp = new CreateAssetTutorial.ChangingBlockParms();
		try {
			com.algorand.algosdk.v2.client.algod.TransactionParams params = client.TransactionParams();
			cp.fee = params.execute().body().fee;
			cp.firstRound = params.execute().body().lastRound;
			cp.lastRound = Long.sum(cp.firstRound.longValue(), Long.valueOf(1000));
			cp.genID = params.execute().body().genesisId;
			cp.genHash = new Digest(params.execute().body().genesisId.getBytes());

		} catch (ApiException e) {
			throw (e);
		}
		return (cp);
	}

	// Utility function for sending a raw signed transaction to the network
	public RawTransaction submitTransaction(SignedTransaction signedTx) throws Exception {
		// Msgpack encode the signed transaction
		byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
		RawTransaction id = client.RawTransaction().rawtxn(encodedTxBytes);
		return (id);
	}

	public static void main(String args[]) throws Exception {
		CreateAssetTutorial ex = new CreateAssetTutorial();
		AlgodClient algodClientInstance = ex.connectToNetwork();

		// recover example accounts

		// final String account1_mnemonic = "<your-25-word-mnemonic>";
		// final String account2_mnemonic = "<your-25-word-mnemonic>";
		// final String account3_mnemonic = "<your-25-word-mnemonic>";

		// CHANGE THESE VALUES WITH YOUR MNEMONICS FROM STEP 1

		final String account1_mnemonic = "neutral blade diesel guard punch glide pepper cancel wise soul legend second capital load hover extra witness forward enlist flee pitch taxi impulse absent common";
		final String account2_mnemonic = "cute ask spread arena glide way feed else this case parade fly diamond cargo satoshi clever pear apple dream champion effort near flee absent gate";
		final String account3_mnemonic = "number define pet usual brave day traffic peasant style goddess wisdom cart mouse fork ecology jungle impose border dad please worth surprise sort abstract mechanic";

		Account acct1 = new Account(account1_mnemonic);
		Account acct2 = new Account(account2_mnemonic);
		Account acct3 = new Account(account3_mnemonic);
		System.out.println("Account1: " + acct1.getAddress());
		System.out.println("Account2: " + acct2.getAddress());
		System.out.println("Account3: " + acct3.getAddress());
		// Create a new asset
		// get changing network parameters
		ChangingBlockParms cp = null;
		try {
			cp = ex.getChangingParms(algodClientInstance);
		} catch (ApiException e) {
			e.printStackTrace();
			return;
		}

		// The following parameters are asset specific
		// and will be re-used throughout the example.

		// Total number of this asset available for circulation

		BigInteger assetTotal = BigInteger.valueOf(10000);

		// Whether user accounts will need to be unfrozen before transacting

		boolean defaultFrozen = false;
		// Used to display asset units to user
		String unitName = "myunit";
		// Friendly name of the asset
		String assetName = "my longer asset name";
		String url = "http://this.test.com";
		String assetMetadataHash = "16efaa3924a6fd9d3a4824799a4ac65d";

		// The following parameters are the only ones
		// that can be changed, and they have to be changed
		// by the current manager
		// Specified address can change reserve, freeze, clawback, and manager

		Address manager = acct2.getAddress();
		Address reserve = acct2.getAddress();
		Address freeze = acct2.getAddress();
		Address clawback = acct2.getAddress();

		// Decimals specifies the number of digits to display after the decimal
		// place when displaying this asset. A value of 0 represents an asset
		// that is not divisible, a value of 1 represents an asset divisible
		// into tenths, and so on. This value must be between 0 and 19

		Integer decimals = 0;
		Transaction tx = Transaction.AssetCreateTransactionBuilder().sender(acct1.getAddress()).fee(0)
				.firstValid(cp.firstRound).lastValid(cp.lastRound).genesisHash(cp.genHash).assetTotal(assetTotal)
				.assetDecimals(decimals).assetUnitName(unitName).assetName(assetName).url(url)
				.metadataHashUTF8(assetMetadataHash).manager(manager).reserve(reserve).freeze(freeze)
				.defaultFrozen(defaultFrozen).clawback(clawback).build();
		// Update the fee as per what the BlockChain is suggesting
		Account.setFeeByFeePerByte(tx, cp.fee.intValue());

		// Sign the Transaction with creator account
		SignedTransaction signedTx = acct1.signTransaction(tx);
		BigInteger assetID = null;
		try {
			RawTransaction id = ex.submitTransaction(signedTx);
			System.out.println("Transaction ID: " + id);
			ex.waitForConfirmation(signedTx.transactionID);
			// Now that the transaction is confirmed we can get the assetID
			PendingTransactionInformation ptx = algodClientInstance
					.PendingTransactionInformation(id.execute().body().txId);
			assetID = ptx.execute().body().txn.tx.assetIndex;

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.out.println("AssetID = " + assetID);
	}
}
