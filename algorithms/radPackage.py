# -*- coding: utf-8 -*-
"""
Created on Fri Jun 19 15:14:59 2015

@author: rw23722
"""

import pandas as pd
import pandas.io.excel as pd_excel
import numpy as np
import numpy.linalg as linalg
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


def findOptirange(Data,winSize):
    
    # Calculate rolling variance
    RollingVar = pd.rolling_var(Data,winSize,center=True)
    # Sum up rolling variance per sample    
    sumRollingVar = RollingVar.sum(axis=1,skipna=True)
    # Find mean    
    meanVar = np.mean(sumRollingVar)

    #Calculate sample indexes where rolling variance is less than the average
    varIndex = np.zeros((1,1))
    for i in range(sumRollingVar.shape[0]):
        if(sumRollingVar[i]<meanVar):
            if(i == 0):
                varIndex = i
            else:
                varIndex = np.append(varIndex,i)
    # Connect contiguous regions
    head = 0
    tail = 0
    numpot = 1
    headArray = np.zeros((1,1))
    tailArray = np.zeros((1,1))
    i = 0
    while(i < varIndex.shape[0]-1):
        if(varIndex[i+1] == varIndex[i]+1):
            if(head==0):
                head = varIndex[i]
            j = i
            while(j < varIndex.shape[0]-1 and varIndex[j+1]==(varIndex[j]+1)):
                tail = varIndex[j+1]
                j = j+1
            if(numpot == 1):
                headArray = head
                tailArray = tail
            else:
                headArray = np.append(headArray,head)
                tailArray = np.append(tailArray,tail)
            i = j    
            head = 0
            tail = 0
            numpot = numpot + 1
        else:
            i = i + 1
    
    # Find largest contiguous region
    difference = np.zeros(headArray.shape)
    for i in range(headArray.shape[0]):
        difference[i] = tailArray[i]-headArray[i]
    
    index = np.argmax(difference)
    optirange = range(int(headArray[index]),int(tailArray[index]+1))
    picked_data = Data.iloc[optirange,:]
    return picked_data,optirange
    
    #Return dataframe slice
    
def getSteadyState(Data,auto_pick_range=1,win_size=30,head=1,tail=0): # PARAMETERS: auto_pick_range, win_size OR range depending on auto_pick_range value
    if(auto_pick_range == 1):
#        SSData,optirange = find_train_set.find_train_set(Data,win_size)
        SSData, optirange = findOptirange(Data,win_size)
    else:
        optirange = range(head-1,tail+1) #Inclusive and 0-index, hence +1 and -1
        SSData = Data.iloc[optirange,:]
    
    return SSData,optirange
    

def radPlot(Data,Mu,Sigma,Loadings,Eigenvalues,optirange,cut_off_val=5): # PARAMETER: cut_off_val
    index = Data.index
    Data = Data.as_matrix(columns=None)
    Loadings = Loadings.as_matrix(columns=None)
    Eigenvalues = Eigenvalues.as_matrix(columns=None)
    Mu = Mu.as_matrix(columns=None)
    Sigma = Sigma.as_matrix(columns=None)
    for i in range(Sigma.shape[0]):
        if(Sigma[i] == 0):
            Sigma[i] = 1
    #print('data.....\n', Data)
    #print('Mu........\n', Mu)
    #print('Sigma.....\n', Sigma)
    ZscoreSSData = (Data-Mu)/Sigma #Full data normalized by steady state mean and standard deviation
    ZscoreSSData = pd.DataFrame(ZscoreSSData)
    print('ZscoreSSData.....\n',ZscoreSSData)
    sumEigen = sum(Eigenvalues)
    Frac = np.zeros((Eigenvalues.shape[0],1))
    for i in range(Eigenvalues.shape[0]):
        Frac[i] = Eigenvalues[i]/sumEigen
    Scores = np.matrix(ZscoreSSData)*np.matrix(Loadings)
    Scores = np.array(Scores)
    retainNum = 0

    #print('Scores......\n', Scores)

    if(cut_off_val < 1):
        sumFracs = 0
        for i in range(Frac.shape[0]):
            sumFracs = sumFracs+Frac[i]
            if(sumFracs > cut_off_val):
                retainNum = i
                break
    else:
        retainNum = cut_off_val
        
    Scores = Scores.transpose()
    Scores = Scores[0:retainNum,:]
    Scores = stats.zscore(Scores,axis = 1)

    #print('Scores2..secondtime....\n', Scores)


        
   # Do radial plotting

    ##print('shape Scores....  ', Scores.shape)
    #print('shape optirange...', optirange)

    DataX,DataY = radialPlotting(Data,Scores,optirange)

    #print('DataX.....\n', DataX)
    #print('DataY.....\n', DataY)

    temp = np.concatenate((DataX,DataY))
    DataXY = pd.DataFrame(np.transpose(temp))


    #DataXY.index = index
    #target = open('/Users/noelbell/bds_datafiles/Sample_raydata_testoutput.csv')
    #print('RETURN DataXY')
    #print(DataXY)
    return DataXY
    
