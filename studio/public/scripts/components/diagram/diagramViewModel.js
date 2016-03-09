
var viewmodels = viewmodels || {};

viewmodels.connectorViewModel = function (data, x, y, parent) {

    //
    // private properties
    //
    this.data = data;
    this._parent = parent;
    this._x = x;
    this._y = y;
    this.visible = data.visible;
    this._normalRadius = 6;
    this._expandedRadius = 12;

    //
    // public properties
    //
    this.radius = this._normalRadius;
    this.showName = false;

    //
    // Set the connector radius to normal.
    //
    this.normalRadius = function () {
        this.radius = this._normalRadius;
    };

    //
    // Set the connector radius to expanded.
    //
    this.expandRadius = function () {
        this.radius = this._expandedRadius;
    };

    //
    // The name of the connector.
    //
    this.name = function () {
        return this.data.name;
    };

    //
    // X coordinate of the connector.
    //
    this.x = function () {
        return this._x;
    };

    //
    // Y coordinate of the connector.
    //
    this.y = function () {
        return this._y;
    };

    //
    // The parent block that the connector is attached to.
    //
    this.parent = function () {
        return this._parent;
    };
};

viewmodels.blockViewModel = function (data) {

    // reference the block data
    this.data = data;

    // Hardcoded height of each block
    this._blockHeight = 80;

    // Set to true when the block is selected
    this._selected = false;

    this.state = 0;

    this._progress = "UNKNOWN";

    /**
     * block state enumeration
     */
    this.blockState = {
        configuring: 0,
        ready: 1,
        executing: 2,
        complete: 3,
        error: 4
    };

    //
    // block's definition
    //
    this.definition = function () { return this.data.definition; };

    //
    // Enumeration representing the type of definition
    //
    this.definitionType = function(){ return this.data.definitionType; };

    //
    // Name of the block
    //
    this.name = function () { return this.data.name; };

    //
    // Unique Name of the block
    //
    this.id = function () { return this.data.id; };

    //
    // X coordinate of the block
    //
    this.x = function () { return this.data.x; };

    //
    // Y coordinate of the block
    //
    this.y = function () { return this.data.y; };

    //
    // Width of the block
    //
    this.width = function () { return this.data.w; };

    //
    // Height of the block
    //
    this.height = function () { return this._blockHeight; };

    //
    // Select this block
    //
    this.select = function () { this._selected = true; };

    //
    // Deselect this block
    //
    this.deselect = function () { this._selected = false; };

    //
    // Toggle the selection state of this block
    //
    this.toggleSelected = function () {
        this._selected = !this._selected;
    };

    //
    // Returns true if the node is selected.
    //
    this.selected = function () {
        return this._selected;
    };

    /**
     * Resolve this block's current state
     */
    this.getState = function(){

        var state;

        if (!this.data.configured){
            state = this.blockState.configuring;
        }
        else{
            state = this.blockState.ready;
        }

        return state;
    };

    this.updateProgress = function(state) { setTimeout(this.state = state, 10); };

    this.progressText = function(){

        var text;
        switch(this.state){
            case 0: // configuring
                text = "\uf040";
                break;
            case 1: // ready
                text = "\uf087";
                break;
            case 2: // executing
                text = "\uf085";
                break;
            case 3: // complete
                text = "\uf00c";
                break;
            case 4: // error
                text = "\uf12A";
                break;
        }

        return text;
    };

    this.stateBgStroke = function(){

        var text;
        switch(this.state){
            case 0: // configuring
                text = "#fdd0a2";
                break;
            case 1: // ready
                text = "#dadaeb";
                break;
            case 2: // executing
                text = "#c7e9c0";
                break;
            case 3: // complete
                text = "#aec7e8";
                break;
            case 4: // error
                text = "#ff9896";
                break;
        }

        return text;
    };

    this.stateFgStroke = function(){

        var text;
        switch(this.state){
            case 0: // configuring
                text = "#ff7f0e";
                break;
            case 1: // ready
                text = "#9467bd";
                break;
            case 2: // executing
                text = "#2ca02c";
                break;
            case 3: // complete
                text = "#1f77b4";
                break;
            case 4: // error
                text = "#d62728";
                break;
        }

        return text;
    };

    this.stateStrokeOffset = function(){

        var offset;
        switch(this.state){
            case 0: // configuring
                offset = 65.625;
                break;
            case 1: // ready
                offset = 43.75;
                break;
            case 2: // executing
                offset = 21.875;
                break;
            case 3: // complete
                offset = 0;
                break;
            case 4: // error
                offset = 0;
                break;
        }

        return offset;
    };

    //
    //
    //
    this.stateClass = function(){

        var result;
        switch(this.state){
            case 0:
                result = "block-config";
                break;
            case 1:
                result = "block-ready";
                break;
            case 2:
                result = "block-executing";
                break;
            case 3:
                result = "block-complete";
                break;
            case 4:
                result = "block-error";
                break;
        }

        return result;
    };

    //
    // retrieve parameter by name
    //
    this.getParameter = function(name){

        for(var p = 0; p < this.data.parameters.length; p++)
            if (this.data.parameters[p].name == name)
                return this.data.parameters[p];
    };

    // connector functions

    //
    // calculate the specified connectors x-coordinate
    //
    this.calculateConnectorX = function (visibleIndex, visibleCount) {

        var spacing = this.width() / (visibleCount + 1);
        return ((visibleIndex + 1) * spacing);
    };

    //
    // create view models for specified connectors
    //
    this.createConnectorViewModels = function (connectorsData, y, parent) {
        var viewModels = [];

        if (connectorsData) {

            var visibleConnectors = [];
            // capture the set of visible connectors
            var i;
            for(i = 0; i  < connectorsData.length; ++i){
                if (connectorsData[i].visible)
                    visibleConnectors.push(connectorsData[i]);
            }

            var visibleIndex = 0;
            for (i = 0; i < connectorsData.length; ++i) {

                var x = (connectorsData[i].visible) ? this.calculateConnectorX(visibleIndex++, visibleConnectors.length) : 0;
                var _connectorViewModel =
                    new viewmodels.connectorViewModel(connectorsData[i],
                        x,
                        y,
                        parent);

                viewModels.push(_connectorViewModel);
            }
        }

        return viewModels;
    };

    //
    // create view models for input and output connectors
    //
    this.inputConnectors = this.createConnectorViewModels(this.data.inputConnectors,
        0,
        this);

    this.outputConnectors = this.createConnectorViewModels(this.data.outputConnectors,
        this.height(),
        this);
};

