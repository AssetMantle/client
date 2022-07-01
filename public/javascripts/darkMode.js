function togglePageContentLightDark() {
    let newClass;
    let bodyDiv = $('#body')
    if (bodyDiv.hasClass('lightMode')) {
        $('#toggleLightDark .darkMode').removeClass("hidden");
        $('#toggleLightDark .dayMode').addClass("hidden");
    } else {
        $('#toggleLightDark .dayMode').removeClass("hidden");
        $('#toggleLightDark .darkMode').addClass("hidden");
    }
    let body = document.getElementById('body');
    if (bodyDiv.hasClass('darkMode')) {
        newClass = 'lightMode';
    } else if ($('body').hasClass('lightMode')) {
        newClass = 'darkMode';
    } else {
        newClass = 'darkMode';
    }
    body.className = newClass
    let date = new Date(Date.now() + 86400e3);
    date = date.toUTCString();
    let mode = (newClass === 'lightMode' ? 'light' : 'dark')
    document.cookie = 'theme=' + (newClass === 'lightMode' ? 'light' : 'dark') + " ; expires=" + date;
}

function isDarkThemeSelected() {
    return document.cookie.match(/theme=dark/i) != null
}

function isLightThemeSelected() {
    return document.cookie.match(/theme=light/i) != null
}

$(document).ready(function () {
    let bodyDiv = $('#body')
    if (isDarkThemeSelected() && !isLightThemeSelected()) {
        bodyDiv.className = 'darkMode'
    } else if (!isDarkThemeSelected() && isLightThemeSelected()) {
        bodyDiv.className = 'lightMode'
    } else {
        bodyDiv.className = 'darkMode'
    }
});