def radialPlotting(Data,Scores,optirange):
    # Convert data to X,Y points
    minValArray = np.amin(Scores,1)
    maxValArray = np.amax(Scores,1)
    minValArray[:] = np.amin(minValArray)
    maxValArray[:] = np.amax(maxValArray)
    minValArray = np.matrix(minValArray).transpose()
    maxValArray = np.matrix(maxValArray).transpose()
    minValArray = np.array(minValArray)
    maxValArray = np.array(maxValArray)
    m,n = Scores.shape
    m = int(m)
    n = int(n)
    # Get the axis
    # old python2.7......temp = range(m+1)
    # old python2.7......temp.reverse()
    temp = list(reversed(range(m+1)))
    th = (2 * np.pi /m)*np.array((np.matrix(np.ones((2,1)))*np.matrix(temp)))
    r = np.array(np.matrix(np.array((0,1))).transpose()*np.matrix(np.ones((1,m))))

    axescoords = np.zeros((int(r.shape[0]),int(r.shape[1])),dtype=complex)
    axesX = np.zeros((int(r.shape[0]),int(r.shape[1])))
    axesY = np.zeros((int(r.shape[0]),int(r.shape[1])))
    for i in range(r.shape[0]):
        for j in range(r.shape[1]):
            axescoords[i,j] = cmath.rect(r[i,j],th[i,j])
            axesX[i,j] = axescoords[i,j].real
            axesY[i,j] = axescoords[i,j].imag
    

    
    # Get isocurves
    thcurves = (2*np.pi/m)*np.array((np.matrix(np.ones((9,1)))*np.matrix(temp)))
    rcurves = np.array(np.matrix(np.linspace(0.1,0.9,num=9)).transpose()*np.matrix(np.ones((1,m))) )   
    
    curvecoords = np.zeros((int(rcurves.shape[0]),int(rcurves.shape[1])),dtype=complex)
    curveX = np.zeros((int(rcurves.shape[0]),int(rcurves.shape[1])))
    curveY = np.zeros((int(rcurves.shape[0]),int(rcurves.shape[1])))
    for i in range(rcurves.shape[0]):
        for j in range(rcurves.shape[1]):
            curvecoords[i,j] = cmath.rect(rcurves[i,j],thcurves[i,j])
            curveX[i,j] = curvecoords[i,j].real
            curveY[i,j] = curvecoords[i,j].imag
    
    # Plot isocurves
    tempCurveX = np.column_stack((curveX,curveX[:,0]))
    tempCurveX = np.array(np.matrix(tempCurveX).transpose())
    
    tempCurveY = np.column_stack((curveY,curveY[:,0]))
    tempCurveY = np.array(np.matrix(tempCurveY).transpose())
    
    
    
    # Get the Radius, Th, for the data
    temp = np.ones((1,n))
    A = np.matrix(minValArray)*np.matrix(temp)
    B = np.matrix(maxValArray-minValArray)*np.matrix(temp)
    C = np.matrix(Scores)-A
    D = np.array(C)/np.array(B)
    Radius = 0.8*D+0.1
    Radius = np.row_stack((Radius,Radius[0,:]))

    print('print 1')

    # old python2.7......temp = range(m+1) # In Matlab it goes from M:-1:0, so m+1 is used
    # old python2.7......temp.reverse()
    temp = list(reversed(range(m+1)))

    temp = np.matrix(temp)
    temp = temp.transpose()
    Th = (2*(np.pi)/m)*(temp*np.ones((1,n)))
    Th = np.array(Th)

    print('print 2')

    # Obtain X, Y coordinates of Data
    coords = np.zeros((int(Radius.shape[0]),int(Radius.shape[1])),dtype=complex)
    DataX = np.zeros((int(Radius.shape[0]),int(Radius.shape[1])))
    DataY = np.zeros((int(Radius.shape[0]),int(Radius.shape[1])))
    for i in range(Radius.shape[0]):
        for j in range(Radius.shape[1]):
            coords[i,j] = cmath.rect(Radius[i,j],Th[i,j])
            DataX[i,j] = coords[i,j].real
            DataY[i,j] = coords[i,j].imag

    print('print 3')
    Z = np.array(range(DataX.shape[1]))
    
    temp = np.concatenate((DataX,DataY))  
    DataXY = pd.DataFrame(np.transpose(temp))
    
    CentXY = calculateCentroids(DataXY)
    print('print 4')
    #faultListDF,int_timeoffaults,timeoffaultDF,binaryFaultList = faultDetection(Data,CentXY,optirange)
    #plotting(DataX,DataY,axesX,axesY,tempCurveX,tempCurveY,int_timeoffaults)


    return DataX,DataY
    
