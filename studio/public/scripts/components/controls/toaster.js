'use strict';

controlsApp

    .factory('toasterService', [function (){

        var index = 0;
        var toasts = [];

        var toast = {
            clear: clear,
            error: error,
            remove: remove
        };

        return toast;
    }]);