function searchFunction( ){
    var queryData = {};
    var searchData = document.getElementById("search_value").value;
    var heightPattern = /^[0-9]*$/;
    var txHashPattern = /^[A-F0-9]{40}$/;
    var height= heightPattern.exec(searchData);
    if (height!=null){
        window.location= blockHeightURL + height;
    }
    var txHash = txHashPattern.exec(searchData);
    if (txHash != null){
        window.location= txHashPage + txHash;
    }
    document.getElementById("search_result").innerHTML= JSON.stringify(queryData);
}