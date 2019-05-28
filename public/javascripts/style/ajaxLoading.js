$('#loading').fadeOut(100);
$(document).ajaxStart(function () {
    $('#loading').fadeIn(100);
}).ajaxStop(function () {
    $('#loading').fadeOut(100);
});