viewmodels.wireViewModel = function (data, parent, sourceConnector, targetConnector) {

    this.data = data;
    this.parent = parent;
    this.source = sourceConnector;
    this.target = targetConnector;

    // Set to true when the connection is selected.
    this._selected = false;

    this.sourceConnectorX = function(){
        return this.source.x();
    };

    this.sourceConnectorY = function(){
        return this.source.y();
    };

    this.sourceCoordX = function() {
        return this.source.parent().x() + this.sourceConnectorX();
    };

    this.sourceCoordY = function() {
        return this.source.parent().y() + this.sourceConnectorY();
    };

    this.sourceCoord = function() {
        return {
            x: this.sourceCoordX(),
            y: this.sourceCoordY()
        };
    };

    this.sourceTangentX = function() {
        return this.parent.computeWireSourceTangentX(this.sourceCoord(), this.targetCoord());
    };

    this.sourceTangentY = function () {
        return this.parent.computeWireSourceTangentY(this.sourceCoord(), this.targetCoord());
    };

    this.targetConnectorX = function(){
        return this.target.x();
    };

    this.targetConnectorY = function(){
        return this.target.y();
    };

    this.targetCoordX = function() {
        return this.target.parent().x() + this.targetConnectorX();
    };

    this.targetCoordY = function() {
        return this.target.parent().y() + this.targetConnectorY();
    };

    this.targetCoord = function() {
        return {
            x: this.targetCoordX(),
            y: this.targetCoordY()
        };
    };

    this.targetTangentX = function() {
        return this.parent.computeWireTargetTangentX(this.sourceCoord(),
            this.targetCoord());
    };

    this.targetTangentY = function() {
        return this.parent.computeWireTargetTangentY(this.sourceCoord(),
            this.targetCoord());
    };

    //
    // Select the wire.
    //
    this.select = function() {
        this._selected = true;
    };

    //
    // Deselect the wire.
    //
    this.deselect = function() {
        this._selected = false;
    };

    //
    // Toggle the selection state of the connection.
    //
    this.toggleSelected = function() {
        this._selected = !this._selected;
    };

    //
    // Returns true if the connection is selected.
    //
    this.selected = function() {
        return this._selected;
    };
};

