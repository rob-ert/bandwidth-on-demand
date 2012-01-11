$(function() {
  var groupInJson;
  $("#_virtualresourcegroup").dropdownReload(dataUrl, ["#_sourcePort", "#_destinationPort"], {
    afterReload: function(data) {
      groupInJson = data;
      setHalfBandwidth();
  	}	
  });
  $("#bandwidthQuater").click(function() {setQuaterBandwidth(); return false;});
  $("#bandwidthHalf").click(function() {setHalfBandwidth(); return false;});
  $("#bandwidthFull").click(function() {setFullBandwidth(); return false;});
  $("#_sourcePort,#_destinationPort").change(function(object) {
	setHalfBandwidth();
  });
  $("#_bandwidth_id").change(function() {
    bandwidthChanged();
  });
  function setFullBandwidth() {
  	resetBandwidthSelection();
  	setBandwidthDividedBy(1);
  	$("#bandwidthFull").children().addClass("active");
  }
  function setHalfBandwidth() {
  	resetBandwidthSelection();
  	setBandwidthDividedBy(2);
      $("#bandwidthHalf").children().addClass("active");
  }
  function setQuaterBandwidth() {
  	resetBandwidthSelection();
  	setBandwidthDividedBy(4);
  	$("#bandwidthQuater").children().addClass("active");
  }
  function resetBandwidthSelection() {
  	$("#bandwidthHalf").children().removeClass("active");
    $("#bandwidthQuater").children().removeClass("active");
    $("#bandwidthFull").children().removeClass("active");
  }
  function setBandwidthDividedBy(divider) {
    $("#_bandwidth_id").val(getMaxBandwidth() / divider);
  };
  function getMaxBandwidth() {
	sourceBandwidth = groupInJson[$("#_sourcePort").prop('selectedIndex')].maxBandwidth;
    destinationBandwidth = groupInJson[$("#_destinationPort").prop('selectedIndex')].maxBandwidth;
	return Math.min(sourceBandwidth, destinationBandwidth);
  }
  function bandwidthChanged() {
	resetBandwidthSelection();
	  
	bandwidth = $("#_bandwidth_id").val();
	if (getMaxBandwidth() == bandwidth) {
	  setFullBandwidth();
	}
	if (getMaxBandwidth() / 2 == bandwidth) {
      setHalfBandwidth();
    }
    if (getMaxBandwidth() / 4 == bandwidth) {
      setQuaterBandwidth();
    }
  }
  function initBandwidthSelection() {
	$.getJSON(dataUrl.replace("{}", $("#_virtualresourcegroup").val()), function(data) {
	    groupInJson = data;
	    bandwidthChanged();
	});
  }
  initBandwidthSelection();
  
  // date pickers
  $("#_startDate_id").DatePicker({
    format:'Y-m-d',
    date: $('#_startDate_id').val(),
    onBeforeShow: function(){
      $('#_startDate_id').DatePickerSetDate($('#_startDate_id').val(), true);
    },
    onChange: function(formated, dates){
      $('#_startDate_id').val(formated);
      $('#_startDate_id').DatePickerHide();
    }
  });
  $("#_endDate_id").DatePicker({
    format:'Y-m-d',
    date: $('#_endDate_id').val(),
    onBeforeShow: function(){
      $('#_endDate_id').DatePickerSetDate($('#_endDate_id').val(), true);
    },
    onChange: function(formated, dates){
      $('#_endDate_id').val(formated);
      $('#_endDate_id').DatePickerHide();
    }
  });
});