# -*- coding: utf-8 -*-
"""
Created on Tue Aug 25 16:06:13 2015

@author: rw23722
"""

# -*- coding: utf-8 -*-
"""
Created on Fri Jun 13 14:13:37 2014

@author: rw23722
"""
import pandas as pd
import pandas.io.excel as pd_excel
#import find_train_set
import radPackage
import os
#os.environ['MDP_DISABLE_SKLEARN']='yes'
from FunctionBlock import FunctionBlock
#import mdp
import sys
import traceback

class AutoPickTrain(FunctionBlock):
    def __init__(self,name,unique_name):
        FunctionBlock.__init__(self,name,unique_name)
        
    def execute(self,results_table):
        try:
            FunctionBlock.report_status_executing(self)
            
            # Check for inputs
            FunctionBlock.check_connector_has_one_wire(self,'Data')
            Data = results_table[self.input_connectors['Data'][0]]
            
            # Check for parameters
            auto_pick_range = int(self.parameters['Autopick(1 = yes, 0 = no)'])
            win_size = int(self.parameters['Moving Window Size'])
            head = int(self.parameters['Start'])
            tail = int(self.parameters['End'])
            
            # Start algorithm
            if(auto_pick_range == 1):
                #        SSData,optirange = find_train_set.find_train_set(Data,win_size)
                PickedDataDF, optirange = radPackage.findOptirange(Data,win_size)
            else:
                optirange = range(head-1,tail+1) #Inclusive and 0-index, hence +1 and -1
                PickedDataDF = Data.iloc[optirange,:]
            
            index = Data.index
            #PickedDataDF.index = index
            
            # Save results
#            FunctionBlock.save_results(self,df=listTagsDF,statistics=True)

            # Report status
            FunctionBlock.report_status_complete(self)
            
            # Return data
            return {FunctionBlock.getFullPath(self,'out'): PickedDataDF}
        
        except:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            traceback.print_exc(file=sys.stderr)
        

