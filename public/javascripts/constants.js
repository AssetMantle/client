firebaseConfig = {
    apiKey: "AIzaSyBEvfVgcKstwt7qzgnusGTrtHMf3sCEzQY",
    authDomain: "comdex-3be8d.firebaseapp.com",
    databaseURL: "https://comdex-3be8d.firebaseio.com",
    projectId: "comdex-3be8d",
    storageBucket: "comdex-3be8d.appspot.com",
    messagingSenderId: "656465615885",
    appId: "1:656465615885:web:18bfbb26f03fd043278944",
};

dateFormatOptions = {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
};

ws = {
    url: 'wss://' + $(location).attr('host') + '/websocket',
    start: 'START',
};
if ($(location).attr('host') === "localhost:9001") {
    ws.url = 'ws://' + $(location).attr('host') + '/websocket'
}
addState = true
