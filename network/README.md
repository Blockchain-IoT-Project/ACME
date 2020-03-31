## Build Your First Network (BYFN)

To start the network, the cryptographic material has to be generated first. To do this, execute `./byfn.sh generate`

After this, the network can be brought up with `./byfn.sh up`, creating all needed docker containers, starting their respective binaries and setting up the network architecture. Towards the end a few preset transactions will be done to verify the network's functionality as well as adding a third organization to the network after it has already been started.

To bring down the network again, use `./byfn.sh down`. This will stop and delete all related docker containers.
