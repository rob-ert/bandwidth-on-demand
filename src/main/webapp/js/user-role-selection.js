$(function() {

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

})
