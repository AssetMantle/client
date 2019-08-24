function updateLastBlock(urlLastBlockHeight, height, updatedTime){
    let newTime = new Date(updatedTime);
    document.getElementById("lastBlockHeight").innerHTML = ""+height+" At  "+ newTime.getHours() + ":" + newTime.getMinutes() + ":" + newTime.getSeconds();
}