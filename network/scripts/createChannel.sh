#!/bin/bash
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
	joinChannelWithRetry 0 1 $CHANNEL_NAME
	echo "===================== peer1.org1 joined channel '$CHANNEL_NAME' ===================== "
	sleep 3
}

echo $1
CHANNEL_NAME=$1
echo "Creating channel $CHANNEL_NAME..."
createChannel $CHANNEL_NAME
joinChannel $CHANNEL_NAME

