
var viewmodels = viewmodels || {};

viewmodels.jobViewModel = function (data) {
    this.diagramId = data.diagramId;
    this.mode = data.mode;
    this.diagramName = data.diagramName;
    this.started = data.started;
    this.lastValue = -1;
    this.trend = [];
    this.killing = false;
};

viewmodels.streamingViewModel = function (data) {


};