def plotting(DataX,DataY,axesX,axesY,tempCurveX,tempCurveY,int_timeoffaults):
    # Plot figures
    # Plot axes
    figure = plt.figure()
    plt.show()
    ax = figure.gca(projection='3d')
    ax.plot(axesX,axesY,color='black')
    #plt.hold  # pycharm says this has no effect
    ax.plot(tempCurveX,tempCurveY, color='black') # Plot iscurves
    Z = np.array(range(DataX.shape[1]))
    
    for i in range(DataX.shape[1]):
        if(i in int_timeoffaults):

            ax.plot(DataX[:,i],DataY[:,i],Z[i],color='red')
        else:

            ax.plot(DataX[:,i],DataY[:,i],Z[i],color='blue')
            
    tempstrname = 'temp'
    uniquenum = 1
    while os.path.exists(tempstrname+str(uniquenum)+'.png'):
        uniquenum+=1
    
    tempstrname = tempstrname+str(uniquenum)
    plt.savefig(tempstrname+'.png')
    plt.close(figure)    
    
def calculateCentroids(DataXY):
    columnNum = DataXY.shape[1]
    ind = int(columnNum/2)
    DataX = DataXY.iloc[:,range(ind)].as_matrix(columns=None)
    DataY = DataXY.iloc[:,range(ind,ind*2)].as_matrix(columns=None)
    
    DataX = DataX.transpose()
    DataY = DataY.transpose()

    
    # Calculate area
    A = np.zeros((1,int(DataX.shape[1])))
    for i in range(DataX.shape[1]):
        a = DataX[:-1,i] * DataY[1:,i]
        b = DataY[:-1,i] * DataX[1:,i]
        A[:,i] = np.sum(a - b) / 2.
    
    # Calculate centroids
    CentroidX = np.zeros((1,int(DataX.shape[1])))
    CentroidY = np.zeros((1,int(DataX.shape[1])))
    for i in range(DataX.shape[1]):
        a = DataX[:-1,i] * DataY[1:,i] #X_i*Y_i+1
        b = DataX[1:,i] * DataY[:-1,i]  #X_i+1*Y_i
        c = DataX[:-1,i] + DataX[1:,i]
        d = DataY[:-1,i] + DataY[1:,i]
        CentroidX[:,i] = np.sum(c*(a-b))/(6*A[:,i])
        CentroidY[:,i] = np.sum(d*(a-b))/(6*A[:,i])
        
    CentroidX = CentroidX.transpose()
    CentroidY = CentroidY.transpose()
    
    CentroidX = -CentroidX
    CentroidY = -CentroidY
    
    temp = np.concatenate((CentroidX,CentroidY))  
    CentXY = pd.DataFrame(np.transpose(temp))
    
    return CentXY
    
