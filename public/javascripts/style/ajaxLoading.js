hideSpinnerEventList = ['chat', 'checkUsernameAvailable', 'refreshCard', 'getForm', 'recentActivity', 'switcher', 'pageChange'];

function showSpinner(event = '') {
    return !hideSpinnerEventList.includes(event);
}