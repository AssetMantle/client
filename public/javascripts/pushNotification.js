$(document).ready(function () {

    MsgElem = document.getElementById("msg");
    TokenElem = document.getElementById("token");
    NotisElem = document.getElementById("notis");
    ErrElem = document.getElementById("err");
    var config = {
        apiKey: "AIzaSyA1N73fWM03Vb1CnEUF1YTbZUj16IxMvdg",
        authDomain: "corp-play-scala.firebaseapp.com",
        databaseURL: "https://corp-play-scala.firebaseio.com",
        projectId: "corp-play-scala",
        storageBucket: "corp-play-scala.appspot.com",
        messagingSenderId: "829626980586"
    };
    firebase.initializeApp(config);
    const messaging = firebase.messaging();
    messaging
        .requestPermission()
        .then(function () {
            MsgElem.innerHTML = "Notification permission granted."
            console.log("Notification permission granted.");

            return messaging.getToken()
        })
        .then(function (token) {
            document.getElementById('token').value = token
            TokenElem.innerHTML = token;
        })

        .catch(function (err) {
            ErrElem.innerHTML = ErrElem.innerHTML + "; " + err
            console.log("Unable to get permission to notify.", err);
        });
    messaging.onMessage(function (payload) {
        console.log("Message received. ", payload);
        NotisElem.innerHTML = NotisElem.innerHTML + JSON.stringify(payload)
    });

});