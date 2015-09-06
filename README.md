# SimpleTextIM
A simple text based IM server

Basic functionality of this server:
  1. Sending a public message to everyone (ie public group chat)
  2. Sending a private message to a given user
  3. Listing what users are connected
  4. Notify connected users when new users join or logged-in users leave

## Design details

Server and clients communicates based on JSON format.
Let us look into the communication payloads.

### Client sends payload

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
	"to", "bob" 
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
	"messages": [{"user_id": "bob", "message": "where is alice?"}]
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

### High level responsibility of each components


