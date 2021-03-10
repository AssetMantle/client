$(document).ready(function () {
    $('#vesselCheckForm').hide();
    $('#organizationCheck').hide();
    $('#nextButton').hide();
    $('#comparisonData').hide();
    $('#completeCheckData').hide();
    $('#searchResult').hide();
});

function showComparisonData() {
    $('#requestingCheck').hide();
    $('#comparisonData').show();
    $('#completeCheckData').hide();
    $('#searchResult').hide();
}

function showCompleteData(){
    $('#requestingCheck').hide();
    $('#comparisonData').hide();
    $('#completeCheckData').show();
    $('#searchResult').show();

}
function showSearchResult(){
    $('#requestingCheck').hide();
    $('#comparisonData').hide();
    $('#completeCheckData').hide();
    $('#searchButton').hide();
    $('#searchResult').show();

}

