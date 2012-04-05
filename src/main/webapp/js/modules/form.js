var app = app || {};

app.form = function(){

    var init = function() {

        initEventHandlers();

    };

    var initEventHandlers = function() {

        initUserSelection();
        initFormLinks();

    };

    var initUserSelection = function() {

        var form = $('.dropdown-menu');

        form.on('click', 'li', function(event) {

            var item = $(event.target).closest('li')[0];
            var roleId = item.getAttribute('data-roleId');

            $('<input>').attr({
                type: 'hidden',
                name: 'roleId',
                value: roleId
            }).appendTo(form);

            form[0].submit();

        })

    };

    var initFormLinks = function() {

        $('a[data-form]').on('click', function(event) {

            var errorMessage = 'Sorry, action failed.';

            var post = function(url, data) {
                $.post(url, data)
                .success(function() {
                    window.location.reload(true);
                })
                .error(function() {
                    alert(errorMessage);
                });
            };

            var element = $(event.target).closest('a')[0];
            var href = element.href;
            var data = href.replace(/[^\?]*\?/, ''); // Everything after '?'
            var url = href.replace(/\?.*/, ''); // Everything before '?'

            var isToBeConfirmed = element.getAttribute('data-confirm');

            if(isToBeConfirmed) {
                var isConfirmed = confirm(isToBeConfirmed);
                if(isConfirmed) {
                    post(url, data);
                }
            } else {
                post(url, data);
            }

            event.preventDefault();

        })

    };

    return {
        init: init
    }

}();

app.register(app.form);
