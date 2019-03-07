importScripts('https://www.gstatic.com/firebasejs/5.8.5/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/5.8.5/firebase-messaging.js');


firebase.initializeApp({
    'messagingSenderId': '829626980586'
});


const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function (payload) {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);
    // Customize notification here
    const notificationTitle = 'Answer to some things';
    const notificationOptions = {
        body: 'This is not the answer to life, universe and everything that comes in the middle.',
        icon: 'notificationImage.png'
    };

    return self.registration.showNotification(notificationTitle,
        notificationOptions);
});
