importScripts('assets/javascripts/constants.js');
importScripts('assets/javascripts/firebase-app.js');
importScripts('assets/javascripts/firebase-messaging.js');

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function (payload) {
    const notificationTitle = JSON.parse(payload.data.notification.title);
    const notificationOptions = {
        body: JSON.parse(payload.notification.body),
    };
    return self.registration.showNotification(notificationTitle, notificationOptions);
});
