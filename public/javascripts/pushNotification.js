getConfigurationAsynchronously("notification.apiKey");
getConfigurationAsynchronously("notification.authDomain");
getConfigurationAsynchronously("notification.databaseURL");
getConfigurationAsynchronously("notification.projectId");
getConfigurationAsynchronously("notification.storageBucket");
getConfigurationAsynchronously("notification.senderID");

$(document).ready(function () {
    $("#notificationWindowCommons").load(jsRoutes.controllers.NotificationController.showNotifications(1).url);

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
            if ($("#" + "NOTIFICATION_TOKEN").length != 0) {
                document.getElementById("NOTIFICATION_TOKEN").value = token
            }
        })

        .catch(function (err) {
            console.log("Unable to get permission to notify.", err);
        });
    messaging.onMessage(function (payload) {
        if($("#" + "notificationWindow").length !==0) {
            let newNotification = document.createElement('div');
            newNotification.innerHTML ='</br>' +" (New) " +JSON.parse(JSON.stringify(payload)).notification.title+" + "+JSON.parse(JSON.stringify(payload)).notification.body+ '</br>';
            newNotification.style.backgroundColor="#09c866";
            newNotification.setAttribute("onclick","location.reload()");
            document.getElementById("notificationWindow").insertBefore(newNotification,document.getElementById("notificationWindow").firstElementChild);
            $('#notificationWindow').children().last().remove();
            document.getElementById("notificationCounter").textContent = "New"
        }
    });
});
