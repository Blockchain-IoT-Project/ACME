import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import User.UserContext;
import org.hyperledger.fabric.sdk.*;

import Util.Util;
import Config.Config;

public class QueryChaincode {

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public static void main(String[] args) {
		try {
            Util.cleanUp();

            //clear Console
			//System.out.print("\033[H\033[2J");
			//System.out.flush();

			if(args.length == 1){
				Channel_1(args);
				Channel_2(args);

				System.out.println("-----------------------------------------------------------------------");
			} else if (args[1].equals("s1")) {

				Channel_1(args);
				System.out.println("-----------------------------------------------------------------------");
			} else if (args[1].equals("s2")) {

				Channel_2(args);
				System.out.println("-----------------------------------------------------------------------");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void Channel_1(String[] args){
		try{
			//Setup CAClient
			String caUrl_org1 = Config.CA_ORG1_URL;

			CAClient caClient = new CAClient(caUrl_org1, new Properties());

			// Enroll Admin to Org1MSP for Channel_1
			UserContext adminUserContext = new UserContext();
			adminUserContext.setName(Config.ADMIN);
			adminUserContext.setAffiliation(Config.ORG1);
			adminUserContext.setMspId(Config.ORG1_MSP);

			//Setup CAClient mit Admin Context + enroll admin for Channel_1
			caClient.setAdminUserContext(adminUserContext);
			adminUserContext = caClient.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

			//Setup Client
			//FabricClient == HFClient where adminUserContext als setUserContext vervendet wird
			FabricClient fabClient = new FabricClient(adminUserContext);

			//Setup ChannelClient for Channel with name
			ChannelClient channel_1_Client = fabClient.createChannelClient(Config.CHANNEL_1_NAME);
			Channel channel_1 = channel_1_Client.getChannel();
			Peer c1_peerorg1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			Peer c1_peerorg2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);

			Peer c1_peer1org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
			Peer c1_peer1org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);

			Orderer c1_orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
			channel_1.addPeer(c1_peerorg1);
			channel_1.addPeer(c1_peerorg2);
			channel_1.addPeer(c1_peer1org1);
			channel_1.addPeer(c1_peer1org2);

			channel_1.addOrderer(c1_orderer);
			channel_1.initialize();

			System.out.println("-----------------------------------------------------------------------");
			System.out.println("Ausgabe für Channel 1");

			QueryByChaincodeRequest request = fabClient.getInstance().newQueryProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName("acme_cc_s1").build();
			request.setChaincodeID(ccid);
			request.setFcn("query");
			if (args.length != 0)
				request.setArgs(args[0]);

			Collection<ProposalResponse> response = channel_1.queryByChaincode(request);
			//Collection<ProposalResponse>  responsesQuery = channel_1_Client.queryByChainCode("fabcar", "queryAllCars", null);
			for (ProposalResponse pres : response) {
				String stringResponse = new String(pres.getMessage() + " " + pres.getPeer().getName());
				Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, stringResponse);

				//Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO,pres.getProposalResponse().getPayload().toStringUtf8());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void Channel_2(String[] args){
		try {
			//Setup CAClient
			String caUrl_org2 = Config.CA_ORG2_URL;

			CAClient caClient_2 = new CAClient(caUrl_org2, new Properties());

			// Enroll Admin to Org1MSP for Channel_1
			UserContext adminUserContext_c2 = new UserContext();
			adminUserContext_c2.setName(Config.ADMIN);
			adminUserContext_c2.setAffiliation(Config.ORG2);
			adminUserContext_c2.setMspId(Config.ORG2_MSP);

			//Setup CAClient mit Admin Context + enroll admin for Channel_1
			caClient_2.setAdminUserContext(adminUserContext_c2);
			adminUserContext_c2 = caClient_2.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

			//Setup Client
			//FabricClient == HFClient where adminUserContext als setUserContext vervendet wird
			FabricClient fabClient_c2 = new FabricClient(adminUserContext_c2);


			ChannelClient channel_2_Client = fabClient_c2.createChannelClient(Config.CHANNEL_2_NAME);
			Channel channel_2 = channel_2_Client.getChannel();

			Peer c2_peerorg1 = fabClient_c2.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			Peer c2_peer1org2 = fabClient_c2.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);
			Peer c2_peerorg2 = fabClient_c2.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
			Peer c2_peer1org1 = fabClient_c2.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
			Peer c2_peer0org3 = fabClient_c2.getInstance().newPeer(Config.ORG3_PEER_0, Config.ORG3_PEER_0_URL);
			Peer c2_peer1org3 = fabClient_c2.getInstance().newPeer(Config.ORG3_PEER_1, Config.ORG3_PEER_1_URL);

			channel_2.addPeer(c2_peerorg1);
			channel_2.addPeer(c2_peer1org2);
			channel_2.addPeer(c2_peerorg2);
			channel_2.addPeer(c2_peer1org1);
			channel_2.addPeer(c2_peer0org3);
			channel_2.addPeer(c2_peer1org3);

			Orderer c2_orderer = fabClient_c2.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);

			channel_2.addOrderer(c2_orderer);
			channel_2.initialize();

			System.out.println("-----------------------------------------------------------------------");
			System.out.println("Ausgabe für Channel 2");

			QueryByChaincodeRequest request_2 = fabClient_c2.getInstance().newQueryProposalRequest();
			ChaincodeID ccid_2 = ChaincodeID.newBuilder().setName("acme_cc_s2").build();
			request_2.setChaincodeID(ccid_2);
			request_2.setFcn("query");
			if (args.length != 0)
				request_2.setArgs(args[0]);

			Collection<ProposalResponse> response_2 = channel_2.queryByChaincode(request_2);

			for (ProposalResponse pres_2 : response_2) {
				String stringResponse = new String(pres_2.getMessage() + " " + pres_2.getPeer().getName());
				Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, stringResponse);

				//Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO,pres.getProposalResponse().getPayload().toStringUtf8());
			}
		} catch( Exception e){
			e.printStackTrace();
		}
	}

	private static void Org3Channel_2(String[] args){
		try {
			//Setup CAClient
			String caUrl_org2 = Config.CA_ORG2_URL;

			CAClient caClient_2 = new CAClient(caUrl_org2, new Properties());

			// Enroll Admin to Org1MSP for Channel_1
			UserContext adminUserContext_c2 = new UserContext();
			adminUserContext_c2.setName(Config.ADMIN);
			adminUserContext_c2.setAffiliation(Config.ORG2);
			adminUserContext_c2.setMspId(Config.ORG2_MSP);

			//Setup CAClient mit Admin Context + enroll admin for Channel_1
			caClient_2.setAdminUserContext(adminUserContext_c2);
			adminUserContext_c2 = caClient_2.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

			//Setup Client
			//FabricClient == HFClient where adminUserContext als setUserContext vervendet wird
			FabricClient fabClient_c2 = new FabricClient(adminUserContext_c2);


			ChannelClient channel_2_Client = fabClient_c2.createChannelClient(Config.CHANNEL_2_NAME);
			Channel channel_2 = channel_2_Client.getChannel();

			Peer c2_peerorg1 = fabClient_c2.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			Peer c2_peer1org2 = fabClient_c2.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);
			Peer c2_peerorg2 = fabClient_c2.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
			Peer c2_peer1org1 = fabClient_c2.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
			Peer c2_peer0org3 = fabClient_c2.getInstance().newPeer(Config.ORG3_PEER_0, Config.ORG3_PEER_0_URL);
			Peer c2_peer1org3 = fabClient_c2.getInstance().newPeer(Config.ORG3_PEER_1, Config.ORG3_PEER_1_URL);

			channel_2.addPeer(c2_peerorg1);
			channel_2.addPeer(c2_peer1org2);
			channel_2.addPeer(c2_peerorg2);
			channel_2.addPeer(c2_peer1org1);
			channel_2.addPeer(c2_peer0org3);
			channel_2.addPeer(c2_peer1org3);

			Orderer c2_orderer = fabClient_c2.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);

			channel_2.addOrderer(c2_orderer);
			channel_2.initialize();

			System.out.println("-----------------------------------------------------------------------");
			System.out.println("Ausgabe für Channel 2");

			QueryByChaincodeRequest request_2 = fabClient_c2.getInstance().newQueryProposalRequest();
			ChaincodeID ccid_2 = ChaincodeID.newBuilder().setName("acme_cc_s2").build();
			request_2.setChaincodeID(ccid_2);
			request_2.setFcn("query");
			if (args.length != 0)
				request_2.setArgs(args[0]);

			Collection<ProposalResponse> response_2 = channel_2.queryByChaincode(request_2);

			for (ProposalResponse pres_2 : response_2) {
				String stringResponse = new String(pres_2.getMessage() + " " + pres_2.getPeer().getName());
				Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, stringResponse);

				//Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO,pres.getProposalResponse().getPayload().toStringUtf8());
			}
		} catch( Exception e){
			e.printStackTrace();
		}
	}


}
