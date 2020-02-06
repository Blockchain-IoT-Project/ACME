import static java.nio.charset.StandardCharsets.UTF_8;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.sql.Timestamp;

import Config.Config;
import User.UserContext;
import Util.Util;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse.Status;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.BlockEvent;

public class InvokeChaincode {

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public static void main(String[] args) {

		if (args.length == 2){

			Channel1(args);
			Channel2(args);

		} else if (args.length == 3 ) {
            if (args[2].equals("c1")) {

                Channel1(args);

            } else if (args[2].equals("c2")) {

                Channel2(args);

            }

        }
	}

	private static void Channel1(String[] args){
		try {

			Util.cleanUp();
			String caUrl = Config.CA_ORG1_URL;
			CAClient caClient = new CAClient(caUrl, new Properties());


			// Enroll Admin to Org1MSP
			UserContext adminUserContext = new UserContext();
			adminUserContext.setName(Config.ADMIN);
			adminUserContext.setAffiliation(Config.ORG1);
			adminUserContext.setMspId(Config.ORG1_MSP);
			caClient.setAdminUserContext(adminUserContext);
			adminUserContext = caClient.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

			//Setup Fabric Client
			FabricClient fabClient = new FabricClient(adminUserContext);


			//Initialize Channel with Peer, EventHub and Orderer
			ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_1_NAME);
			Channel channel = channelClient.getChannel();
			Peer peer0org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            Peer peer0org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
            Peer peer1org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
            Peer peer1org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			channel.addPeer(peer0org1);
            channel.addPeer(peer0org2);
            channel.addPeer(peer1org1);
            channel.addPeer(peer1org2);
			channel.addOrderer(orderer);
			channel.initialize();

			//get Timestamp
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Timestamp newTimestamp = new Timestamp(System.currentTimeMillis());

			//Setup request
			TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request.setChaincodeID(ccid);
			request.setFcn("write");

			String[] arguments = { args[0], args[1], sdf.format(newTimestamp) };
			request.setArgs(arguments);
			request.setProposalWaitTime(10000);

			//Once with arguments and once as bytes

			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
			tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
			tm2.put("result", ":)".getBytes(UTF_8));
			tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
			request.setTransientMap(tm2);

			//Send request
			Collection<ProposalResponse> responses = channel.sendTransactionProposal(request, channel.getPeers());
			System.out.println("------------------------------------------------------------");
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				String response = res.getProposalResponse().getResponse().toString();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked write on " + Config.CHAINCODE_1_NAME + ". Status - " + status + "\n"+ response);
			}

			System.out.println("------------------------------------------------------------");

			// split all the responses up into failed and successful ones
			Collection<ProposalResponse> successful = new ArrayList<>();
			Collection<ProposalResponse> failed = new ArrayList<>();

			for (ProposalResponse response : responses) {
				if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
					successful.add(response);
				} else {
					failed.add(response);
				}
			}

			/*
			if (failed.size() > 0) {
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.SEVERE, "Received failed response(s)");
			} */

			//submit it
			CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompleteableFuture = channel.sendTransaction(successful);



		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void Channel2(String[] args){
		try {
			Util.cleanUp();
			String caUrl_2 = Config.CA_ORG1_URL;
			CAClient caClient_c2 = new CAClient(caUrl_2, new Properties());


			// Enroll Admin to Org1MSP
			UserContext adminUserContext_c2 = new UserContext();
			adminUserContext_c2.setName(Config.ADMIN);
			adminUserContext_c2.setAffiliation(Config.ORG1);
			adminUserContext_c2.setMspId(Config.ORG1_MSP);
			caClient_c2.setAdminUserContext(adminUserContext_c2);
			adminUserContext_c2 = caClient_c2.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

			//Setup Fabric Client
			FabricClient fabClient_c2 = new FabricClient(adminUserContext_c2);


			//Initialize Channel with Peer, EventHub and Orderer
			ChannelClient channelClient = fabClient_c2.createChannelClient(Config.CHANNEL_2_NAME);
			Channel channel = channelClient.getChannel();
			Peer peer0org1_c2 = fabClient_c2.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            Peer peer0org2_c2 = fabClient_c2.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
            Peer peer1org1_c2 = fabClient_c2.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
            Peer peer1org2_c2 = fabClient_c2.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);
            Peer peer0org3_c2 = fabClient_c2.getInstance().newPeer(Config.ORG3_PEER_0, Config.ORG3_PEER_0_URL);
            Peer peer1org3_c2 = fabClient_c2.getInstance().newPeer(Config.ORG3_PEER_1, Config.ORG3_PEER_1_URL);
			Orderer orderer = fabClient_c2.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			channel.addPeer(peer0org1_c2);
            channel.addPeer(peer0org2_c2);
            channel.addPeer(peer1org1_c2);
            channel.addPeer(peer1org2_c2);
            channel.addPeer(peer0org3_c2);
            channel.addPeer(peer1org3_c2);
			channel.addOrderer(orderer);
			channel.initialize();

			//get Timestamp
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Timestamp newTimestamp = new Timestamp(System.currentTimeMillis());

			//Setup request
			TransactionProposalRequest request = fabClient_c2.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName("acme_cc_s2").build();
			request.setChaincodeID(ccid);
			request.setFcn("write");

			String[] arguments = { args[0], args[1], sdf.format(newTimestamp) };
			request.setArgs(arguments);
			request.setProposalWaitTime(10000);

			//Once with arguments and once as bytes

			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
			tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
			tm2.put("result", ":)".getBytes(UTF_8));
			tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);
			request.setTransientMap(tm2);

			//Send request
			Collection<ProposalResponse> responses = channel.sendTransactionProposal(request, channel.getPeers());
			System.out.println("------------------------------------------------------------");
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				String response = res.getProposalResponse().getResponse().toString();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked write on " + Config.CHAINCODE_1_NAME + ". Status - " + status + "\n"+ response);
			}

			System.out.println("------------------------------------------------------------");

			// split all the responses up into failed and successful ones
			Collection<ProposalResponse> successful = new ArrayList<>();
			Collection<ProposalResponse> failed = new ArrayList<>();

			for (ProposalResponse response : responses) {
				if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
					successful.add(response);
				} else {
					failed.add(response);
				}
			}

			//submit it
			CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompleteableFuture = channel.sendTransaction(successful);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
