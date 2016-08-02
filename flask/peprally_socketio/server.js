var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.get('/', function(req, res){
  res.sendfile('index.html');
});

io.on('connection', function(socket){
  // console.log('a user connected');
  // socket.on('disconnect', function(){
  //   console.log('user disconnected');
  // });

  socket.on('chat message', function(msg){
    console.log('message: ' + msg);
    io.emit('chat message', msg)
  });

  // PepRally Socket Handlers
  socket.on('join_chat', function(nickname){
    console.log(nickname + ' joined the chat');
    io.emit(nickname, 'welcome ' + nickname)
  });

  socket.on('leave_chat', function(nickname){
    console.log(nickname + ' left the chat');
    io.emit(nickname, 'goodbye ' + nickname)
  });

  socket.on('send_message', function(jsonData){
    var jsonParsed = JSON.parse(jsonData);
    var receiverNickname = jsonParsed.receiver_nickname;
    var senderNickname = jsonParsed.sender_nickname;
    console.log("receiver_nickname = " + receiverNickname)
    console.log("sender_nickname = " + senderNickname)
    // io.emit(receiverNickname, senderNickname);
  });
});

http.listen(8080, function(){
  console.log('Starting server - listening on port :8080');
});