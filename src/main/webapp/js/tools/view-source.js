jQuery.fn.outerHTML = function(s) {
    return (s) ? $(this).replaceWith(s) : $(this).clone().wrap('div').parent().html();
}

$(document.body).append('<div id="source-code"><a id="x" href="#">x</a></div>')

$('section, .row').on('click', function(event) {
    console.log($(event.target).parent()[0].outerHTML);
    window.location.hash = 'source-code';
    var code = $(event.target).parent()[0].outerHTML.replace(/[<>]/g,
            function (m) {
                return{'<':'&lt;', '>':'&gt;'}[m]
            }).replace(/((ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?)/gi, '<a href="$1">$1</a>');
    $('#source-code pre').remove();
    $("<pre />", {"html":code, "class":"prettyprint"}).appendTo("#source-code");
    prettyPrint()

});
