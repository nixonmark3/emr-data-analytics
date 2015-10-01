'use strict';

angular.module('emr.ui.files', [])

    .directive('fileDrop', ['diagramService', function(diagramService) {

        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {

                // add event listeners

                // listen for drag over event, set dragging-file class
                element[0].addEventListener('dragover', function (evt) {
                    element.addClass("dragging-file");
                    evt.preventDefault();
                    evt.stopPropagation();
                }, false);

                // listen for drag enter event
                element[0].addEventListener('dragenter', function (evt) {
                    evt.preventDefault();
                    evt.stopPropagation();
                }, false);

                // listen for drag leave event, remove dragging-file class
                element[0].addEventListener('dragleave', function () {
                    element.removeClass("dragging-file");
                }, false);

                // listen for drop event, remove dragging-file class, update file
                element[0].addEventListener('drop', function (evt) {

                    element.removeClass("dragging-file");
                    var files = evt.dataTransfer.files;

                    if ($scope.onFileDropStart)
                        $scope.onFileDropStart(files);

                    for(var index = 0; index < files.length; index++){

                        var file = files[index];
                        diagramService.upload(file).then(
                            function(result){   // on success

                                if ($scope.onFileDrop)
                                    $scope.onFileDrop(result, evt);
                            },
                            function(message){     // on error

                                if($scope.onFileDropError)
                                    $scope.onFileDropError(message);
                            },
                            function(evt){      // on notification

                                if (evt.lengthComputable) {

                                    var complete = (evt.loaded / evt.total * 100 | 0);

                                    if($scope.onFileDropNotification)
                                        $scope.onFileDropNotification(file, complete);
                                }
                            }
                        );
                    }

                    evt.preventDefault();
                    evt.stopPropagation();

                }, false);
            }
        }
    }]);
