# WebSocket Communication Protocol

Web Flow uses Web Sockets to deliver a better user experience with instant reaction of the web user interface to authorization events. This chapter describes the communication protocol.

## Introduction

Web Flow server uses Web Sockets to notify web frontend about complete authorization events. The Web Socket communication is initiated by a registration handshake. The client listens after registration to authorization messages delivered from the server. Web Sockets are used to provide better responsiveness over polling in authorization steps where authorization is done asynchronously.

All messages use the JSON format. Web Socket communication leverages user topics to avoid broadcasting messages to multiple clients.

## Registration of new Web Socket client

Each web socket client needs to register to initiate communication with the Web Flow server using Web Sockets. During registration a webSocketId value is used. The webSocketId value is unique for each Web Socket communication and is derived from the operationId value used in Web Flow. The Web Socket sessionId is saved during registration and the mapping from webSocketId -> sessionId is kept in memory, so that later during authorization of the step an asynchronous message can be sent to a previously registered client.

The registration is initiated by the client:

CLIENT => SERVER, topic: /user/topic/registration
```json
{
    "webSocketId": "12345678"
}
```

Synchronous response to client request is is received:

SERVER => CLIENT, topic: /user/topic/registration
```json
{
   "webSocketId": "12345678"
}
```

## Authorization messages

Each Web Flow operation has a unique operationId which is mapped to a webSocketId from which the sessionId value is derived (the value is unique for each Web Socket session). The sessionId is used to communicate with a previously registered client who can receive a Web Socket message to get notified about completed authorization step:

SERVER => CLIENT, topic: /user/topic/authorization
```json
{
    "webSocketId": "12345678",
    "authResult": "CONFIRMED"
}
```

This message is not used for the actual authorization, it is used just as a notification of the client that authorization is complete. The client can immediately call the REST API to complete authorization of the step.
