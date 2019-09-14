function submitFormOnEnter(event, source) {
    if (event.keyCode === 13) {
        event.preventDefault();
        validateForm(source);
    }
}
