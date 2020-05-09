$('#loading').fadeIn(100);
$(document).ajaxStart(function () {
    $('#loading').fadeIn(100);
}).ajaxStop(function () {
    $('#loading').fadeOut(100);
    console.log("asdfds")
});

// window.addEventListener('DOMContentLoaded', (event) => {
//     $('#loading').fadeOut();
// });
