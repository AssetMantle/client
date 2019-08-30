function validateForm(event, source) {
    if (event.keyCode === 13) {
        event.preventDefault();
        submitForm(source);
    }
}