$(document).ready(function () {
    $('nav a').filter(function(idx, elem) {
        if(elem.pathname !== null && elem.pathname === window.location.pathname){
            $(elem).addClass('active');
        }
    });
});