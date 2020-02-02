import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

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

	public static void main(String args[]) {
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
			ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
			Channel channel = channelClient.getChannel();
			Peer peer0org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			Peer peer0org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
			//EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			channel.addPeer(peer0org1);
			channel.addPeer(peer0org2);
			//channel.addEventHub(eventHub);
			channel.addOrderer(orderer);
			channel.initialize();
			
			//ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);

			
			//Setup request
			TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request.setChaincodeID(ccid);
			request.setFcn("write");
			String[] arguments = { "tag2", "4", "2019-01-01 14:22:11" };
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
            //Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				String response = res.getProposalResponse().getResponse().toString();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked write on " + Config.CHAINCODE_1_NAME + ". Status - " + status + "\n"+ response);
			}
			//2736


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
			
			/*
			//Enroll User to Org1MSP
			UserContext newUserContext = new UserContext();
			newUserContext.setName("user");
			newUserContext.setAffiliation("org1");
			newUserContext.setMspId("Org1MSP");
			caClient.enrollUser(newUserContext, "secret");

			//Setup FabricClient

			FabricClient newFabricClient = new FabricClient(newUserContext);

			//Initialize Channel with Peer, EventHub and Orderer
			//Question is if necessary
			ChannelClient newChannelClient = newFabricClient.createChannelClient(Config.CHANNEL_NAME);
			Channel newChannel = newChannelClient.getChannel();
			Peer peer = newFabricClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			EventHub eventHub = newFabricClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
			Orderer orderer = newFabricClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			newChannel.addPeer(peer);
			newChannel.addEventHub(eventHub);
			newChannel.addOrderer(orderer);
			newChannel.initialize();

			//Setup request
			//With normal arguments
			TransactionProposalRequest request = newFabricClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName("ChainCode_Name").build();
			request.setChaincodeID(ccid);
			request.setFcn("FunctionName");
			String[] arguments = {"Wert1", "Wert2", "Wert3", "Wert4", "Wert5"};
			request.setArgs(arguments);
			request.setProposalWaitTime(1000);

			//As byte stream
			Map<String, byte[]> byteStream = new HashMap<>();
			byteStream.put("HyperledgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
			byteStream.put("method", "TransactionProposalRequest".getBytes(UTF_8));
			byteStream.put("result", ":)".getBytes(UTF_8));
			byteStream.put("event", "!".getBytes(UTF_8));

			request.setTransientMap(byteStream);

			//Send request
			Collection<ProposalResponse> responses = newChannelClient.sendTransactionProposal(request);
			for (ProposalResponse res: responses) {
				Status status = res.getStatus();
				Logger.getLogger(InvokeChaincode.class.getName()).log(Level.INFO,"Invoked createCar on "+Config.CHAINCODE_1_NAME + ". Status - " + status);
			} */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
