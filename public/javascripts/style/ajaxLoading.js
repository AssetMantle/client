const hideSpinnerEventList = [];//['chat', 'checkUsernameAvailable', 'comet', 'getForm','recentActivity', 'switcher'];

function showSpinner(event = '') {
    return !hideSpinnerEventList.includes(event);
}