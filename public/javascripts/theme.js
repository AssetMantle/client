// Theme colors used by the chart helpers. Resolved lazily via getComputedStyle
// so the values follow whichever theme (lightMode / darkMode) is currently on
// <body> at the moment a chart is drawn. See public/javascripts/chart/*.js.
const theme_colors = new Proxy({}, {
    get(_, key) {
        const css = getComputedStyle(document.documentElement);
        switch (key) {
            case "primary":
                return (css.getPropertyValue("--primary-color") || "#FFC640").trim();
            case "line":
                // Soft warm grey reads cleanly on both cream and dark surfaces;
                // pieChart uses this for the secondary wedge ("Others", etc.).
                return (css.getPropertyValue("--gray") || "#9c9a9a").trim();
            case "disabled":
                return (css.getPropertyValue("--disabled") || "#5F5D5A").trim();
        }
    }
});

const horizontal_stacked_chart_theme_colors = {
    block1 : "#FFB400",
    block2 : "#006BAF",
    block3 : "#D7E9BD",
    block4 : "#FFBFC2",
    block5 : "#55B9A9",
    block6 : "#D5DB48",
    block7 : "#FF3F00",
    block8 : "#007C7B",
    block9 : "#542157",
};
