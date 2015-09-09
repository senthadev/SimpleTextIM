# SimpleTextIM
A simple text based IM server

Basic functionality of this server:
  1. Sending a public message to everyone (ie public group chat)
  2. Sending a private message to a given user
  3. Listing what users are connected
  4. Notify connected users when new users join or logged-in users leave

## How to run it

*Compiling and running(use java 8)*

> git clone https://github.com/senthadev/SimpleTextIM.git

> cd SimpleTextIM

> mkdir bin

e.g: 
/Users/sentha/SimpleTextIM/src -> contains the source files (*.java)
/Users/sentha/SimpleTextIM/bin -> contains the class files (*.class)

> javac -d bin -sourcepath src src/com/senthadev/core/SimpleIMServer.java

e.g: /Users/sentha/SimpleTextIM : javac -d bin -sourcepath src src/com/senthadev/core/SimpleIMServer.java 

> javac -d bin -sourcepath src src/com/senthadev/client/UIClient.java 

> cd bin

*Starting the server*

(move to bin directory)
// This starts the server in port 1000
> java com.senthadev.core.SimpleIMServer 10000

e.g: /Users/sentha/SimpleTextIM/bin : java com.senthadev.core.SimpleIMServer 10000

*Starting the client*

(move to bin directory)
// This starts a the client UI
// java com.senthadev.client.UIClient server_host server_port login_name login_password

>java com.senthadev.client.UIClient 127.0.0.1 10000 bob bob_secrect_password

e.g: /Users/sentha/SimpleTextIM/bin : java com.senthadev.client.UIClient 127.0.0.1 10000 bob bob_secrect_password


Following commands are available for clients to send messages

command  | Descriptions
------------- | -------------
send  | send hello world
	| This will send a broadcast message, hello world, to all the online clients
list  | list 
	| Displays the list of online clients
private:client  | private:alice ready for pizza?
	| This will send a private message, ready for pizza?, to client alice.


## Design details

Server and clients communicates based on JSON format.
Let us look into the communication payloads.

### Client sends following payload

Key  | Allowed values
------------- | -------------
command  | [login , send, list, exit]
	| login -> (logins to chat with login details, user_name/password)
	| send -> (sends chat messages)
	| list -> (list of all users who are currently logged in)
	| exit -> (ends the chat and closes the sessions)	
message  | [string] 
	| "text" -> (contains the chat text and maximum length is 140 characters)
to  | [user_id, *]
	| user_id -> (sends a private message to the this user)
	| * -> (sends a public message )

```json
// login payload
{
	"command": "login alice/alice_top_hidden_pwd"
}

// sends a public message
{
	"command": "send",
	"message": "Hello group" 
}

// list all current online members
{
	"command": "list"
}

// sends a private message to bob
{
	"command": "send",
	"message": "When is the party?",
	"to": "bob" 
}
```

### Server replies with following payload

Key  | Allowed values
------------- | -------------
command | [message, list, fail]
	| message -> (server response contains chat messages)
	| list -> (server response contains list of active users)
	| fail -> (client request failed)
messages  | [{user_id: user_id, message: string}, ] 
	| array of pending messages
list  | [{user_id: user_id}, ] 
	| array of active users
fail  | {error_code: code, reason: reason_text} 
	| reason with error codes

```json
// after user logged in,
{
	"command": "message",
	"messages": [{"user_id": "bob", "message": "where is alice?"}, {}]
}

// list response
{
	"command": "list",
	"list": ["bob", "mark"] 
}

```

### How server process the requests

 
![design][logo]

[logo]: https://github.com/senthadev/SimpleTextIM/raw/master/doc/images/SimpleTextIM.png "Design"

### High level responsibility of each modules

*1. Request Handler*

This module handles the initial login requests. Once the user is validated, it adds an entry in the mapping table with reference to a newly created
Actor instance and pass the socket object for further communications. If it's already exists, then socket object is passed for further communications.
Once login is successful, it submits a message, stating that a new user has logged in, to the Message Handler, to inform all other active users.

*2. Message Handler*

This module's responsibility is to pass the messages to requested mailbox.
For example, if the message is for Alice then it calls the Alice's Actor object via looking up the mapping table and passes the messages to it's mailbox.
If it's a group message, then it passes the messages to all the mailboxes.

*3. Actor*

This module's responsibility is to communicate with it's client via socket and deliver the messages.
Also, receives the messages from the client and pass it to the Message Handler for further processing.
Mailbox is a queue, which stores the messages (payloads) which needs to be processed.
For example, a private message to Bob.

*4. SimpleTextIMServer*

This module is to provide the IM server service. Once, after the successful TCP connection, it passes the request to Request Handler for further processing.

*5. Client*

This is a client module which provides a simple UI to send and receive messages.


