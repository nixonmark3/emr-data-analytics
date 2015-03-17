'use strict';

var navigationApp = angular.module('navigationApp', ['ngAnimate'])
    .controller('navigationController', ['$scope', function($scope){

        // initialize the pages
        $scope.pages = [{
            "categories" : $scope.navigation,
            "definitions" : [],
            "definition" : {}
        }];

        // initialize the page index
        $scope.pageIndex = 0;

        // set page index
        $scope.setPage = function(index){
            $scope.pageIndex = index;
        };

        // identify whether index is current page
        $scope.isCurrentPage = function(index){
            return $scope.pageIndex === index;
        };

        // page the navigation panel
        $scope.pageForward = function(item){

            var index = $scope.pageIndex + 1;
            $scope.pages.push({
                    "categories" : item.categories,
                    "definitions" : item.definitions,
                    "definition" : {}
                }
            );

            $scope.pageIndex = index;
        };

    }]);