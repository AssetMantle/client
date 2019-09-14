
function validateForm(source) {
       let formValidationBoolean = true;

        const form = $(source).closest("form");

        let inputElements = form.find("input");

        for (let i = 1; i < inputElements.length; i++) {

            $("#" + inputElements[i].id).css('border-color', 'transparent');
            let errorElements = $("#" + inputElements[i].id + '_field').find(".error");

            try {
                errorElements[0].remove();
            } catch {
            }

            if ($("#" + inputElements[i].id).attr('type') === 'checkbox' || $("#" + inputElements[i].id).attr('type') === 'date') {
                continue
            }
            let elementValidationBool=validateElement(inputElements[i].id);
            formValidationBoolean=(formValidationBoolean==false)? false: elementValidationBool
        }

        if (formValidationBoolean === true) {
            submitForm(source);
        }



}

function validateElement(id) {


    let ddElement = $("#" + id + '_field').find(".info");

    let noOfInfoElements = ddElement.length;

    let inputValue = $("#" + id)[0].value;

    if (ddElement[noOfInfoElements - 1].innerHTML === 'Numeric') {
        if (isNaN(inputValue) || inputValue === "") {
            $("#" + id).css('border-color', 'red');
            $("#" + id + '_field').append("<dd class=\"error\">Numeric Value Expected</dd>");
            
            return false
        }

        if (noOfInfoElements == 2) {
            let firstInfoElementArray = ddElement[0].innerHTML.split(" ");
            if (firstInfoElementArray[0] === "Minimum") {
                if (inputValue < parseInt(firstInfoElementArray[2].replace(/,/g, ""))) {
                    $("#" + id).css('border-color', 'red');
                    $("#" + id + '_field').append("<dd class=\"error\">Must be greater than or equal to " + ddElement[0].innerHTML.split(" ")[2] + "</dd>");
                    return false
                }
            } else if (firstInfoElementArray[0] === "Maximum") {
                if (inputValue > parseInt(firstInfoElementArray[2].replace(/,/g, ""))) {
                    $("#" + id).css('border-color', 'red');
                    $("#" + id + '_field').append("<dd class=\"error\">Must be less than or equal to " + ddElement[0].innerHTML.split(" ")[2] + "</dd>");
                    
                    return false
                }

            }
            return true
        } else if (noOfInfoElements == 3) {
            let firstInfoElementArray = ddElement[0].innerHTML.split(" ");
            let secondInfoElementArray = ddElement[1].innerHTML.split(" ");

            if (firstInfoElementArray[0] === "Minimum") {
                if (inputValue < parseInt(firstInfoElementArray[2].replace(/,/g, ""))) {
                    $("#" + id).css('border-color', 'red');
                    $("#" + id + '_field').append("<dd class=\"error\">Must be greater than or equal to " + firstInfoElementArray[2] + "</dd>");
                    
                    return false
                }
            }
            if (secondInfoElementArray[0] === "Maximum") {

                if (inputValue > parseInt(secondInfoElementArray[2].replace(/,/g, ""))) {
                    $("#" + id).css('border-color', 'red');
                    $("#" + id + '_field').append("<dd class=\"error\">Must be less than or equal to " + secondInfoElementArray[2] + "</dd>");
                    
                    return false
                }
            }
            return true
        }


    } else {
        let regEx = ddElement[noOfInfoElements - 1].innerHTML;
        let newRegEx = new RegExp(regEx);
        if (newRegEx.test(document.getElementById(id).value)) {

            if (noOfInfoElements === 2) {
                let firstInfoElementArray = ddElement[0].innerHTML.split(" ");
                if (firstInfoElementArray[0] === "Minimum") {
                    if (inputValue.length < parseInt(firstInfoElementArray[2])) {
                        $("#" + id).css('border-color', 'red');
                        $("#" + id + '_field').append("<dd class=\"error\">Minimum length is " + firstInfoElementArray[2] + "</dd>");

                        return false
                    }
                } else if (firstInfoElementArray[0] === "Maximum") {
                    if (inputValue.length > parseInt(firstInfoElementArray[2])) {
                        $("#" + id).css('border-color', 'red');
                        $("#" + id + '_field').append("<dd class=\"error\">Maximum length is " + firstInfoElementArray[2] + "</dd>");
                        

                        return false
                    }
                }
                return true
            } else if (noOfInfoElements === 3) {
                let firstInfoElementArray = ddElement[0].innerHTML.split(" ");
                let secondInfoElementArray = ddElement[1].innerHTML.split(" ");

                if (firstInfoElementArray[0] === "Minimum") {
                    if (inputValue.length < parseInt(firstInfoElementArray[2])) {
                        $("#" + id).css('border-color', 'red');
                        $("#" + id + '_field').append("<dd class=\"error\">Minimum length is " + firstInfoElementArray[2] + "</dd>");
                        
                        return false
                    }
                }
                if (secondInfoElementArray[0] === "Maximum") {
                    if (inputValue.length > parseInt(secondInfoElementArray[2])) {
                        $("#" + id).css('border-color', 'red');
                        $("#" + id + '_field').append("<dd class=\"error\">Maximum length is " + secondInfoElementArray[2] + "</dd>");
                        
                        return false
                    }
                }
                return true
            }

        } else {
            $("#" + id).css('border-color', 'red');
            $("#" + id + '_field').append("<dd class=\"error\">Error Response </dd>");
            
            return false
        }

    }
}

