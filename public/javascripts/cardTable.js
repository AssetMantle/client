$(document).click(function (e) {
    $('.editbutton').each(function () {
        const elementcontent = $(this);
        if (elementcontent.is(e.target)) {
            cardTable(elementcontent);
        }
    });
});

function cardTable(element) {
    element.parent().find('#Edit').toggle();
    var $table = element.parent().parent().siblings().find("table");
    element.parent().find('#tableView').click(function() {
        var $cardTable = $table;
        var invertedTable = [];
        for(var i=0 ; i < $cardTable.find('tr:first th').length ; i++){
            invertedTable.push([]);
        }
        $cardTable.find('th,td').each(function(){
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
        for(var i=0 ; i < invertedTable.length ; i++){
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
    element.parent().find('#listView').click(function() {
        var $tableCard = $table;
        var ul = $("<ul class=mainlist>");
        $tableCard.find('tr').each(function(){
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
        $(this).closest(".cmuk-card-header").siblings().find('table').replaceWith(ul);
    });

}