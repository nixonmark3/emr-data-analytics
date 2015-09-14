# -*- coding: utf-8 -*-
"""
Created on Fri Jun 13 14:13:37 2014

@author: rw23722
"""
import pandas as pd
import pandas.io.excel as pd_excel
import numpy as np
import numpy.linalg as linalg
#import find_train_set
import radPackage
from scipy import stats
from scipy.stats import norm
from scipy.stats import chi2
import math
import cmath
import os
#os.environ['MDP_DISABLE_SKLEARN']='yes'
import matplotlib.pyplot as plt
from matplotlib import mlab
from mpl_toolkits.mplot3d import Axes3D
from FunctionBlock import FunctionBlock
import numpy as np
#import mdp
import sys


class RadPlot(FunctionBlock):
    def __init__(self,name,unique_name):
        FunctionBlock.__init__(self,name,unique_name)
        
    def execute(self,results_table):
        try:
            FunctionBlock.report_status_executing(self)
            
            # Check for inputs
            FunctionBlock.check_connector_has_one_wire(self,'Data')
            Data = results_table[self.input_connectors['Data'][0]]
            FunctionBlock.check_connector_has_one_wire(self,'Mean')
            Mu = results_table[self.input_connectors['Mean'][0]]
            FunctionBlock.check_connector_has_one_wire(self,'Std')
            Sigma = results_table[self.input_connectors['Std'][0]]
            FunctionBlock.check_connector_has_one_wire(self,'Loadings')
            Loadings = results_table[self.input_connectors['Loadings'][0]]
            FunctionBlock.check_connector_has_one_wire(self,'Eigenvalues')
            Eigenvalues = results_table[self.input_connectors['Eigenvalues'][0]]
            
            # Check for parameters
            auto_pick_range = int(self.parameters['Autopick(1 = yes, 0 = no)'])
            win_size = int(self.parameters['Moving Window Size'])
            head = int(self.parameters['Start'])
            tail = int(self.parameters['End'])
            cut_off_val = self.parameters['VarCapNumPC']
            conf_int = self.parameters['ConfInt']
            num_vars_show = int(self.parameters['NumContVar'])
            OutputParam = int(self.parameters['OutputIndex'])
            if (cut_off_val == 0) | (conf_int == 0) | (num_vars_show == 0):
                FunctionBlock.report_status_configure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}
            
            # Start algorithm
            steadystateData,int_optirange = radPackage.getSteadyState(Data, auto_pick_range, win_size, head, tail) # Params auto_pick_range (default 1),win_size,and Head,Tail
            DataXYDF = radPackage.radPlot(Data,Mu,Sigma,Loadings,Eigenvalues,int_optirange, cut_off_val)  # Params cutoffval
            print('after DataXYDF........\n')
            CentXYDF = radPackage.calculateCentroids(DataXYDF)
            print('after CentXYDF........\n')
            faultListDF,int_timeoffault,timeoffaultDF,binaryFaultListDF = radPackage.faultDetection(Data,CentXYDF,int_optirange, conf_int) # Params conf

            print('after faultlistDF........\n')

            #listTagsDF = radPackage.faultAnalysis(Data,faultListDF, num_vars_show) # Params numVarsShow
            #Data2 = Data.as_matrix()
            #Data2DF = pd.DataFrame(Data2,columns=Data2[0,:])
            #cols = Data.columns
            #Data2DF.columns=cols
            #DataXYDF.columns = ['a','b','c','d','e','f','g','h','i','j','k','l']
            # Save results
#            FunctionBlock.save_results(self,df=Data,statistics=True)
            #binaryFaultListDF.columns = ['fault']
            binaryFaultListDF.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_BinaryFaultList.csv')
            CentXYDF.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_Centroids.csv')
            timeoffaultDF.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_TimeofFault.csv')
            Loadings.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_Loadings.csv')
            Eigenvalues.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_Eigenvalues.csv')
            Mu.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_Mean.csv')
            Sigma.to_csv('/Users/noelbell/bds_datafiles/Test_raydata_Sigma.csv')

            index = Data.index
            binaryFaultListDF.index = index

            # Report status
            FunctionBlock.report_status_complete(self)
            
            # Return data
            # List of possible returns: Tag List, Fault times, DataXY, CentroidXY
            # if(OutputParam == 1):
            #     return {FunctionBlock.getFullPath(self,'out'): listTagsDF}
            # elif(OutputParam == 2):
            #     return {FunctionBlock.getFullPath(self,'out'): faultListDF}
            # elif(OutputParam == 3):
            #return {FunctionBlock.getFullPath(self,'out'): DataXYDF}
            #return {FunctionBlock.getFullPath(self,'out'): Data2DF}
            # elif(OutputParam == 4):
            #     return {FunctionBlock.getFullPath(self,'out'): CentXYDF}
            # elif(OutputParam == 5):
            #return {FunctionBlock.getFullPath(self,'out'): timeoffaultDF}
            return {FunctionBlock.getFullPath(self,'out'): binaryFaultListDF}
        
        except Exception as err:
             FunctionBlock.save_results(self)
             FunctionBlock.report_status_failure(self)
             print(err.args, file=sys.stderr)
        

