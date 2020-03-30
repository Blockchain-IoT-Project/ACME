#!/bin/bash
export PATH=${PWD}/../bin:${PWD}:$PATH
export FABRIC_CFG_PATH=${PWD}
export VERBOSE=false

# import utils
. scripts/utils.sh

function printHelp() {
  echo "Usage: "
  echo "  addChannel.sh CHANNEL_NAME  "
  echo " Adds CHANNEL_NAME as a new Channel "
}


function GenChannel() {
  which configtxgen
  if [ "$?" -ne 0 ]; then
    echo "configtxgen tool not found. exiting"
    exit 1
  fi

  echo
  echo "###############################################################################"
  echo "### Generating channel configuration transaction 'channel_$CHANNEL_NAME.tx' ###"
  echo "###############################################################################"
  set -x
  configtxgen -profile OneOrgChannel -outputCreateChannelTx ./channel-artifacts/channel_$CHANNEL_NAME.tx -channelID $CHANNEL_NAME
  res=$?
  set +x
  if [ $res -ne 0 ]; then
    echo "Failed to generate channel configuration transaction..."
    exit 1
  fi
}

CHANNEL_NAME=$1
GenChannel
docker exec cli scripts/createChannel.sh $CHANNEL_NAME

while getopts "h?" opt; do
  # shellcheck disable=SC1073
  case "$opt" in
  h | \?)
    printHelp
    exit 0
    ;;
  esac
done
