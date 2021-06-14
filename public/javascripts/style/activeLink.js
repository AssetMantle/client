$(document).ready(function () {
    $('nav .cmuk-navbar-nav li a').filter(function(idx, elem) {
        const location = window.location.pathname;
        if('/'+elem.getAttribute("id") === location){
            $(elem).addClass('active');
        }else if(location == '/'){
            $('#dashBoard').addClass('active');
        }else if(location.startsWith('/validator')){
            $('#validators').addClass('active');
        }else if(location.startsWith('/block')){
            $('#blocks').addClass('active');
        }else if(location.startsWith('/transaction')){
            $('#transactions').addClass('active');
        }
    });
});

function setActiveLink(e){
    var elems = document.querySelectorAll(".active");
    [].forEach.call(elems, function(el) {
        el.classList.remove("active");
    });
    e.target.className = "active";
}