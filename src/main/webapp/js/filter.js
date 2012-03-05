//Sets the filter selectbox to the rest id in the url
$(document).ready(function(){   	
       	var startPos = window.location.href.lastIndexOf("/filter/");
       	var endPos = window.location.href.indexOf("/", startPos+8);
       	var selectedValue = window.location.href.substring(startPos+8, endPos);
       	
        $("#_filter").val(selectedValue);        
});
