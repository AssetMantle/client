function validateForm(form) {

    let formValidationBoolean = true;

    form.find("dl").each(function () {
            const dlElement = $(this);
            const inputElement = dlElement.find("input")[0];
            const inputValue = inputElement.value;
            inputElement.style.borderColor = "transparent";
            try {
                dlElement.find(".error").remove();
            } catch {
            }

            let errorStatement = "";
            if (inputElement.type === "date" || inputElement.type === "checkbox" || (inputElement.getAttribute("required")==="false" && inputValue==="")) {
                return;
            }

            dlElement.find(".info").each(function () {

                    if (errorStatement !== "") {
                        return;
                    }
                    const ddInfoElement = $(this)[0];
                    const ddValidationInfo = ddInfoElement.innerHTML.split(": ");

                    switch (ddValidationInfo[0]) {
                        case "Numeric":
                            if (inputValue === "" || isNaN(inputValue)) {
                                errorStatement = "Numeric Value Expected";

                            }
                            break;
                        case "Minimum value":
                            if (inputValue < parseInt(ddValidationInfo[1].replace(/,/g, ""))) {
                                if (inputValue === "" || isNaN(inputValue)) {
                                    errorStatement = "Numeric Value Expected";
                                } else {
                                    errorStatement = "Must be greater than or equal to " + ddValidationInfo[1];
                                }
                            }
                            break;
                        case "Maximum value":
                            if (inputValue > parseInt(ddValidationInfo[1].replace(/,/g, ""))) {
                                if (inputValue === "" || isNaN(inputValue)) {
                                    errorStatement = "Numeric Value Expected";
                                } else {
                                    errorStatement = "Must be less than or equal to " + ddValidationInfo[1];
                                }
                            }
                            break;
                        case "Minimum length":
                            if (inputValue.length < parseInt(ddValidationInfo[1].replace(/,/g, ""))) {
                                errorStatement = "Minimum length is " + ddValidationInfo[1];

                            }
                            break;
                        case "Maximum length":
                            if (inputValue.length > parseInt(ddValidationInfo[1].replace(/,/g, ""))) {
                                errorStatement = "Maximum length is " + ddValidationInfo[1];

                            }
                            break;
                        default :
                            const newRegEx = new RegExp(ddInfoElement.innerHTML);
                            if (!(newRegEx.test(inputValue))) {
                                errorStatement = "Invalid Input";
                            }
                    }
                }
            );
            if (errorStatement !== "") {
                formValidationBoolean = false;
                inputElement.style.borderColor = "red";
                dlElement.append("<dd class=\"error\">" + errorStatement + "</dd>")
            }
        }
    );
    return formValidationBoolean;
}