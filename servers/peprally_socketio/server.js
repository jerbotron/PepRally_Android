var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

http.listen(8080, function(){
  console.log('Starting server - listening on port :8080');
});

app.get('/', function(req, res){
  res.sendfile('index.html');
});

io.on('connection', function(socket){

  socket.on('chat message', function(msg){
    console.log('message: ' + msg);
    io.emit('chat message', msg)
  });

  // PepRally Socket Handlers
  socket.on('join_chat', function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderNickname = jsonParsed.sender_nickname;
    var receiverNickname = jsonParsed.receiver_nickname;
    console.log(senderNickname + ' joined the chat with ' + receiverNickname);

    // emitting to receiver
    io.emit("on_join_" + senderNickname, "callback_request");
  });

  // requesting call back for sender
  socket.on("callback_ack", function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderNickname = jsonParsed.sender_nickname;
    var receiverNickname = jsonParsed.receiver_nickname;
    console.log("got the callback from: " + senderNickname);
    io.emit("on_join_" + senderNickname); // not requesting callback here
  });

  socket.on('leave_chat', function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderNickname = jsonParsed.sender_nickname;
    var receiverNickname = jsonParsed.receiver_nickname;
    console.log(senderNickname + ' left the chat with ' + receiverNickname);

    // emitting to receiver
    io.emit("on_leave_" + senderNickname);
  });

  socket.on('send_message', function(jsonData){
    var jsonParsed = JSON.parse(jsonData.toString());
    var senderNickname = jsonParsed.sender_nickname;
    var receiverNickname = jsonParsed.receiver_nickname;
    // emitting to receiver
    io.emit("new_message_" + receiverNickname, senderNickname);
  });
});
