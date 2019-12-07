/*
Project ACME chaincode
*/
package org.hyperledger.fabric.example;

import java.util.List;

import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SimpleChaincode extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(SimpleChaincode.class);

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
            /*
            // Initialize the chaincode
            String account1Key = args.get(0);
            int account1Value = Integer.parseInt(args.get(1));
            String account2Key = args.get(2);
            int account2Value = Integer.parseInt(args.get(3));

            _logger.info(String.format("account %s, value = %s; account %s, value %s", account1Key, account1Value, account2Key, account2Value));
            stub.putStringState(account1Key, args.get(1));
            stub.putStringState(account2Key, args.get(3));

            stub.putStringState("1", "5");
			*/

            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoke java sensor chaincode");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            /*if (func.equals("invoke")) {
                return invoke(stub, params);
            }
            if (func.equals("delete")) {
                return delete(stub, params);
            }
            */
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

	/*
    private Response invoke(ChaincodeStub stub, List<String> args) {
        if (args.size() != 3) {
            return newErrorResponse("Incorrect number of arguments. Expecting 3");
        }
        String accountFromKey = args.get(0);
        String accountToKey = args.get(1);

        String accountFromValueStr = stub.getStringState(accountFromKey);
        if (accountFromValueStr == null) {
            return newErrorResponse(String.format("Entity %s not found", accountFromKey));
        }
        int accountFromValue = Integer.parseInt(accountFromValueStr);

        String accountToValueStr = stub.getStringState(accountToKey);
        if (accountToValueStr == null) {
            return newErrorResponse(String.format("Entity %s not found", accountToKey));
        }
        int accountToValue = Integer.parseInt(accountToValueStr);

        int amount = Integer.parseInt(args.get(2));

        if (amount > accountFromValue) {
            return newErrorResponse(String.format("not enough money in account %s", accountFromKey));
        }

        accountFromValue -= amount;
        accountToValue += amount;

        _logger.info(String.format("new value of A: %s", accountFromValue));
        _logger.info(String.format("new value of B: %s", accountToValue));

        stub.putStringState(accountFromKey, Integer.toString(accountFromValue));
        stub.putStringState(accountToKey, Integer.toString(accountToValue));

        _logger.info("Transfer complete");

        return newSuccessResponse("invoke finished successfully", ByteString.copyFrom(accountFromKey + ": " + accountFromValue + " " + accountToKey + ": " + accountToValue, UTF_8).toByteArray());
    }

    // Deletes an entity from state
    private Response delete(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            return newErrorResponse("Incorrect number of arguments. Expecting 1");
        }
        String key = args.get(0);
        // Delete the key from the state in ledger
        stub.delState(key);
        return newSuccessResponse();
    }
	*/
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
   		if(params.size() != 2) {
   			return newErrorResponse("Invalid number of parameters. Expecting 2");
   		}

   		String itemTag = params.get(0);
   		int sensorValue = Integer.parseInt(params.get(1));

   		_logger.info(String.format("Writing sensor value %s to item-tag %s", sensorValue, itemTag));

		if (itemTag.equals("unit")) {
			return newErrorResponse(String.format("Reserved keyword \"%s\"", itemTag));
		}

   		stub.putStringState(itemTag, Integer.toString(sensorValue));

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
        new SimpleChaincode().start(args);
    }

}
