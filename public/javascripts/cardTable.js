$(document).ready(function() {
    var $tabl = $('#tabledata');
    console.log("table length",$tabl.find('tr').length);
    $('#tableView').click(function() {
        var $tab = $tabl;
        var invertedTable = [];
        for(var i=0 ; i < $tab.find('tr:first th').length ; i++){
            invertedTable.push([]);
        }
        $tab.find('th,td').each(function(){
            invertedTable[$(this).index()].push($(this).text());
        });
        var $newTable = $('<table class="cmuk-table cmuk-table-divider"></table>');
        var $newTr = $('<tr></tr>');
        for(var i=0 ; i < invertedTable.length ; i++){
            for(var j = 0 ; j < invertedTable[i].length; j++){
                if(j == 0){
                    $newTr.append('<th>'+invertedTable[i][j]+'</th>');
                }
            }
            $newTable.append($newTr);
        }
        for(var i=0; i < invertedTable.length; i++){
            if(i < invertedTable[0].length ) {
                var $newTrd = $('<tr></tr>');
                for (var j = 0 ; j < invertedTable.length; j++) {
                    if (i != 0) {
                        $newTrd.append('<td>' + invertedTable[j][i] + '</td>');
                    }
                }
            }
            $newTable.append($newTrd);
        }
        $(".productTable ul").replaceWith($newTable);
    });

    $('#listView').click(function() {
        var $table = $tabl;
        console.log("table length",$table.find('tr').length);
        var ul = $("<ul class=mainlist>");
        $table.find('tr').each(function(){
            var li = $("<li class=listItem>");
            $("td", this).each(function(){
                var div = $("<div class=listItemchild>");
                var $this =$(this);
                var th = $("<a class=listItemchildhTitle>").html($this.closest('table').find('th').eq($this.index()).text());
                var p = $("<p>").html(this.innerHTML);
                div.append(th);
                div.append(p);
                li.append(div);
            });
            ul.append(li);
        })
        $(".productTable table").replaceWith(ul);
    });
});