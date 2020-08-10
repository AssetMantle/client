const hideSpinnerEventList = ['chat', 'checkUsernameAvailable', 'comet', 'getForm','recentActivity', 'switcher', 'pageChange'];

function showSpinner(event = '') {
    return !hideSpinnerEventList.includes(event);
}