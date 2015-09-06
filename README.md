# SimpleTextIM
A simple text based IM server

Basic functionality of this server:
  1. Sending a public message to everyone (ie public group chat)
  2. Sending a private message to a given user
  3. Listing what users are connected
  4. Notify connected users when new users join or logged-in users leave

## Design details

Server and clients communicates based on JSON format.

### Client sends following payload
