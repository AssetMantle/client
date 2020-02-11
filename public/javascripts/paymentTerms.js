/*let advancePayment=$("#ADVANCE_PAYMENT");
let advancePercentage=$("#ADVANCE_PERCENTAGE");
let credit=$("#CREDIT");
let tenure=$("#TENURE");
let tentativeDate=$("#TENTATIVE_DATE");
let refrence=$("#REFRENCE");*/

function advancePaymentInput(source,advancePercentage,credit){
    if($(source).prop('checked') === false){
        $(credit).prop('checked',true);
        $(advancePercentage).val('').prop('disabled',true).prop('required',false);
        //$(advancePercentage).prop('disabled',true);
    }else{
        $(advancePercentage).prop('disabled',false).prop('required',true).val('0');
    }
}

function advancePercentageInput(source,credit){
    console.log("PercentageInput");
    if($(source).val() === '100'){
        $(credit).prop('checked',false);
    }else{
        $(credit).prop('checked',true);
    }
}

function creditInput(source,advancePayment,advancePercentage){
    console.log("creditInput");
    if($(source).prop('checked') === false){
        $(advancePercentage).prop('disabled',false).val('100');
        $(advancePayment).prop('checked',true);
    }else{
        $(advancePercentage).prop('disabled',false).val('50');
        $(advancePayment).prop('checked',true);
    }
}
function tentativeDateInput(source,tenure,refrence){
    if($(source).val()){
        console.log("here");
        $(tenure).val('');
        $(tenure).prop('disabled',true);
        $(refrence).val('');
        $(refrence).prop('disabled',true);

    }else{
        console.log("here2");
        $(tenure).prop('disabled',false);
        $(refrence).prop('disabled',false);
    }
}
function tenureInput(source,tentativeDate){

    if($(source).val()){
        console.log("here3");
        $(tentativeDate).prop('disabled',true);
    }else{
        console.log("here4");
        $(tentativeDate).prop('disabled',false);
    }
}
function refrenceInput(source,tentativeDate){
    if($(source).val()){
        console.log("here3");
        $(tentativeDate).prop('disabled',true);
    }else{
        console.log("here4");
        $(tentativeDate).prop('disabled',false);
    }
}

/*

$("#ADVANCE_PAYMENT").on('input',function () {
    if($(this).prop('checked') === false){
        credit.prop('checked',true);
        advancePercentage.val('');
        advancePercentage.prop('disabled',true);
    }else{
        advancePercentage.prop('disabled',false);
       if(advancePercentage.val() === '100'){
           credit.prop('checked',false);
       }else{
           credit.prop('checked',true);
       }
    }
});

advancePercentage.on('input',function () {
    console.log($(this).val());
    if($(this).val() === '100'){
        console.log("works");
        credit.prop('checked',false);

    }else{
        credit.prop('checked',true);
    }
});

credit.on('input',function () {
    if($(this).prop('checked') === false){
        advancePercentage.val('100');
        advancePayment.prop('checked',true);

    }else{
        advancePercentage.val('50');
        advancePayment.prop('checked',true);
    }

});

tentativeDate.on('input',function () {
   if($(this).val()){
       console.log("here");
       tenure.val('');
       tenure.prop('disabled',true);
       refrence.val('');
       refrence.prop('disabled',true);

   }else{
       console.log("here2");
       tenure.prop('disabled',false);
       refrence.prop('disabled',false);
   }

});

tenure.on('input',function () {
    if($(this).val()){
        console.log("here3");
        tentativeDate.prop('disabled',true);
    }else{
        console.log("here4");
        tentativeDate.prop('disabled',false);
    }

});
*/
