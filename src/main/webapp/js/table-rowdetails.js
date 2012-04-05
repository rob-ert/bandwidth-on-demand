$(function() {

    $('.table').on('click', '.rowdetails td:first-child', function(event) {

        var row = $(event.target).closest('.rowdetails');
        var details = row.find('.rowdetails-content');

        var rowHeight = row.closest('table').find('tr').height();

        if(!row.hasClass('expanded')) {

            details[0]._originalHeight = details[0]._originalHeight || details.height();
            details.height(0);

            row.animate({
                height: rowHeight + details[0]._originalHeight + 20
            })

            details.animate({
                height: details[0]._originalHeight,
                opacity: 1
            })
        } else {

            row.animate({
                height: rowHeight
            })

            details.animate({
                height: 0,
                opacity: 0
            })
        }

        row.toggleClass('expanded');
    });

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

})
