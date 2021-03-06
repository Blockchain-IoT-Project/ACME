package Config;

import java.io.File;

public class Config {
	
	public static final String ORG1_MSP = "Org1MSP";

	public static final String ORG1 = "org1";

	public static final String ORG2_MSP = "Org2MSP";

	public static final String ORG2 = "org2";

	public static final String ADMIN = "admin";

	public static final String ADMIN_PASSWORD = "adminpw";
	
	public static final String CHANNEL_CONFIG_PATH = "config/channel_s1.tx";
	
	public static final String ORG1_USR_BASE_PATH = ".." + File.separator + "network" + File.separator + "crypto-config" +  File.separator
			+ "org1.acme.org" + File.separator + "users" + File.separator + "Admin@org1.acme.org"
			+ File.separator + "msp";
	
	public static final String ORG2_USR_BASE_PATH = ".." + File.separator + "network" + File.separator + "crypto-config" +  File.separator
			+ "org2.acme.org" + File.separator + "users" + File.separator + "Admin@org2.example.org"
			+ File.separator + "msp";
	
	public static final String ORG1_USR_ADMIN_PK = ORG1_USR_BASE_PATH + File.separator + "keystore";
	public static final String ORG1_USR_ADMIN_CERT = ORG1_USR_BASE_PATH + File.separator + "signcerts";

	public static final String ORG2_USR_ADMIN_PK = ORG2_USR_BASE_PATH + File.separator + "keystore";
	public static final String ORG2_USR_ADMIN_CERT = ORG2_USR_BASE_PATH + File.separator + "admincerts";
	
	public static final String CA_ORG1_URL = "http://localhost:7054";
	
	public static final String CA_ORG2_URL = "http://localhost:8054";
	
	public static final String ORDERER_URL = "grpc://localhost:7050";
	
	public static final String ORDERER_NAME = "orderer.acme.org";
	
	public static final String CHANNEL_1_NAME = "s1";

    public static final String CHANNEL_2_NAME = "s2";
	
	public static final String ORG1_PEER_0 = "peer0.org1.acme.org";
	
	public static final String ORG1_PEER_0_URL = "grpc://localhost:7051";
	
	public static final String ORG1_PEER_1 = "peer1.org1.acme.org";
	
	public static final String ORG1_PEER_1_URL = "grpc://localhost:8051";
	
    public static final String ORG2_PEER_0 = "peer0.org2.acme.org";
	
	public static final String ORG2_PEER_0_URL = "grpc://localhost:9051";
	
	public static final String ORG2_PEER_1 = "peer1.org2.acme.org";
	
	public static final String ORG2_PEER_1_URL = "grpc://localhost:10051";

	public static final String ORG3_PEER_0 = "peer0.org3.acme.org";

	public static final String ORG3_PEER_0_URL = "grpc://localhost:11051";

	public static final String ORG3_PEER_1 = "peer1.org3.acme.org";

	public static final String ORG3_PEER_1_URL = "grpc://localhost:12051";
	
	public static final String CHAINCODE_ROOT_DIR = "chaincode";
	
	public static final String CHAINCODE_1_NAME = "acme_cc_s1";
	
	public static final String CHAINCODE_1_PATH = "github.com/chaincode";
	
	public static final String CHAINCODE_1_VERSION = "1";


}
