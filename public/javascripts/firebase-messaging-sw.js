importScripts('https://www.gstatic.com/firebasejs/5.8.5/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/5.8.5/firebase-messaging.js');


firebase.initializeApp({
    'messagingSenderId': '829626980586'
});


const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function (payload) {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);

    const notificationTitle = JSON.parse(payload.data.notification.title);
    const notificationOptions = {
        body: JSON.parse(payload.notification.body),
    };
    return self.registration.showNotification(notificationTitle, notificationOptions);

});
