function switchTab(switcherHeader,index, route) {
    let switcherContentID= 'switcherContent'+switcherHeader;
    let switcherHeaderID= 'switcherHeader'+switcherHeader;
    let switcherUlELement=$('#'+switcherHeaderID).closest("ul");
    cmukUIkit.switcher(switcherUlELement).show(index);
    componentResource(switcherContentID, route);
}