importScripts('https://www.gstatic.com/firebasejs/5.8.5/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/5.8.5/firebase-messaging.js');


firebase.initializeApp({
    'messagingSenderId': '829626980586'
});


const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function (payload) {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);

    if(JSON.parse(payload.data.notification).title === "Login") {
        const notificationTitle = 'Login';
        const notificationOptions = {
            body: 'Login executed',
            icon: 'notificationImage.png'
        };
        return self.registration.showNotification(notificationTitle,
            notificationOptions);
    }
    else if(JSON.parse(payload.data.notification).title === "sendOTP") {
        const notificationTitle = 'OTP Verification';
        const notificationOptions = {
            body: 'Your OTP is ' + JSON.parse(payload.data.notification).passedData,
            icon: 'notificationImage.png'
        };

        return self.registration.showNotification(notificationTitle,
            notificationOptions);
    }
    else {
        const notificationTitle = 'Default Notification';
        const notificationOptions = {
            body: 'Did not redirect from anywhere.',
            icon: 'notificationImage.png'
        };

        return self.registration.showNotification(notificationTitle,
            notificationOptions);
    }
});
