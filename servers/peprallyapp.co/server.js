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
      user: 'peprallyapp@gmail.com',
      pass: ''
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
var API_ACCESS_KEY = "key=AIzaSyDH7MFAWAq9tFKTaGQYjVp5trBD2ZEORT8";
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

/////////////////////////////
// DynamoDB Service Routes //
/////////////////////////////
// Load the AWS SDK for Node.js
var AWS = require('aws-sdk');
// Load credentials and set region from JSON file
AWS.config.loadFromPath('./config/config.json');

// Create DynamoDB service object
var ddb = new AWS.DynamoDB({apiVersion: '2012-08-10'});
var docClient = new AWS.DynamoDB.DocumentClient();

app.get('/test', function(req, res) {
  var params = {
    TableName: 'UserNotifications',
    KeyConditionExpression: 'Username = :n',
    ExpressionAttributeValues: {
      ':n' : {S: 'kev'}
    }
  };

  ddb.query(params, function(err, data) {
    if (err) {
      console.log("Error querying DDB: ", err);
    } else {
      data.Items.forEach(function(item) {
        console.log("time stamp: " + item.TimestampSeconds.N);
      });
    }
  });
});

app.get('/scan', function(req, res) {
  var params = {
    TableName: 'Usernames'
  };

  ddb.scan(params, function(err, data) {
    if (err) {
      console.log("Error scanning DDB: ", err);
    } else {
      data.Items.forEach(function(item) {
        console.log("Username: " + item.Username.S);
      });
    }
  });
  res.end('scanning db');
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