viewmodels.diagramViewModel = function(data) {

    // the diagram data model
    this.data = data;

    // internal variables

    var blockNames = {},        // tracks the current set of block names
        boundary = { x1: Number.MIN_VALUE, x2: Number.MIN_VALUE, y1: Number.MIN_VALUE, y2: Number.MIN_VALUE }, // track the diagram boundary
        selectionCount = 0,     // tracks the current number of selected blocks
        defaultName = "New Diagram";    // the default name given to a new diagram

    //
    // private methods
    //

    var BlockData = function(id, name, x, y, definitionViewModel){

        // generate a new guid for this block
        this.id = id;
        this.name = name;
        this.definition = definitionViewModel.name();
        this.definitionType = definitionViewModel.definitionType();
        this.configured = false;
        this.dirty = false;
        this.w = definitionViewModel.w();
        this.x = x;
        this.y = y;
        this.inputConnectors = angular.copy(definitionViewModel.inputs());
        this.outputConnectors = angular.copy(definitionViewModel.outputs());

        var parameters = [];
        definitionViewModel.parameters().forEach(function(parameter){

            var newParam = {
                name: parameter.name,
                parameterType: parameter.parameterType,
                valueType: parameter.valueType
            };
            if (typeof(parameter.value) === 'object')
                newParam.value = angular.copy(parameter.value);
            else
                newParam.value = parameter.value;

            parameters.push(newParam);
        });

        this.parameters = parameters;

        // if the block does not contain any parameters, set to configured
        if (this.parameters.length == 0)
            this.configured = true;
    };

    /**
     * Generate guid
     * @returns {string}
     */
    var newGuid = function() {

        var delim = "-";

        function S4() {
            return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
        }

        return (S4() + S4() + delim + S4() + delim + S4() + delim + S4() + delim + S4() + S4() + S4());
    };

    //
    // public properties
    //

    this.getId = function(){
        return data.id;
    };

    this.setId = function(id){
        this.data.id = id;
    };

    this.getName = function () {
        return this.data.name;
    };

    this.setName = function(value){
        this.data.name = value;
    };

    this.getDescription = function () {
        return this.data.description;
    };

    this.setDescription = function(value){
        this.data.description = value;
    };

    this.getTargetEnvironment = function () {
        return this.data.targetEnvironment;
    };

    this.setTargetEnvironment = function(value){
        this.data.targetEnvironment = value;
    };

    this.getOwner = function () {
        return this.data.owner;
    };

    this.setOwner = function(value){
        this.data.owner = value;
    };

    this.getCategory = function () {
        return this.data.category;
    };

    this.setCategory = function(value){
        this.data.category = value;
    };

    this.getMode = function(){ return this.data.mode; };

    this.mode = function(){ return this.data.mode; };

    //
    // public methods
    //

    /**
     * Select blocks and wires that fall within the selection rect
     * @param selectionRect
     */
    this.applySelectionRect = function (selectionRect) {

        selectionCount = 0;

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.x() >= selectionRect.x &&
                block.y() >= selectionRect.y &&
                block.x() + block.width() <= selectionRect.x + selectionRect.width &&
                block.y() + block.height() <= selectionRect.y + selectionRect.height) {

                selectionCount++;

                // Select blocks that are within the selection rect
                block.select();
            }
        }

        for (var j = 0; j < this.wires.length; ++j) {
            var wire = this.wires[j];
            if (wire.source.parent().selected() &&
                wire.target.parent().selected()) {

                // Select the wire if both its parent blocks are selected
                wire.select();
            }
        }
    };

    /**
     * calculate the connector's position
     * @param block
     * @param connectorIndex
     * @param inputConnector
     * @returns {{x: *, y: *}}
     */
    this.computeConnectorPos = function (block, connectorIndex, inputConnector) {
        return {
            x: block.x() + block.calculateConnectorX(connectorIndex, inputConnector),
            y: block.y() + (inputConnector ? 0 : block.height())
        };
    };

    /**
     * create a view model for each block
     * @param blocksData
     * @returns {Array}
     */
    this.createBlockViewModels = function (blocksData) {
        var models = [];

        if (blocksData) {
            for (var i = 0; i < blocksData.length; ++i) {

                var block = new viewmodels.blockViewModel(blocksData[i]);
                blockNames[block.name()] = true;

                this.updateBoundary(block);

                models.push(block);
            }
        }

        return models;
    };

    /**
     * create a view model for each child diagram
     * @param diagramsData
     * @returns {Array}
     */
    this.createDiagramViewModels = function(diagramsData){
        var models = [];

        if (diagramsData){
            for (var i = 0; i < diagramsData.length; ++i) {

                models.push(new viewmodels.diagramViewModel(diagramsData[i]));
            }
        }

        return models;
    };

    /**
     * create a view model for the specified wire
     * @param wireData
     * @returns {viewmodels.wireViewModel}
     */
    this.createWireViewModel = function(wireData) {

        var sourceConnector = this.findOutputConnector(wireData.from_node,
            wireData.from_connectorIndex);

        var destConnector = this.findInputConnector(wireData.to_node,
            wireData.to_connectorIndex);

        return new viewmodels.wireViewModel(wireData,
            this,
            sourceConnector,
            destConnector);
    };

    /**
     * create a view model for each wire in the list of wires
     * @param wiresData
     * @returns {Array}
     */
    this.createWireViewModels = function (wiresData) {

        var models = [];

        if (wiresData) {
            for (var i = 0; i < wiresData.length; ++i) {
                models.push(this.createWireViewModel(wiresData[i]));
            }
        }

        return models;
    };

    /**
     * Add a new block to the diagram
     * @param blockData
     */
    this.addBlock = function (blockData) {

        if (!this.data.blocks) {
            this.data.blocks = [];
        }

        // capture new block name
        blockNames[blockData.name] = true;

        //
        // Update the data model.
        //
        this.data.blocks.push(blockData);

        // create the block view model
        var block = new viewmodels.blockViewModel(blockData);

        // update the diagram boundary
        this.updateBoundary(block);

        //
        // Update the view model.
        //
        this.blocks.push(block);
    };

    /**
     * Create a view model for a new wire
     * @param startConnector
     * @param endConnector
     */
    this.createWire = function (startConnector, endConnector) {

        var wiresData = this.data.wires;
        if (!wiresData) {
            wiresData = this.data.wires = [];
        }

        var wiresViewModels = this.wires;
        if (!wiresViewModels) {
            wiresViewModels = this.wires = [];
        }

        var startBlock = startConnector.parent();
        var startConnectorIndex = startBlock.outputConnectors.indexOf(startConnector);
        var startConnectorType = 'output';
        if (startConnectorIndex == -1) {
            startConnectorIndex = startBlock.inputConnectors.indexOf(startConnector);
            startConnectorType = 'input';
            if (startConnectorIndex == -1) {
                throw new Error("Failed to find source connector within either inputConnectors or outputConnectors of source block.");
            }
        }

        var endBlock = endConnector.parent();
        var endConnectorIndex = endBlock.inputConnectors.indexOf(endConnector);
        var endConnectorType = 'input';
        if (endConnectorIndex == -1) {
            endConnectorIndex = endBlock.outputConnectors.indexOf(endConnector);
            endConnectorType = 'output';
            if (endConnectorIndex == -1) {
                throw new Error("Failed to find dest connector within inputConnectors or outputConnectors of dest block.");
            }
        }

        if (startConnectorType == endConnectorType) {
            throw new Error("Failed to create wire. Only output to input wires are allowed.")
        }

        if (startBlock == endBlock) {
            throw new Error("Failed to create wire. Cannot link a block with itself.")
        }

        var wireDataModel = {
            from_node: (startConnectorType == 'output') ? startBlock.data.id : endBlock.data.id,
            from_connectorIndex: (startConnectorType == 'output') ? startConnectorIndex : endConnectorIndex,
            from_connector: (startConnectorType == 'output') ? startConnector.name() : endConnector.name(),
            to_node: (startConnectorType == 'output') ? endBlock.data.id : startBlock.data.id,
            to_connectorIndex: (startConnectorType == 'output') ? endConnectorIndex : startConnectorIndex,
            to_connector: (startConnectorType == 'output') ? endConnector.name() : startConnector.name()
        };

        wiresData.push(wireDataModel);

        var outputConnector = startConnectorType == 'output' ? startConnector : endConnector;
        var inputConnector = startConnectorType == 'output' ? endConnector : startConnector;

        var wireViewModel = new viewmodels.wireViewModel(wireDataModel,
            this,
            outputConnector,
            inputConnector);
        wiresViewModels.push(wireViewModel);
    };

    /**
     * delete the specified block
     * @param block
     */
    this.deleteBlock = function(block){

        this.selectBlock(block);
        this.deleteSelected();
    };

    /**
     * Delete all blocks and wires that are selected.
     * @returns {number}
     */
    this.deleteSelected = function () {

        var newBlockViewModels = [];
        var newBlocks = [];

        var deletedBlocks = [];

        //
        // Sort blocks into:
        //		blocks to keep and
        //		blocks to delete.
        //

        for (var blockIndex = 0; blockIndex < this.blocks.length; ++blockIndex) {

            var block = this.blocks[blockIndex];
            if (!block.selected()) {
                // Only retain non-selected nodes.
                newBlockViewModels.push(block);
                newBlocks.push(block.data);
            }
            else {
                // Keep track of blocks that were deleted, so their wires can also
                // be deleted.
                deletedBlocks.push(block.data.id);

                delete blockNames[block.name()];
            }
        }

        var newWireViewModels = [];
        var newWires = [];

        //
        // Remove wires that are selected.
        // Also remove wires for blocks that have been deleted.
        //
        for (var wireIndex = 0; wireIndex < this.wires.length; ++wireIndex) {

            var wire = this.wires[wireIndex];
            if (!wire.selected() &&
                deletedBlocks.indexOf(wire.data.from_node) === -1 &&
                deletedBlocks.indexOf(wire.data.to_node) === -1)
            {
                //
                // The nodes this connection is attached to, where not deleted,
                // so keep the connection.
                //
                newWireViewModels.push(wire);
                newWires.push(wire.data);
            }
        }

        //
        // Update nodes and connections.
        //
        this.blocks = newBlockViewModels;
        this.data.blocks = newBlocks;
        this.wires = newWireViewModels;
        this.data.wires = newWires;

        // reset the diagram boundary
        this.resetBoundary();

        return 0;
    };

    /**
     * Deselect all blocks and wires in the diagram, return the number of selected blocks (0)
     */
    this.deselectAll = function () {

        var blocks = this.blocks;
        for (var i = 0; i < blocks.length; ++i) {
            var block = blocks[i];
            block.deselect();
        }

        var wires = this.wires;
        for (var j = 0; j < wires.length; ++j) {
            var wire = wires[j];
            wire.deselect();
        }

        selectionCount = 0;
    };

    /**
     * Find a block by guid
     * @param id
     * @returns {*}
     */
    this.findBlock = function (id) {

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.data.id == id)
                return block;
        }

        throw new Error("Failed to find block " + id);
    };

    this.findBlockByName = function(name) {

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.data.name == name)
                return block;
        }

        throw new Error("Failed to find block " + name);
    };

    /**
     * Find a diagram by name
     * @param name
     * @returns {*}
     */
    this.findDiagram = function(name){
        for (var i = 0; i < this.diagrams.length; ++i) {
            var diagram = this.diagrams[i];
            if (diagram.data.name == name) {
                return diagram;
            }
        }

        throw new Error("Failed to find block " + name);
    };

    /**
     * Find input connector by block id and connector index
     * @param id
     * @param connectorIndex
     * @returns {*}
     */
    this.findInputConnector = function (id, connectorIndex) {

        var block = this.findBlock(id);

        if (!block.inputConnectors || block.inputConnectors.length <= connectorIndex) {
            throw new Error("Block " + id + " has invalid input connectors.");
        }

        return block.inputConnectors[connectorIndex];
    };

    /**
     * Find output connector by block id and connector index
     * @param id
     * @param connectorIndex
     * @returns {*}
     */
    this.findOutputConnector = function (id, connectorIndex) {

        var block = this.findBlock(id);

        if (!block.outputConnectors || block.outputConnectors.length <= connectorIndex) {
            throw new Error("Block " + id + " has invalid output connectors.");
        }

        return block.outputConnectors[connectorIndex];
    };

    /**
     * Returns an array of all input wires for the specified connector (blockId, connector name)
     * @param id: block id
     * @param connectorName
     * @returns {Array}
     */
    this.findInputWires = function(id, connectorName){

        var wires = [];
        for(var i = 0; i < this.wires.length; i++){
            var wire = this.wires[i].data;
            if (wire["to_node"] == id && wire["to_connector"] == connectorName)
                wires.push(wire);
        }

        return wires;
    };

    /**
     * Returns an array of all output wires for the specified connector (blockId, connector name)
     * @param id: block id
     * @param connectorName
     * @returns {Array}
     */
    this.findOutputWires = function(id, connectorName) {

        var wires = [];
        for(var i = 0; i < this.wires.length; i++){
            var wire = this.wires[i];
            if (wire["from_node"] == id && wire["from_connector"] == connectorName)
                wires.push(wire);
        }

        return wires;
    };

    /**
     *
     * @param x
     * @param y
     * @param definitionViewModel
     * @returns {BlockData}
     */
    this.getBlockData = function(x, y, definitionViewModel){

        var id = newGuid(),
            name = this.generateBlockName(definitionViewModel.name());

        return new BlockData(id, name, x, y, definitionViewModel);
    };

    this.getBoundary = function(){

        return boundary;
    };

    /**
     * Get the array of blocks that are currently selected.
     * @returns {Array}
     */
    this.getSelectedBlocks = function () {
        var selectedBlocks = [];

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            if (block.selected()) {
                selectedBlocks.push(block);
            }
        }

        return selectedBlocks;
    };

    /**
     * retrieve the number of selected blocks
     * @returns {number}
     */
    this.getSelectionCount = function() { return selectionCount; };

    /**
     * Get the array of wires that are currently selected.
     * @returns {Array}
     */
    this.getSelectedWires = function () {

        var selectedWires = [];

        for (var i = 0; i < this.wires.length; ++i) {

            var wire = this.wires[i];
            if (wire.selected()) {

                selectedWires.push(wire);
            }
        }

        return selectedWires;
    };

    /**
     * Generate a unique block name based on the definition name
     * @param definitionName
     * @returns {*}
     */
    this.generateBlockName = function(definitionName){

        // generate unique block name
        var index = 1;
        var name = definitionName;
        while(name in blockNames){
            name = definitionName + index;
            index++;
        }

        return name;
    };

    /**
     * checks whether this diagram has the default diagram name
     * @returns {boolean}
     */
    this.hasDefaultName = function(){
        return (this.getName() == defaultName);
    };

    /**
     * Handle mouse click on a particular block.
     * @param block
     */
    this.onBlockClicked = function (block) {

        this.selectBlock(block);

        // Move node to the end of the list so it is rendered after all the other.
        // This is the way Z-order is done in SVG.

        var blockIndex = this.blocks.indexOf(block);
        if (blockIndex == -1) {
            throw new Error("Failed to find block in view model!");
        }
        this.blocks.splice(blockIndex, 1);
        this.blocks.push(block);
    };

    /**
     * Handle mouse down on a wire.
     * @param wire
     * @param ctrlKey
     */
    this.onWireMouseDown = function (wire, ctrlKey) {

        if (ctrlKey) {
            wire.toggleSelected();
        }
        else {
            this.deselectAll();
            wire.select();
        }
    };

    /**
     * Reset the diagram boundary
     */
    this.resetBoundary = function(){

        var count = this.blocks.length;
        if (count == 0)
            boundary = { x1: Number.MIN_VALUE, x2: Number.MIN_VALUE, y1: Number.MIN_VALUE, y2: Number.MIN_VALUE };

        for (var i = 0; i < count; ++i)
            this.updateBoundary(this.blocks[i]);
    };

    /**
     * select all blocks
     */
    this.selectAll = function () {

        selectionCount = 0;

        for (var i = 0; i < this.blocks.length; ++i) {
            var block = this.blocks[i];
            selectionCount++;
            block.select();
        }

        for (var j = 0; j < this.connections.length; ++j) {
            var wire = this.wires[i];
            wire.select();
        }
    };

    /**
     * select block
     * @param block
     */
    this.selectBlock = function(block){

        this.deselectAll();
        block.select();

        selectionCount = 1;
    };

    /**
     * Update a block view model based on the specified config block
     * @param configBlock
     */
    this.updateBlock = function(configBlock){

        var block = this.findBlock(configBlock.id);

        if ((configBlock.name) && (!blockNames[configBlock.name])) {
            block.data.name = configBlock.name;
            blockNames[configBlock.name] = true;
        }

        var configured = true;
        var dirty = false;
        configBlock.parameters.forEach(function(configParameter){

            if (!configParameter.collected)
                configured = false;

            if (configParameter.dirty)
                dirty = true;

            var parameter = block.getParameter(configParameter.name());
            parameter.value = configParameter.value;
            parameter.collected = configParameter.collected;
        });

        /*
        if (dirty && configured)
            block.data.state = 1;
        else if (!configured)
            block.data.state = 0;
            */
    };

    /**
     * Update the state of the specified block
     * @param blockId
     * @param blockState
     */
    this.updateBlockState = function(blockId, blockState) {

        var block = this.findBlock(blockId);
        block.updateProgress(blockState);
    };

    /**
     * Check whether the specified block expands the diagram's current boundary
     * @param block
     */
    this.updateBoundary = function(block){

        if (boundary.x1 == Number.MIN_VALUE || block.x() < boundary.x1)
            boundary.x1 = block.x();
        if (boundary.x2 == Number.MIN_VALUE || (block.x() + block.width()) > boundary.x2)
            boundary.x2 = (block.x() + block.width());
        if (boundary.y1 == Number.MIN_VALUE || block.y() < boundary.y1)
            boundary.y1 = block.y();
        if (boundary.y2 == Number.MIN_VALUE || (block.y() + block.height()) > boundary.y2)
            boundary.y2 = (block.y() + block.height());
    };

    /**
     * Update the location of the block and its connectors.
     * @param deltaX
     * @param deltaY
     */
    this.updateSelectedBlocksLocation = function (deltaX, deltaY) {

        var selectedBlocks = this.getSelectedBlocks();

        for (var i = 0; i < selectedBlocks.length; ++i) {
            var block = selectedBlocks[i];
            block.data.x += deltaX;
            block.data.y += deltaY;
        }
    };

    // create a view model for each block
    this.blocks = this.createBlockViewModels(this.data.blocks);

    // create a view model for each wire
    this.wires = this.createWireViewModels(this.data.wires);

    // create a view model for each nested diagram
    this.diagrams = this.createDiagramViewModels(this.data.diagrams);

    //
    // Bezier curve calculations
    //

    this.computeWireSourceTangentX = function (pt1, pt2) {

        return pt1.x;
    };

    this.computeWireSourceTangentY = function (pt1, pt2) {

        return pt1.y + this.computeWireTangentOffset(pt1, pt2);
    };

    this.computeWireSourceTangent = function(pt1, pt2) {
        return {
            x: this.computeWireSourceTangentX(pt1, pt2),
            y: this.computeWireSourceTangentY(pt1, pt2)
        };
    };

    this.computeWireTargetTangentX = function (pt1, pt2) {

        return pt2.x;
    };

    this.computeWireTargetTangentY = function (pt1, pt2) {

        return pt2.y - this.computeWireTangentOffset(pt1, pt2);
    };

    this.computeWireTargetTangent = function(pt1, pt2) {
        return {
            x: this.computeWireTargetTangentX(pt1, pt2),
            y: this.computeWireTargetTangentY(pt1, pt2)
        };
    };

    this.computeWireTangentOffset = function (pt1, pt2) {

        return (pt2.y - pt1.y) / 2;
    };
};

