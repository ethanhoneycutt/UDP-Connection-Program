Ethan Honeycutt

Communication Program

The folder contains this readme and two files, Server.java and Client.java.

Compile with

javac *.java

start the server with

java Server port
	for example,
java Server 8080

then start up a client with

java Client host port
	for example,
java Client 0.0.0.0 8080

When prompted, enter a single character.
The client will attempt to send the character with an appropriate sequence number
starting at 0 to the server. The sever will respond with an ACK with the same sequence
number if there are no problems, or a duplicate ACK if the sequence numbers are incorrect.
If the server does not repond within 5 seconds, the client will resend the message and
wait until there is a response.