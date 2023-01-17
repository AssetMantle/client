function shortAddress(id, address){
    var shortText = jQuery.trim(address).substring(0, 11)+ "..." + jQuery.trim(address).substring(address.length - 11);
    $("#" + id).text(shortText);
}