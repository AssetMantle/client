let heightPattern = /^[0-9]*$/;
let txHashPattern = /^[A-F0-9]{40}$/;

function searchFunction( ){
    let searchData = document.getElementById("searchValue").value;
    let height= heightPattern.exec(searchData);
    if (height != null){
        window.location= blockHeightURL + height;
    }
    let txHash = txHashPattern.exec(searchData);
    if (txHash != null){
        window.location= txHashPage + txHash;
    }
}