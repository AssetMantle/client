$(document).ready(function () {

    $('#username').keyup(function () {

        const username = $(this).val();
        const result = $('#result');

        if (username.length > 2) {
            result.html('loading');
            $.ajax({
                url: 'checkUsernameAvailable',
                type: 'GET',
                data: {
                    username: username//,
                    //csrfToken: $('[name="csrfToken"]').attr('value')
                },
                statusCode: {
                    200: function () {
                        result.html('available');
                    },
                    204: function () {
                        result.html('taken');
                    },
                }
            });
        } else {
            result.html('Enter at least 3 characters');
        }
        if (username.length === 0) {
            result.html('');
        }
    });
});

//https://stackoverflow.com/questions/11133059/play-2-x-how-to-make-an-ajax-request-with-a-common-button