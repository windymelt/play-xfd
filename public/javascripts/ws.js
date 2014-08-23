var ws = new WebSocket("ws://localhost:9000/xfd/ws");

ws.onopen = function () {
    $('body').text('opened');
//    ws.send('hi-ho silver');
};

ws.onmessage = function (message) {
    var parsed = JSON.parse(message.data);
    if (parsed.status == 'running') $('body').attr('style', 'background-color: cyan');
    if (parsed.status == 'success') $('body').attr('style', 'background-color: green');
    if (parsed.status == 'failed') $('body').attr('style', 'background-color: red');
};

ws.onerror = function (evt) {
    $('#message').append(' Error occurred: ' + evt + "\n");
};

