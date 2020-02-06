# Short Status update
- Runs only when one peer has to endorse, endorsement policies with 2 peers lead to an error response on the transaction request
- Therefore to make it work, instantiate the chaincode with `-P OR ('ORG1MSP.peer', 'ORG2MSP.peer')`, and not with AND
- Also tidy up the mess that is InvokeChaincode.java
- For debugging on the endorsment issue, check docker logs with `docker logs peer[i].org[j].acme.org` or `docker logs orderer.acme.org`
- Invoke runs with no error and return the chaincode newSuccessResponse
- Input: [tagname] [value] optional [c1/c2]
- Output: Status + ProposelResponse -> Response
