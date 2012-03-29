$(function() {
	$.fn.twipsy.defaults.delayIn = 500;

	$('[rel="twipsy"]').twipsy();

	$('th.sortable').each(function(i, item) {
	    var $header = $(item),
	        $link = $header.find('a'),
	        url = $link.attr('href');

	    $link.replaceWith($link.html());

	    $header.click(function() {
	        document.location = url;
	    });
	});
});

function showInfoMessage(message) {
    var closeLink = $("<a/>", {class: "close", href: "#", html: "&times;"});
    var record = $("<div/>", {html: message, class: "alert-message fade in"}).append(closeLink).alert();

    $("#messages").prepend(record);
}