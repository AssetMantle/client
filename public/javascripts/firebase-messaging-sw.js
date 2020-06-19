importScripts('assets/javascripts/constants.js');
importScripts('assets/javascripts/firebase-app.js');
importScripts('assets/javascripts/firebase-messaging.js');

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

//This delivers message in background. https://firebase.google.com/docs/cloud-messaging/js/receive
messaging.setBackgroundMessageHandler(function (payload) {
    const notificationTitle = JSON.parse(payload.data.notification.title);
    const notificationOptions = {
        body: JSON.parse(payload.notification.body),
    };
    return self.registration.showNotification(notificationTitle, notificationOptions);
});
