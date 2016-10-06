var app         = require('express')();
var http        = require('http').Server(app);
var io          = require('socket.io')(http);
var bodyParser  = require('body-parser');
var request     = require('request');

/*
 * NOTE: Remember to manually re-route port 80 to 8080 on actual server using
 * sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to 8080
 */
http.listen(8080, function(){
  console.log('Starting server - listening on port :8080');
});

//////////////////////////////
// Push Notification Routes //
//////////////////////////////

// Constants
// Firebase Keys
var API_ACCESS_KEY = "key=AIzaSyDH7MFAWAq9tFKTaGQYjVp5trBD2ZEORT8";
var FCM_URL = 'https://gcm-http.googleapis.com/gcm/send';

// Set the request headers
var headers = {
    'User-Agent'    : 'Super Agent/0.0.1',
    'Content-Type'  : 'application/json',
    'Authorization' : API_ACCESS_KEY
}

app.use(bodyParser.json());

app.post('/send', function(req, res) {
  console.log("Got a POST request");

  // Set the post data
  var json_data = req.body;
  var post_data = {
    'data' : json_data,
    'to': json_data['receiver_id'] 
  };

  // Configure the request
  var options = {
      url: FCM_URL,
      method: 'POST',
      headers: headers,
      json: post_data
  }

  // Send the request
  request(options, function (error, res, body) {
    if (error) {
      console.log("ERROR: " + error);
    }
    else if (res.statusCode == 200) {
        // Print out the response body
        // console.log('response: ' + body)
    }
  });
});

////////////////////////////////
// Socket.IO Messaging Routes //
////////////////////////////////
app.get('/', function(req, res){
  res.sendfile('index.html');
});

io.on('connection', function(socket){
  socket.on('chat message', function(msg){
    console.log('message: ' + msg);
    io.emit('chat message', msg);
  });

  // PepRally Socket Handlers
  socket.on('join_chat', function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderUsername = jsonParsed.sender_username;
    var receiverUsername = jsonParsed.receiver_username;
    console.log(senderUsername + ' joined the chat with ' + receiverUsername);

    // emitting to receiver
    io.emit("on_join_" + senderUsername, "callback_request");
  });

  // requesting call back for sender
  socket.on("callback_ack", function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderUsername = jsonParsed.sender_username;
    var receiverUsername = jsonParsed.receiver_username;
    console.log("got the callback from: " + senderUsername);
    io.emit("on_join_" + senderUsername); // not requesting callback here
  });

  socket.on('leave_chat', function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderUsername = jsonParsed.sender_username;
    var receiverUsername = jsonParsed.receiver_username;
    console.log(senderUsername + ' left the chat with ' + receiverUsername);

    // emitting to receiver
    io.emit("on_leave_" + senderUsername);
  });

  socket.on('send_message', function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderUsername = jsonParsed.sender_username;
    var receiverUsername = jsonParsed.receiver_username;
    // emitting to receiver
    io.emit("new_message_" + receiverUsername, senderUsername);
  });
});
