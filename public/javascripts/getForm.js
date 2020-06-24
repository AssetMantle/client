function getForm(route, modalContent = '#commonModalContent', modal = '#commonModal', loadingSpinnerID = 'commonSpinner') {
    let loadingSpinner = $('#' + loadingSpinnerID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner('getForm'),
        beforeSend: function () {
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
        statusCode: {
            200: function (data) {
                $(modal).fadeIn(200);
                $(modalContent).html(data);
                $('.modalContent').addClass('fadeInEffect');
            },
            500: function (data) {
                replaceDocument(data.responseText);
            },
        }
    }).fail(function (XMLHttpRequest) {
        if (XMLHttpRequest.readyState === 0) {
            $('#connectionError').fadeIn(100);
        }
    });
}