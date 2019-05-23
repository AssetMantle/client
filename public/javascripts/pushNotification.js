getConfigurationAsynchronously("notification.apiKey");
getConfigurationAsynchronously("notification.authDomain");
getConfigurationAsynchronously("notification.databaseURL");
getConfigurationAsynchronously("notification.projectId");
getConfigurationAsynchronously("notification.storageBucket");
getConfigurationAsynchronously("notification.senderID");

$(document).ready(function () {

    let config = {
        apiKey: getConfiguration("notification.apiKey"),
        authDomain: getConfiguration("notification.authDomain"),
        databaseURL: getConfiguration("notification.databaseURL"),
        projectId: getConfiguration("notification.projectId"),
        storageBucket: getConfiguration("notification.storageBucket"),
        messagingSenderId: getConfiguration("notification.senderID")
    };
    firebase.initializeApp(config);
    const messaging = firebase.messaging();
    messaging
        .requestPermission()
        .then(function () {
            return messaging.getToken()
        })
        .then(function (token) {
            if ($("#" + "NOTIFICATION_TOKEN").length !== 0) {
                document.getElementById("NOTIFICATION_TOKEN").value = token
            }
        })

        .catch(function (err) {
            console.log("Unable to get permission to notify.", err);
        });
    messaging.onMessage(function (payload) {
        alert(JSON.parse(JSON.stringify(payload)).notification.title + " + " + JSON.parse(JSON.stringify(payload)).notification.body);
    });
});