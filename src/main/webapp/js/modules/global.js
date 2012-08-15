var app = app || {};

app.global = function() {

    var init = function() {

        initEventHandlers();
        initPlugins();
    };

    var initEventHandlers = function() {

        initUserSelection();
        initTextSearch();
    };

    var initPlugins = function() {

        initCsrfFilter();
        initTooltips();
        initPopovers();
    };

    var initCsrfFilter = function() {
        var token = $("#csrf-token").text();

        $.ajaxPrefilter(function(options, originalOptions, jqXHR) {
            options.data = options.data + "&csrf-token=" + token;
        });
    };

    var initUserSelection = function() {
        var form = $('form.dropdown-menu');

        form.on('click', 'li', function(event) {

            var item = $(event.target).closest('li')[0],
                roleId = item.getAttribute('data-roleId');

            $('<input>').attr({
                type: 'hidden',
                name: 'roleId',
                value: roleId
            }).appendTo(form);

            form[0].submit();
        });
    };

        
    var initTextSearch = function () {
       var searchButton = $('#sb_id');
       var searchInput = $('#si_id');
       var currentUrl = document.URL;
       var searchPart = "/search?search=";
       
       searchButton.on('click', function(event) {
    	   if (currentUrl.indexOf('search') == -1){
    	     window.location.href=(currentUrl + searchPart + searchInput.val());
    	   } 
    	   else {
    	     window.location.href = currentUrl.substring(0, currentUrl.lastIndexOf('/search?')) + searchPart + searchInput.val();
    	   }
       });
    };

    var _placement = function(popup, element) {
        popup.setAttribute('data-type', element.getAttribute('data-type'));
        return 'top';
    };

    var initTooltips = function() {

        $('[rel="tooltip"]').tooltip({
            placement: _placement
        });

    };

    var initPopovers = function() {

        $('[rel="popover"]').popover({
            placement: _placement
        });

    };

    return {
        init: init
    };

}();

app.register(app.global);
