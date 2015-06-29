'use strict';

analyticsApp.factory('colorService', function () {

    var colors = {
        default: ['#1f77b4','#ff7f0e','#2ca02c','#d62728','#9467bd','#8c564b','#e377c2','#bcbd22','#17becf','#aec7e8','#ffbb78','#98df8a','#ff9896','#c5b0d5','#c49c94','#f7b6d2','#dbdb8d','#9edae5']
    };

    return {

        getKeys: function(){

            var keys = [];
            for(var key in colors) keys.push(key);

            return keys;
        },

        getColor: function(setName, index){

            var scale = colors[setName];
            return scale[index % scale.length];
        },

        getSet: function(setName){

            return colors[setName];
        }
    };
});
