function moveProgressBar(target, progress) {
    const id = setInterval(frame, 10);
    let width = $(target).width();

    function frame() {
        if (width >= progress) {
            clearInterval(id);
        } else {
            width++;
            $(target).width(width + '%');
        }
    }
}