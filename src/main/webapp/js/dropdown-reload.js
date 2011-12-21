(function($){
	$.fn.dropdownReload = function(dataUrl, otherSelect) {
		var inputSelect = this;
		
		inputSelect.change(function() {
			$.get(dataUrl.replace("{}", inputSelect.val()), function(responseData) {
				var options = $.map(responseData, function(object) {
					return '<option value="'+object.id+'">'+object.name+'</option>';
				}).join("");
				if ($.isArray(otherSelect)) {
					$.each(otherSelect, function(i, select) {
						$(select).html(options);
					});
				} else {
					$(otherSelect).html(options);
				}
			})
		});
	}
})(jQuery);