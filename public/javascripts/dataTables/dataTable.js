 $(document).ready(function() {
        var table = $(".tableSort").DataTable(
            {
                retrieve: true,
                "bLengthChange": false,
                "paging":   false,
                "info":     false,
                "oLanguage": { "sSearch": "search"
                }
            }
        );
     var tables = $(".tableSortPagination").DataTable(
         {
             retrieve: true,
             "pageLength": 2,
                 "oLanguage": { "sSearch": "search"
             }
         }
     );
    } );


