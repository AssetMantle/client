$(document).ready(function () {
    $("#notificationWindowCommons").load(jsRoutes.controllers.NotificationController.showNotifications(1).url);

    var config = {
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
        if($("#" + "notificationWindow").length !=0) {
            var newNotification = document.createElement('div');
            newNotification.innerHTML ='</br>' +" (New) " +JSON.parse(JSON.stringify(payload)).notification.title+" + "+JSON.parse(JSON.stringify(payload)).notification.body+ '</br>';
            newNotification.style.backgroundColor="#09c866";
            newNotification.setAttribute("onclick","location.reload()");
            document.getElementById("notificationWindow").insertBefore(newNotification,document.getElementById("notificationWindow").firstElementChild);
            $('#notificationWindow').children().last().remove();
            document.getElementById("notificationCounter").textContent = "New"
        }
    });

    function getConfiguration(configuration) {
        var route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
        var response = null;
        $.ajax({
            url: route.url,
            type: route.type,
            async: false,
            statusCode: {
                200: function (result) {
                    response= result;
                }
            }
        });
        return response;
    }
});
