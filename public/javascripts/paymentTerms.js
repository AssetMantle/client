function advancePaymentInput(source,advancePercentage,credit){
    if($(source).prop('checked') === false){
        $(credit).prop('checked',true);
        $(advancePercentage).val('').prop('disabled',true).prop('required',false);
    }else{
        $(advancePercentage).prop('disabled',false).prop('required',true).val('0');
    }
}

function advancePercentageInput(source,credit){
    if(parseFloat($(source).val()) === 100.0){
        $(credit).prop('checked',false);
    }else{
        $(credit).prop('checked',true);
    }
}

function creditInput(source,advancePayment,advancePercentage){
    $(advancePayment).prop('checked',true);
    if($(source).prop('checked') === false){
        $(advancePercentage).prop('disabled',false).val('100');
    }else{
        $(advancePercentage).prop('disabled',false).val('50');
    }
}

function tentativeDateInput(source,tenure,refrence){
    if($(source).val()){
        $(tenure).val('').prop('disabled',true).prop('required',false);
        $(refrence).val('').prop('disabled',true).prop('required',false);

    }else{
        $(tenure).prop('disabled',false).prop('required',true);
        $(refrence).prop('disabled',false).prop('required',true);
    }
}

function tenureInput(source,tentativeDate){
    if($(source).val()){
        $(tentativeDate).prop('disabled',true).prop('required',false);
    }else{
        $(tentativeDate).prop('disabled',false).prop('required',true);
    }
}

function refrenceInput(source,tentativeDate){
    if($(source).val()){
        $(tentativeDate).prop('disabled',true).prop('required',false);
    }else{
        $(tentativeDate).prop('disabled',false).prop('required',true);
    }
}
