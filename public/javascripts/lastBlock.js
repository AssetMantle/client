function lastBlockFunc(urlLastBlockHeight, height, updatedTime){
    let newTime = new Date(updatedTime);
    document.getElementById("last_block_height").innerHTML = "Height: "+height+" At Time: "+ newTime.getHours() + ":" + newTime.getMinutes() + ":" + newTime.getSeconds();
}