viewmodels.configuringBlockViewModel = function (definitionViewModel, blockData) {

    this.id = blockData.id;
    this.name = blockData.name;
    this.parameters = [];

    this.data = blockData;
    this.definitionViewModel = definitionViewModel;

    //
    // public methods
    //

    this.getBlock = function(){

        // update the name
        this.data.name = this.name;

        // update block parameter values
        var configured = true,
            parameters = [];
        this.parameters.forEach(function(parameter){

            if (!parameter.collected)
                configured = false;

            parameters.push({
                name: parameter.name(),
                type: parameter.type(),
                value: parameter.value,
                collected: parameter.collected
            });
        });
        this.data.parameters = parameters;

        return this.data;
    };

    this.getParameter = function(name){

        for(var p = 0; p < this.parameters.length; p++)
            if (this.parameters[p].name() == name)
                return this.parameters[p];
    };

    this.init = function(){

        var blockParameters = this.data.parameters;

        // capture the block's parameters
        var parameters = [];
        this.definitionViewModel.parameters().forEach(function(parameterDefinition){

            // if exists - reference block parameter
            var blockParameter;
            if (blockParameters){
                for(var i = 0; i < blockParameters.length; i++){

                    if (blockParameters[i].name == parameterDefinition.name){

                        blockParameter = blockParameters[i];
                        break;
                    }
                }
            }

            // copy parameter definition
            var parameter = new viewmodels.configuringParameterViewModel(parameterDefinition, blockParameter);

            // add to list of parameters
            parameters.push(parameter);
        });

        this.parameters = parameters;
    };

    this.reset = function() {
        return new viewmodels.configuringBlockViewModel(this.definition, this.data);
    };

    this.setParameter = function(name, value){

        var parameter = this.getParameter(name);
        parameter.value = value;
        parameter.dirty = true;
        parameter.collected = true;
    };

    this.init();
};

