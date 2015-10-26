'use strict';

controlsApp

    .controller('toastController', ['$scope', '$timeout', 'type', 'title', 'message', 'duration', 'close',
        function($scope, $timeout, type, title, message, duration, close){

            var timer;

            // internal methods

            function init(){

                $scope.title = title;
                $scope.toastClass = "toast-" + type;

                switch(type) {
                    case "error":
                        $scope.toastIcon = "fa-times-circle";
                        break;
                    case "info":
                        $scope.toastIcon = "fa-info-circle";
                        break;
                    case "success":
                        $scope.toastIcon = "fa-check";
                        break;
                    case "warning":
                        $scope.toastIcon = "fa-exclamation-triangle";
                        break;
                }

                startTimer();
            }

            function startTimer(){
                timer = $timeout(function() { $scope.onClose(); }, duration);
            }

            // public methods

            $scope.onClose = close;

            $scope.onOver = function(evt){
                $timeout.cancel(timer);

                evt.stopPropagation();
                evt.preventDefault();
            };

            $scope.onLeave = function(evt){
                startTimer();

                evt.stopPropagation();
                evt.preventDefault();
            };

            init();
        }])

    .factory('toasterService', ['popupService', function (popupService){

        var toaster = {
            error: error,
            info: info,
            success: success,
            warning: warning
        };

        function error(title, message, parentId, duration){
            makeToast('error', title, message, parentId, duration);
        }

        function info(title, message, parentId, duration){
            makeToast('info', title, message, parentId, duration);
        }

        function success(title, message, parentId, duration){
            makeToast('success', title, message, parentId, duration);
        }

        function warning(title, message, parentId, duration){
            makeToast('warning', title, message, parentId, duration);
        }

        /**
         * Method that creates a toast instance
         * @param type
         * @param title
         * @param message
         * @param parentId
         * @param duration
         */
        function makeToast(type, title, message, parentId, duration){

            // verify that a duration has been provided
            if(duration === undefined || duration === null) duration = 5000;

            // build the toast popup properties
            var properties = {
                templateUrl: '/assets/scripts/components/controls/toast.html',
                controller: 'toastController',
                inputs: {
                    type: type,
                    title: title,
                    message: message,
                    duration: duration
                }
            };

            // if a parent element has been specified, reference as append element
            if (parentId)
                properties.appendElement = angular.element($('#' + parentId));

            popupService.show(properties)
                .then(function (popup) {


                }
            );
        }

        return toaster;
    }]);