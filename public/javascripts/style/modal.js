$(document).mouseup(function (e) {
    $('.modal').each(function () {
        const modal = $(this);
        if (modal.is(e.target)) {
            modal.fadeOut(400);
        }
    })
});

function openNav() {
    document.getElementById("sideBar").style.width = "200px";
}

function closeNav() {
    document.getElementById("sideBar").style.width = "0px";
}

function sideBarSearchFunction() {
    var input, filter, ul, li, a, i;
    input = document.getElementById("sideBarSearch");
    filter = input.value.toUpperCase();
    ul = document.getElementById("sideBarSearchMenu");
    li = ul.getElementsByTagName("li");
    for (i = 0; i < li.length; i++) {
        a = li[i].getElementsByTagName("a")[0];
        if (a.innerHTML.toUpperCase().indexOf(filter) > -1) {
            li[i].style.display = "";
        } else {
            li[i].style.display = "none";
        }
    }
}