$(document).ready(function () {

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
            return messaging.getToken()
        })
        .then(function (token) {
            document.getElementById('submitToken').value = token
        })

        .catch(function (err) {
            console.log("Unable to get permission to notify.", err);
        });
    messaging.onMessage(function (payload) {
        console.log("Message received. ", payload);
    });

});
