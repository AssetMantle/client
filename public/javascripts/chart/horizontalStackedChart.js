function horizontalStackedChart(id, keys, values){
    let nameList = keys.replace('List(', '').slice(0, -1).split(', ');
    let valueList = values.replace('Iterable(', '').replace(')', '').split(', ');
    let totalValue = parseFloat(valueList[0]);
    let colors = [horizontal_stacked_chart_theme_colors.block1, horizontal_stacked_chart_theme_colors.block2, horizontal_stacked_chart_theme_colors.block3, horizontal_stacked_chart_theme_colors.block4, horizontal_stacked_chart_theme_colors.block5, horizontal_stacked_chart_theme_colors.block6, horizontal_stacked_chart_theme_colors.block7, horizontal_stacked_chart_theme_colors.block8, horizontal_stacked_chart_theme_colors.block9];

    for (let i = 1; i < valueList.length; i++) {
        totalValue = totalValue + parseFloat(valueList[i]);
    }

    valueList.slice(0, 9).forEach((item,index) => {
        $("#" + id + " .chart").append(`
        <span class="block" title="${nameList[index]} - ${(item * 100.0 / totalValue).toFixed(2) + "%"}" style="width: ${(item * 100.0 / totalValue).toFixed(2) + "%"}; background-color: ${colors[index]};"></span>`);
    });

    let legendColumn = 1;
    nameList.slice(0, 9).forEach((item,index) => {
        $("#" + id + " .legend").append(`
        <div class="legendItem section${legendColumn}">
            <span class="legendColor" style="background-color: ${colors[index]};"></span>
            <span class="legendLabel">${item}</span>    
        </div>`);
        if((index+1) % 3 === 0){
            legendColumn+=1;
        }
    });
}