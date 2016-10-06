// ExpressJS Server Setup
var express = require('express');
var app     = express();
var server  = require('http').Server(app);

// Other library includes
var io          = require('socket.io')(server);
var bodyParser  = require('body-parser');
var request     = require('request');
var nodemailer = require('nodemailer');
var smtpTransport = require("nodemailer-smtp-transport")

//////////////////////////////////////////
// Serve static html for peprallyapp.co //
app.use(express.static(__dirname + '/public'));
app.use(bodyParser.json());

////////////////////////////
// Contact Me Email Route //
////////////////////////////
var transport = nodemailer.createTransport(smtpTransport({
  host: 'smtp.gmail.com',
  secureConnection: false, // use SSL
  port: 587, // port for secure SMTP
  auth: {
      user: '***',
      pass: '***'
  } 
}));

// Contact me email script
app.get('/send',function(req,res){
    var message = "Phone number: " + req.query.phone + "\nEmail: " + req.query.email + "\n\n" + req.query.message;
    var mailOptions={
      from: '"' + req.query.name + '" <' + req.query.email + '>',
        to: 'wyjeremy@gmail.com',
        subject: "peprallyapp.co Inquiry",
        text: message
    }
    console.log(mailOptions);
    transport.sendMail(mailOptions, function(error, response){
        if (error){
          console.log(error);
          res.end("error");
        } else {
          console.log("Message sent");
          res.end("sent");
        }
    });
});

//////////////////////////////
// Push Notification Routes //
//////////////////////////////

// Constants
// Firebase Keys
var API_ACCESS_KEY = "***";
var FCM_URL = 'https://gcm-http.googleapis.com/gcm/send';

// Set the request headers
var headers = {
    'User-Agent'    : 'Super Agent/0.0.1',
    'Content-Type'  : 'application/json',
    'Authorization' : API_ACCESS_KEY
}

app.post('/push', function(req, res) {
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
        console.log('response: ' + body)
    }
  });
});

////////////////////////////////
// Socket.IO Messaging Routes //
////////////////////////////////
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

/*
 * NOTE: Remember to manually re-route port 80 to 8080 on actual server using
 * sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to 8080
 */
server.listen(8080, function(){
  console.log('Starting server - listening on port :8080');
});
