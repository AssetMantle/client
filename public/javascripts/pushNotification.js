getConfigurationAsynchronously("pushNotification.apiKey");
getConfigurationAsynchronously("pushNotification.authDomain");
getConfigurationAsynchronously("pushNotification.databaseURL");
getConfigurationAsynchronously("pushNotification.projectId");
getConfigurationAsynchronously("pushNotification.storageBucket");
getConfigurationAsynchronously("pushNotification.senderID");

$(document).ready(function () {
    console.log("PUSH_NOTIFICATION");
    let config = {
        apiKey: getConfiguration("pushNotification.apiKey"),
        authDomain: getConfiguration("pushNotification.authDomain"),
        databaseURL: getConfiguration("pushNotification.databaseURL"),
        projectId: getConfiguration("pushNotification.projectId"),
        storageBucket: getConfiguration("pushNotification.storageBucket"),
        messagingSenderId: getConfiguration("pushNotification.senderID")
    };
    firebase.initializeApp(config);
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
    messaging.onMessage(function (payload) {
        alert(JSON.parse(JSON.stringify(payload)).notification.title + " + " + JSON.parse(JSON.stringify(payload)).notification.body);
    });
});