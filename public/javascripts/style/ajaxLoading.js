$('#loading').fadeIn(100);
setTimeout(function() {
    $('#financials, #recentActivities').addClass('fskeleton');
    $('#commonHome, #contact, #identification, #traderViewOrganization, #traderViewOrganizationBankAccount').addClass('skeleton')
},400);
$(document).ajaxStart(function () {
    $('#loading').fadeIn(100);
    setTimeout(function() {
        $('#financials, #recentActivities').addClass('fskeleton');
        $('#commonHome, #contact, #identification, #traderViewOrganization, #traderViewOrganizationBankAccount').addClass('skeleton')
    },400);

}).ajaxStop(function () {
    $('#loading').fadeOut(100);
    $('#financials, #recentActivities').removeClass('fskeleton');
    $('#commonHome, #contact, #identification, #traderViewOrganization, #traderViewOrganizationBankAccount').removeClass('skeleton')
});