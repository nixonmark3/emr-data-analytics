'use strict';

var navigationApp = angular.module('navigationApp', ['ngAnimate', 'draggableApp'])
    .controller('navigationController', ['$scope', '$rootScope', function($scope, $rootScope){

        var beginDragEvent = function(x, y, config){

            $scope.$root.$broadcast("beginDrag", {
                x: x,
                y: y,
                config: config
            });
        };

        // initialize the pages
        $scope.pages = [{
            "categories" : $scope.navigation,
            "definitions" : [],
            "definition" : null
        }];

        // initialize the page index
        $scope.pageIndex = 0;

        $scope.pageHeader = [];

        // set page index
        $scope.setPage = function(index){
            $scope.pageIndex = index;
        };

        // identify whether index is current page
        $scope.isCurrentPage = function(index){
            return $scope.pageIndex === index;
        };

        // page the navigation panel back
        $scope.pageBack = function(){

            $scope.pages.splice($scope.pageIndex);
            $scope.pageIndex = $scope.pageIndex - 1;
            $scope.pageHeader.splice($scope.pageIndex);
        };

        // page the navigation panel forward
        $scope.pageForward = function(item){

            $scope.pageHeader.push(item.name);

            var index = $scope.pageIndex + 1;
            $scope.pages.push({
                    "categories" : item.categories,
                    "definitions" : item.definitions,
                    "definition" : null
                }
            );

            $scope.pageIndex = index;
        };

        // display a definition for configuration
        $scope.show = function(item){

            $scope.pageHeader.push(item.name);

            var index = $scope.pageIndex + 1;
            $scope.pages.push({
                    "categories" : [],
                    "definitions" : [],
                    "definition" : item
                }
            );

            $scope.pageIndex = index;
        };

        $scope.mouseDown = function(evt, item){

            beginDragEvent(evt.pageX, evt.pageY, {

                dragStarted: function (x, y) {

                },

                dragging: function (x, y) {

                },

                dragEnded: function () {

                    alert(JSON.stringify(item));
                }

            });

            evt.stopPropagation();
            evt.preventDefault();
        };

    }]);