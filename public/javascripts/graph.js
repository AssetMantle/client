function graph(title, chartID) {
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Block Time',
                data: []
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });
    ctx.data(chartID, chart);
}

function updateGraph(chartID, label, data) {
    let chart = $('#' + chartID).data(chartID);
    if (chart.data.datasets[0].data.length > 10) {
        chart.data.labels.splice(0, 1);
        chart.data.datasets[0].data.splice(0, 1);
    }
    for (let i = 0; i < data.length; i++) {
        chart.data.labels.push(label[i]);
        chart.data.datasets[0].data.push(data[i]);
    }
    chart.update();
}

// https://www.chartjs.org/docs/latest/