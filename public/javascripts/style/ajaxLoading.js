$('#loading').fadeIn(100);
$(document).ajaxStart(function () {
    $('#loading').fadeIn(100);
}).ajaxStop(function () {
    $('#loading').fadeOut(100);
});

const hideSpinnerList = ['chat', 'checkUsernameAvailable', 'comet', 'getForm','recentActivity', 'switcher'];

function showSpinner(event = '') {
    return !hideSpinnerList.includes(event);
}