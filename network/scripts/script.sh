#!/bin/bash

echo
echo " ____    _____      _      ____    _____ "
echo "/ ___|  |_   _|    / \    |  _ \  |_   _|"
echo "\___ \    | |     / _ \   | |_) |   | |  "
echo " ___) |   | |    / ___ \  |  _ <    | |  "
echo "|____/    |_|   /_/   \_\ |_| \_\   |_|  "
echo
echo
CHANNEL_NAME="$1"
DELAY="$2"
LANGUAGE="$3"
TIMEOUT="$4"
VERBOSE="$5"
NO_CHAINCODE="$6"
: ${CHANNEL_NAME:="mychannel"}
: ${DELAY:="3"}
: ${LANGUAGE:="java"}
: ${TIMEOUT:="10"}
: ${VERBOSE:="false"}
: ${NO_CHAINCODE:="false"}
LANGUAGE=`echo "$LANGUAGE" | tr [:upper:] [:lower:]`
COUNTER=1
MAX_RETRY=10

CC_SRC_PATH="github.com/chaincode/chaincode_example02/go/"
if [ "$LANGUAGE" = "node" ]; then
	CC_SRC_PATH="/opt/gopath/src/github.com/chaincode/chaincode_example02/node/"
fi

if [ "$LANGUAGE" = "java" ]; then
	CC_SRC_PATH="/opt/gopath/src/github.com/chaincode/"
fi

echo "Channel name : "$CHANNEL_NAME

# import utils
. scripts/utils.sh

createChannel() {
	CHANNEL_NAME=$1
	setGlobals 0 1

	if [ -z "$CORE_PEER_TLS_ENABLED" -o "$CORE_PEER_TLS_ENABLED" = "false" ]; then
                set -x
		peer channel create -o orderer.acme.org:7050 -c $CHANNEL_NAME -f ./channel-artifacts/channel_$(echo $CHANNEL_NAME).tx >&log.txt
		res=$?
                set +x
	else
				set -x
		peer channel create -o orderer.acme.org:7050 -c $CHANNEL_NAME -f ./channel-artifacts/channel_$(echo $CHANNEL_NAME).tx --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA >&log.txt
		res=$?
				set +x
	fi
	cat log.txt
	verifyResult $res "Channel creation failed"
	echo "===================== Channel '$CHANNEL_NAME' created ===================== "
	echo
}

joinChannel () {
	CHANNEL_NAME=$1
	for org in 1 2; do
	    for peer in 0 1; do
		joinChannelWithRetry $peer $org $CHANNEL_NAME
		echo "===================== peer${peer}.org${org} joined channel '$CHANNEL_NAME' ===================== "
		sleep $DELAY
		echo
	    done
	done
}

## Create channel 's1'
echo "Creating channel 's1'..."
createChannel 's1'

## Create channel 's2'
echo "Creating channel 's2'..."
createChannel 's2'



## Join all the peers to the channel 's1'
echo "Having all peers join the channel 's1'..."
joinChannel 's1'

## Set the anchor peers for each org in s1
echo "Updating anchor peers for org1 in s1..."
updateAnchorPeers 0 1 's1'
echo "Updating anchor peers for org2 in s1..."
updateAnchorPeers 0 2 's1'



## Join all the peers to the channel 's2'
echo "Having all peers join the channel 's2'..."
joinChannel 's2'

## Set the anchor peers for each org in s22
echo "Updating anchor peers for org1 in s2..."
updateAnchorPeers 0 1 's2'
echo "Updating anchor peers for org2 in s2..."
updateAnchorPeers 0 2 's2'

if [ "${NO_CHAINCODE}" != "true" ]; then

	## Install chaincode on peer0.org1 and peer0.org2 for s1
	echo "Installing chaincode on peer0.org1..."
	installChaincode 0 1 's1' 1.0
	echo "Install chaincode on peer0.org2..."
	installChaincode 0 2 's1' 1.0

	## Install chaincode on peer0.org1 and peer0.org2 for s2
	echo "Installing chaincode on peer0.org1..."
	installChaincode 0 1 's2' 1.0
	echo "Install chaincode on peer0.org2..."
	installChaincode 0 2 's2' 1.0
	
	# Instantiate chaincode on peer0.org2 and peer0.org1 in s1
	echo "Instantiating chaincode on peer0.org2 in s1..."
	instantiateChaincode 0 2 's1'
	echo "Instantiating chaincode on peer0.org2 in s1..."
	instantiateChaincode 0 1 's1'
	
    # Instantiate chaincode on peer0.org2 and peer0.org1 in s2
	echo "Instantiating chaincode on peer0.org2 in s2..."
	instantiateChaincode 0 2 's2'
	echo "Instantiating chaincode on peer0.org2 in s2..."
	instantiateChaincode 0 1 's2'

	# Invoke chaincode on peer0.org1 and peer0.org2 in s1
	echo "Sending invoke transaction on peer0.org1 peer0.org2 in s1..."
	chaincodeInvoke 's1' 0 1 0 2

	# Invoke chaincode on peer0.org1 and peer0.org2 in s2
	echo "Sending invoke transaction on peer0.org1 peer0.org2 in s2..."
	chaincodeInvoke 's2' 0 1 0 2
	

	## Install chaincode on peer1.org2
	echo "Installing chaincode on peer1.org2..."
	installChaincode 1 2 's2' 1.0

	# Query chaincode on peer0.org1 in s2
	echo "Querying chaincode on peer0.org1 in s1..."
	chaincodeQuery 0 1 's2' '{"value": "4", "timestamp": "1994-01-13 14:22:11"}'

	# Now query on chaincode on peer1.org2 in s2
	echo "Querying chaincode on peer1.org2 in s2..."
	chaincodeQuery 1 2 's2' '{"value": "4", "timestamp": "1994-01-13 14:22:11"}'
fi

echo
echo "========= All GOOD, BYFN execution completed =========== "
echo

echo
echo " _____   _   _   ____   "
echo "| ____| | \ | | |  _ \  "
echo "|  _|   |  \| | | | | | "
echo "| |___  | |\  | | |_| | "
echo "|_____| |_| \_| |____/  "
echo

exit 0