def faultDetection(Data,CentXY,optirange, conf_int=0.95): # PARAMTER: conf_int for confidence interval
    print('in fault detection........\n')
    columnNum = CentXY.shape[1]
    ind = int(columnNum/2)
    CentX = CentXY.iloc[:,range(ind)].as_matrix(columns=None)
    CentY = CentXY.iloc[:,range(ind,ind*2)].as_matrix(columns=None)
    
    CentX = CentX.transpose()
    CentY = CentY.transpose()
    X = np.column_stack((CentX,CentY))
    Xtemp = np.column_stack((CentX[optirange],CentY[optirange]))
    Mu = np.mean(Xtemp,axis = 0)
    X0 = Xtemp-Mu
    
    print('before scale.....\n')
    scale = chi2.ppf(conf_int,2)

    Cov = np.cov(X0.transpose())*scale
    (eigenvalue, eigenvector) = linalg.eig(Cov)
    eigenvector = -eigenvector
    order = np.argsort(eigenvalue)
    eigenvalue = np.sort(eigenvalue)
    eigenvalue = np.flipud(eigenvalue) # Results in sorted, descending eigenvalues
    eigenvalueMat = np.diag(eigenvalue) # Make it into a matrix


    t = np.linspace(0,2 * np.pi, num=100)
    e = np.concatenate([np.cos(t),np.sin(t)])
    vv1 = np.matrix(eigenvector)
    vv2 = np.matrix(np.sqrt(eigenvalueMat))
    vv = np.matrix(vv1) * np.matrix(vv2)
    #vv = np.matrix(eigenvector)*np.matrix(math.sqrt(eigenvalueMat))
    vv = np.array(vv)
    #e = np.array(np.matrix(vv)*np.matrix(e))+np.array(np.matrix(Mu).transpose())


    
    # Centroids and ellipse generated, now do fault detection
    tempCents = X-Mu
    tempCentsUnit = np.array(np.matrix(linalg.inv(vv))*np.matrix(tempCents.transpose()))
    Distance = np.sqrt(np.square(tempCentsUnit[0,:])+np.square(tempCentsUnit[1,:]))
    # ^ Above is equation of circle

    print('before binaryFaultList........\n')
    binaryFaultList = np.zeros((Data.shape[0],1))
    # Check distances
    count = 1
    for i in range(Distance.shape[0]):
        if(Distance[i]>1):
            binaryFaultList[i]=1
            count += 1

    timeoffault = np.zeros((count-1,1))
    tempcount = 1
    for i in range(Distance.shape[0]):
        if(Distance[i]>1):
            timeoffault[tempcount-1] = i
            tempcount += 1
            
    print('after timeoffault......\n')
    head = 0
    tail = 0
    index = 0
    faultlist = []
    faultHead = []
    faultTail = []
    faultHeadtemp = []
    faultTailtemp = []
    numberOfVariables = Data.shape[1]
    print('before timeoffault..for i in range(timeoffault.shape[0]-1)....\n')
    print('timeoffault shape .......\n', timeoffault.shape)
    for i in range(timeoffault.shape[0]-1):
        print('after timeoffault..for i in range(timeoffault.shape[0]-1)....\n')
        if(timeoffault[i+1] == timeoffault[i]+1):
            if(head == 0):
                head = timeoffault[i]
            tail = timeoffault[i+1]
            if(i == timeoffault.shape[0]-2):
                faultHead.append(head)
                faultTail.append(tail)

        else:
            if(head != tail and head == 0):
                head = timeoffault[i]
                tail = timeoffault[i]
                faultHead.append(head)
                faultTail.append(tail)
                index = index+1
            if(head != tail):
                faultHead.append(head)
                faultTail.append(tail)
                index = index+1
            head = 0
            tail = timeoffault[i]
