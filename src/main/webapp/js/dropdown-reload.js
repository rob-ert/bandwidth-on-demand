(function($){
	$.fn.dropdownReload = function(dataUrl, otherSelect) {
		var inputSelect = this;
		
		inputSelect.change(function() {
			$.get(dataUrl.replace("{}", inputSelect.val()), function(responseData) {
				var options = $.map(responseData, function(object, i) {
					return '<option value="'+object.id+'">'+object.name+'</option>';
				});
				if ($.isArray(otherSelect)) {
					$.each(otherSelect, function(i, select) {
						$(select).html(options.join(""));
						selectedIndex = options.length >= i ? i :options.length;
						$(select).prop('selectedIndex', selectedIndex);
					});
				} else {
					$(otherSelect).html(options.join(""));
				}
			});
		});
	};
})(jQuery);