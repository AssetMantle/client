// (function ($) {
//     $('#togglePageContentLightDark').on('click', function () {
//
//         $('body').toggleClass('dark-mode');
//         if ($('body').hasClass('dark-mode')) {
//             writeCookie("displaymode", "dark");
//             window.mode = 'dark';
//             document.cookie = "displaymode = normal ; expires=" + date;
//
//         }
//         else {
//             let date = new Date(Date.now() + 86400e3);
//             date = date.toUTCString();
//             document.cookie = key + "=" + value +  " ; expires=" + date;
//             document.cookie = "displaymode = normal ; expires=" + date;
//             writeCookie("displaymode", "normal");
//             window.mode = 'normal';
//         }
//     });
//     function writeCookie(key, value) {
//         console.log("raj");
//         let date = new Date(Date.now() + 86400e3);
//         date = date.toUTCString();
//         document.cookie = key + "=" + value +  " ; expires=" + date;
//         window.document.cookie = key + "=" + value + "; expires=" + date.toGMTString() + "; path=/"; return value;
//
//     }
//
// })(jQuery);


function togglePageContentLightDark() {
    var body = document.getElementById('body')
    var currentClass = body.className
    if ($('body').hasClass('dark-mode')) {
        var newClass ='light-mode';
    }
    else if ($('body').hasClass('light-mode')) {
        var newClass ='dark-mode';
    }
    body.className = newClass
    let date = new Date(Date.now() + 86400e3);
    date = date.toUTCString();
    document.cookie = 'theme=' + (newClass == 'dark-mode' ? 'dark' : 'light') + " ; expires=" + date;
    console.log('Cookies are now: ' + document.cookie)
}

function isDarkThemeSelected() {
    return document.cookie.match(/theme=dark/i) != null
}

function setThemeFromCookie() {
    var body = document.getElementById('body')
    const bg =isDarkThemeSelected();
    console.log(bg)
    console.log('Cookies are now: ' + document.cookie)
    body.className = isDarkThemeSelected() ? 'dark-mode' : 'light-mode'
}

(function() {
    setThemeFromCookie()
    isDarkThemeSelected()

})();



















//
// function setThemeFromCookie() {
//     var pageContent = document.getElementById('page-content')
//     pageContent.className = isDarkThemeSelected() ? 'page-content' : 'page-content-light'
// }
//
// function isDarkThemeSelected() {
//     return document.cookie.match(/theme=dark/i) != null
// }
//
// function setLightDarkSwitchState() {
//     document.getElementById('lightDarkSwitchInput').checked = isDarkThemeSelected()
// }
//
// function togglePageContentLightDark() {
//     var pageContent = document.getElementById('page-content')
//     var currentClass = pageContent.className;
//     var newClass = pageContent.className == 'page-content' ? 'page-content-light' : 'page-content'
//     pageContent.className = newClass
//
//     document.cookie = 'theme=' + (newClass == 'page-content-light' ? 'light' : 'dark')
// }
//
// (function() {
//     setThemeFromCookie()
//     setLightDarkSwitchState()
//     document.getElementById('lightDarkSwitchInput').onchange=togglePageContentLightDark
// })();