#    If single fault at end
    if(timeoffault[-1] != faultTail[-1]):
        print('after timeoffault..if(timeoffault[-1] != faultTail[-1]):....\n')
        faultHead.append(timeoffault[-1])
        faultTail.append(timeoffault[-1])
#    If single fault at beginning
    if(timeoffault[0] != faultHead[0]):
        faultHeadtemp.append(timeoffault[0])
        faultTailtemp.append(timeoffault[0])
        faultHeadtemp.extend(faultHead)
        faultTailtemp.extend(faultTail)
        faultHead = faultHeadtemp
        faultTail = faultTailtemp

    print('after faultTail = faultTailtemp.......\n ')
    
    newFaultHead = np.array(faultHead)
    newFaultTail = np.array(faultTail)
    
    for i in range(newFaultHead.shape[0]):
        if(newFaultTail[i]-newFaultHead[i]>numberOfVariables):
            if(newFaultHead[i] != 0 and newFaultTail[i] != 0):
                faultlist.append(str(newFaultHead[i]) + ':' + str(newFaultTail[i]))
    
    if(not faultlist):
        faultlist.append("")
    
    for i in range(len(faultlist)):
        faultlist[i] = faultlist[i].replace('[',"")
        faultlist[i] = faultlist[i].replace('.',"")
        faultlist[i] = faultlist[i].replace(']',"")
    
    if(faultlist[0] != ""):
        faultListDF = pd.DataFrame(faultlist)
    else:
        faultListDF = pd.DataFrame(np.zeros(0))
    
    if(faultlist[0] != ""):
        timeoffaultDF = pd.DataFrame(timeoffault)
    else:
        timeoffaultDF = pd.DataFrame(np.zeros(0))

    #Distance = np.concatenate((Distance,np.ones((Distance.shape[0],))),axis=1)
    DistanceDF = pd.DataFrame(Distance, columns=['Fault'])
    sLength = len(DistanceDF['Fault'])
    distLim = pd.Series(np.ones(sLength))
    DistanceDF['Limit'] = distLim

    #binaryFaultListDF = pd.DataFrame(binaryFaultList)

    print('before return in binaryFaultList')

    return faultListDF,timeoffault,timeoffaultDF,DistanceDF#binaryFaultListDF

def faultAnalysis(Data,faultListDF, num_vars_show=5): # PARAMETER: num_vars_show
    count = 0    
    varArray = list(Data.columns.values)
    topVars = []
    if(faultListDF.empty):
        print('No fault available')
    for i in range(faultListDF.shape[0]):
        topVars.append([])
        temp = faultListDF.iloc[i].str.split(":")
        tempRange = range(int(temp[0][0]),int(temp[0][1])+1)
        zscoredData = stats.zscore(Data.iloc[tempRange,:])
        PCAResults = mlab.PCA(zscoredData)
        loadings = PCAResults.Wt
        loadings = loadings.transpose()

        loadingsSquared = np.square(loadings)
        # Get eigenvalues
        covariance = np.cov(zscoredData.transpose())
        eigvalue,eigvector = linalg.eig(covariance) #eigvalue is same as latent in MATLAB
        eigvalue = np.sort(eigvalue)
        eigvalue = np.flipud(eigvalue)
    
        weighted = np.zeros((int(loadings.shape[0]),int(loadings.shape[1])))
        for j in range(int(loadings.shape[1])):
            weighted[:,j] = np.dot(loadingsSquared[:,j],eigvalue[j])
    
        variableNo = np.zeros((int(weighted.shape[0]),1))
        for k in range(int(weighted.shape[0])):
            variableNo[k] = np.sum(weighted[k,0:4]) # Get top 5 
    

        variableIndex = np.argsort(variableNo,0)
        variableIndex = np.flipud(variableIndex)
    

        varToChoose = variableIndex[0:num_vars_show]
        varToChoose = varToChoose.transpose()
        for L in range(varToChoose.shape[1]):
            topVars[i].append(varArray[varToChoose[0][L]])
        count+=1
    topVarsDF = pd.DataFrame(topVars)
    return topVarsDF 
    return Data.iloc[optirange,:],optirange