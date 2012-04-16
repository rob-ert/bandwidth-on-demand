$(function() {
  $("#_virtualresourcegroup").dropdownReload(
    dataUrl,
    ["#_sourcePort", "#_destinationPort"],
    {
      afterReload: function(data) {
        groupInJson = data;
        setHalfBandwidth();
      },
      displayProp: "userLabel"
    }
  );
});