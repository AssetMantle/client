$(document).ready(function () {
    if (parseFloat($('#advancePercentage').val()) === 100.0) {
        $('#tentativeDate').val('').prop('disabled', true);
        $('#tenure').val('').prop('disabled', true);
        $('#reference').val('').prop('disabled', true);
    }
    if($('#tentativeDate').val()){
        $('#tenure').val('').prop('disabled',true);
        $('#reference').val('').prop('disabled',true);
    }else if($('#tenure').val() || $('#reference').val()){
        $('#tentativeDate').prop('disabled',true);
    }
});

function advancePercentageInput() {
    if (parseFloat($('#advancePercentage').val()) === 100.0) {
        $('#tentativeDate').val('').prop('disabled', true);
        $('#tenure').val('').prop('disabled', true);
        $('#reference').val('').prop('disabled', true);
    } else {
        if ($('#tentativeDate').val()) {
            $('#tenure').val('').prop('disabled', true);
            $('#reference').val('').prop('disabled', true);
        } else if($('#tenure').val() || $('#reference').val()) {
            $('#tentativeDate').prop('disabled', true);
        } else {
            $('#tentativeDate').prop('disabled', false);
            $('#tenure').prop('disabled', false);
            $('#reference').prop('disabled', false);
        }
    }
}

function tentativeDateInput() {
    if ($('#tentativeDate').val()) {
        $('#tenure').val('').prop('disabled', true);
        $('#reference').val('').prop('disabled', true);
    } else {
        $('#tenure').prop('disabled', false);
        $('#reference').val('').prop('disabled', false);
    }
}

function tenureInput() {
    if ($('#tenure').val() || $('#reference').val()) {
        $('#tentativeDate').val('').prop('disabled', true);
    } else {
        $('#tentativeDate').prop('disabled', false);
    }
}

function refrenceInput() {
    if ($('#tenure').val() || $('#reference').val()) {
        $('#tentativeDate').val('').prop('disabled', true);
    } else {
        $('#tentativeDate').prop('disabled', false);
    }
}

