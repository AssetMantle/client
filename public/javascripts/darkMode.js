function togglePageContentLightDark() {
    if($('body').hasClass('lightMode')){
        $('#toggleLightDark .darkMode').removeClass("hidden");
        $('#toggleLightDark .dayMode').addClass("hidden");
    }
    else {
        $('#toggleLightDark .dayMode').removeClass("hidden");
        $('#toggleLightDark .darkMode').addClass("hidden");
    }
    var body = document.getElementById('body')
    var currentClass = body.className
    if ($('body').hasClass('darkMode')) {
        var newClass ='lightMode';
    }
    else if ($('body').hasClass('lightMode')) {
        var newClass ='darkMode';
    }
    body.className = newClass
    let date = new Date(Date.now() + 86400e3);
    date = date.toUTCString();
    document.cookie = 'theme=' + (newClass == 'darkMode' ? 'dark' : 'light') + " ; expires=" + date;
}

function isDarkThemeSelected() {
    return document.cookie.match(/theme=dark/i) != null
}

function setThemeFromCookie() {
    var body = document.getElementById('body')
    body.className = isDarkThemeSelected() ? 'darkMode' : 'lightMode'
}

(function() {
    setThemeFromCookie()
    isDarkThemeSelected()

})();

