var ws = new WebSocket("ws://localhost:9000/xfd/ws");

ws.onopen = function () {
    $('body').text('opened');
    ws.send('hi-ho silver');
};

ws.onmessage = function (message) {
    $('body').append('<section>' + message.data + '</section>');
};

ws.onerror = function (evt) {
    $('#message').append(' Error occurred: ' + evt + "\n");
};

