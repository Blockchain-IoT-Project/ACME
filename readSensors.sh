#!/bin/bash

# This script needs a compiled java application in order to produce a set of random dummy data
# to be written into the blockchain.

# the maximum temperature value
MAXTEMP=400

# path to the java executable
JARPATH=$1
CHANNEL=$2
: ${CHANNEL:=""}
# colors, baby!
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# the set of items that will pass our sensors
TAGS="t1 t2 t3 t4"

# iterate over the tags
for tag in $TAGS; do
  # get a first random number for that tag

  echo -e "${BLUE}Reading values for tag $tag ${NC}"
  java -cp $JARPATH  QueryChaincode $tag $CHANNEL

  if [ $? -eq 0 ]; then
    echo
  	echo -e "${GREEN}SUCCESS!${NC}"
  	echo
  else
    echo
  	echo -e "${RED}FAILURE!${NC}"
  	echo
  fi

  echo
  echo
  echo
done

exit 0
