'use strict';

angular.module('emr.ui.shared', [])
    .factory('sharedService', ['$http', '$q', '$templateCache', function($http, $q, $templateCache){

        return {

            getTemplate: function(template, templateUrl){

                var deferred = $q.defer();

                // first - check whether template text was specified
                if(template){

                    deferred.resolve(template);
                }
                else if(templateUrl) {

                    // check to see if the template has already been loaded
                    var cachedTemplate = $templateCache.get(templateUrl);
                    if(cachedTemplate !== undefined) {
                        deferred.resolve(cachedTemplate);
                    }
                    // if not, grab the template for the first time
                    else {

                        $http({method: 'GET', url: templateUrl, cache: true})
                            .then(function(result) {

                                // save template into the cache and return the template
                                $templateCache.put(templateUrl, result.data);
                                deferred.resolve(result.data);
                            },
                            function(error) {
                                deferred.reject(error);
                            });
                    }
                } else {
                    deferred.reject("A template has not been specified.");
                }

                return deferred.promise;
            }
        }

    }]);