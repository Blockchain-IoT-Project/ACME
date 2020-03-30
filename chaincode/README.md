# Chaincode

Our chaincode supports several functions in order to write a new sensor value with a given item id to the blockchain and to read said value with a given tag.
Furthermore we store information about the sensor's unit of measurement upon chaincode instantiation, which cannot be altered at a later time.
This functionality is realized in the following methods:

- `init [unit]` is called upon chaincode instantiation.
- `write [item id] [value] [timestamp]` writes a new sensor value and its timestamp (formatted in yyyy-MM-dd HH:mm:ss) onto the blockchain with the given item id as the key
- `query [item id]` returns the sensor value and timestamp in a json formatted string
- `getUnit` returns the sensor's unit of measurement

## Todo

Implement some sort of authorization with the help of the client identification library in order to restrict the sensor organization to only write and the others to only read data from the blockchain.