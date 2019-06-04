function hideUnhide(source) {
   let a = $(source).next("div");
   a.toggle("slow");
}

// function hideUnhide(source) {
//     let x = $(source).next( ".flexContainer")[0];
//     if (x.style.display === "none") {
//         x.style.display = "flex";
//     } else {
//         x.style.display = "none";
//     }
// }