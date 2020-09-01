$(document).ready(function () {
    $('nav a').filter(function(idx, elem) {
        if(elem.pathname !== null && elem.pathname === window.location.pathname){
            $(elem).addClass('active');
        }else if(window.location.pathname == '/'){
            $('#dashBoard a').addClass('active');
        }
    });
});