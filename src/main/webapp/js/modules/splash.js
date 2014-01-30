var app = app || {};

app.splash = function() {
    var setSkip = function() {
        $(".splash input.noMore").click(function() {
            var cookieName = "skipSplash";
            if($(this).is(':checked')) {
                document.cookie =  cookieName +"=1;expires=Tue, 01 Jan 2030 00:00:01 UTC;path=/";
            } else{
                document.cookie = cookieName + '=;expires=Thu, 01 Jan 1970 00:00:01 UTC;path=/'; // deletes the cookie
            }
        });
    };
    return {
        init: setSkip
    };
}();

app.register(app.splash);

