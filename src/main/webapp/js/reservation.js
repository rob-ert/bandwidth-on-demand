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

  var $startTime = $("#_startTime_id"),
      $startDate = $("#_startDate_id"),
      startTimeVal = $startTime.val(),
      startDateVal = $startDate.val();

  $("#now_chk").click(function() {
    $startTime.closest("div.control-group").toggle('slow');
    if ($startDate.attr('disabled')) {
      $startDate.val(startDateVal);
      $startTime.val(startTimeVal);
      $startDate.removeAttr('disabled');
      $startTime.removeAttr('disabled');
    } else {
      $startDate.val("");
      $startTime.val("");
      $startDate.attr('disabled', 'true');
      $startTime.attr('disabled', 'true');
    }
  });

  // date pickers
  $("#_startDate_id").DatePicker({
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