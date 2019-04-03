$(document).ready(function () {
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
            if($("#" + "notificationToken").length !=0) {
                document.getElementById('notificationToken').value = token
            }
        })

        .catch(function (err) {
            console.log("Unable to get permission to notify.", err);
        });
    messaging.onMessage(function (payload) {
        console.log("Message received. ", payload);
        if($("#" + "notificationBox").length !=0) {
            var newNotification = document.createElement('div');
            newNotification.innerHTML ='</br>' +" (New) " +JSON.parse(JSON.stringify(payload)).notification.title+" + "+JSON.parse(JSON.stringify(payload)).notification.body+ '</br>';
            newNotification.style.backgroundColor="#09c866";
            newNotification.setAttribute("onclick","location.reload()");
            document.getElementById("notificationBox").insertBefore(newNotification,document.getElementById("notificationBox").firstElementChild);
            $('#notificationBox').children().last().remove();
            $('#noti_Counter').text("!!");
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
