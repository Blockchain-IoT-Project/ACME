/*
Project ACME chaincode
*/
package org.ACME.chaincode;

import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.contract.ClientIdentity;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ACMEChaincode extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(ACMEChaincode.class);

    @Override
    public Response init(ChaincodeStub stub) {
        try {
            _logger.info("Init java sensor chaincode");
            String func = stub.getFunction();
            if (!func.equals("init")) {
                return newErrorResponse("function other than init is not supported");
            }
            List<String> args = stub.getParameters();
            if (args.size() != 1) {
                newErrorResponse("Incorrect number of arguments. Expecting 1");
            }

            //Define the unit of measurement for this channel
            String unit = args.get(0);
            _logger.info(String.format("Defining unit %s for this channel", unit));

            stub.putStringState("unit", unit);

            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoke java sensor chaincode by peer");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();

            if (func.equals("getUnit")) {
            	return getUnit(stub, params);
            }
            if (func.equals("write")) {
       			return write(stub, params);
            }
            if (func.equals("query")) {
                return query(stub, params);
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"write\", \"query\", \"getUnit\"]");
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    // query callback representing the query of a chaincode
    private Response query(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            return newErrorResponse("Incorrect number of parameters. Expecting 1");
        }
        String key = args.get(0);

        String val	= stub.getStringState(key);
        if (val == null) {
            return newErrorResponse(String.format("Error: state for %s is null", key));
        }
        _logger.info(String.format("Query Response:\nTag: %s, Value: %s\n", key, val));
        return newSuccessResponse(val, ByteString.copyFrom(val, UTF_8).toByteArray());
    }

	// write callback representing the addition of a new sensor record to the blockchian
    private Response write(ChaincodeStub stub, List<String> params) {
   		if(params.size() != 3) {
   			return newErrorResponse("Invalid number of parameters. Expecting 3");
   		}

		try {
			// check if the client belongs to Org1 (or OrdererMSP as this is what the cli executes the byfn.sh script in)
			ClientIdentity identity = new ClientIdentity(stub);
	   		if(!("Org1MSP".equals(identity.getMSPID()))) {
	   			return newErrorResponse("Only Org1MSP is allowed to write new values to the blockchain, " + identity.getMSPID() + " is not.");
	   		}
   		} catch (Exception e) {
   			newErrorResponse("Could not determine client MSPID");
   		}

		// prepare a formatter for validating and storing the timestamp in the blockchain
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   		String itemTag = params.get(0);
   		int sensorValue = Integer.parseInt(params.get(1));
		LocalDateTime timestamp = LocalDateTime.parse(params.get(2), f);

   		_logger.info(String.format("Writing value/timestamp pair  %s to item-tag %s", sensorValue, itemTag));

		if (itemTag.equals("unit")) {
			return newErrorResponse(String.format("Reserved keyword \"%s\"", itemTag));
		}

   		stub.putStringState(itemTag, String.format("{\"value\": \"%s\", \"timestamp\": \"%s\"}", Integer.toString(sensorValue), timestamp.format(f)));

   		_logger.info("Write complete");

  		return newSuccessResponse("write finished successfully", ByteString.copyFrom(itemTag + ": " + sensorValue, UTF_8).toByteArray());
    }

	// query callback to get the channel's unit of measurement
	private Response getUnit(ChaincodeStub stub, List<String> args) {
		if (args.size() != 0) {
			return newErrorResponse("Incorrect number of parameters. Expecting 0");
		}

		_logger.info("Returning unit of measurement for this channel.");

		String unit = stub.getStringState("unit");

		return newSuccessResponse(unit, ByteString.copyFrom(unit, UTF_8).toByteArray());
	}

    public static void main(String[] args) {
        System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new ACMEChaincode().start(args);
    }

}
