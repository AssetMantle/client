function updateLastBlock(height, updatedTime){
    let newTime = new Date(updatedTime);
    $('#lastBlockHeight').html(""+ height+" At  "+ newTime.getHours() + ":" + newTime.getMinutes() + ":" + newTime.getSeconds());
}