viewmodels.configuringParameterViewModel = function (definitionParameter, blockParameter) {

    this.data = angular.copy(definitionParameter);

    this.loaded = (this.data.source == null);
    this.loading = false;
    this.dirty = false;
    this.fieldOptions = this.data.fieldOptions;

    if (blockParameter && blockParameter.collected !== undefined){
        this.collected = blockParameter.collected;
    }
    else{
        this.collected = false;
    }

    if (blockParameter && blockParameter.value !== undefined){
        this.value = blockParameter.value;
    }
    else{
        this.value = this.data.value;
    }

    this.editTypes = {
        number: 0,
        text: 1,
        textArea: 2,
        list: 3,
        multiSelectList: 4
    };

    this.editType = function(){

        var result;
        switch(this.parameterType()){
            case "ENUMERATION":
                if (this.valueType() == "SCALAR")
                    result = this.editTypes.list;
                else
                    result = this.editTypes.multiSelectList;
                break;
            case "INT":
                result = this.editTypes.number;
                break;
            case "JSON":
                result = this.editTypes.textArea;
            case "STRING":
            default:
                result = this.editTypes.text;
                break;

        }

        return result;
    };

    this.name = function(){
        return this.data.name;
    };

    this.parameterType = function(){
        return this.data.parameterType;
    };

    this.source = function(){
        return this.data.source;
    };

    this.valueType = function(){
        return this.data.valueType;
    };
};

viewmodels.definitionViewModel = function (mode, data) {

    // reference the mode and the data
    this.mode = mode;
    this.data = data;

    switch(this.mode){
        case "OFFLINE":
            this.modeDefinition = this.data.offlineDefinition;
            break;
        case "ONLINE":
            if (this.data.onlineDefinition)
                this.modeDefinition = this.data.onlineDefinition;
            else
                this.modeDefinition = this.data.offlineDefinition;
            break;
    }

    //
    // get the definition type
    //
    this.definitionType = function(){ return this.data.definitionType; };

    //
    // get the input connectors
    //
    this.inputs = function(){ return this.modeDefinition.inputs; };

    //
    // returns the mode definition according to the specified mode
    //
    this.setModeDefinition = function(){


    };

    //
    // get the definitions name
    //
    this.name = function(){ return this.data.name; };

    //
    // get the output connectors
    //
    this.outputs = function(){ return this.modeDefinition.outputs; };

    //
    // get the parameters
    //
    this.parameters = function(){ return this.modeDefinition.parameters; };

    //
    // get the width
    //
    this.w = function(){ return this.data.w; };
};

