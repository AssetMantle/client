$(document).ready(function () {
    firebase.initializeApp(firebaseConfig);
    const messaging = firebase.messaging();
    messaging
        .requestPermission()
        .then(function () {
            return messaging.getToken()
        })
        .then(function (token) {
            $('#PUSH_NOTIFICATION_TOKEN').val(token);
        })
        .catch(function (err) {
        });
    //This delivers message in foreground, i.e., when web app is open in browser. https://firebase.google.com/docs/cloud-messaging/js/receive
    messaging.onMessage(function (payload) {
        // alert(JSON.parse(JSON.stringify(payload)).notification.title + " + " + JSON.parse(JSON.stringify(payload)).notification.body);
    });
});