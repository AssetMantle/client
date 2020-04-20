function submitFormOnEnter(event, source) {
    if (event.keyCode === 13) {
        event.preventDefault();
        console.log(source);
        submitForm(source);
    }
}
