import os
import sys
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import warnings
import time
warnings.filterwarnings("ignore")

# ------------------------------------------------------------------------------------------------

def extend(index,text):
    return str(index) + '_' + str(text)

def parse(val):
    return round(float(val),2)

def printDict(dictionary):
    for key,value in dictionary.items():    
        print(key + ' ' + value)

# ------------------------------------------------------------------------------------------------

benchmark = sys.argv[1]
statsDir="./stats/" + benchmark
data = dict()

index=0
for filename in os.listdir(statsDir):

    if "png" in filename:
        continue

    file = os.path.join(statsDir, filename)

    if os.path.isfile(file):
        # print(file) 
        statsFile=open(file,"r")

        # print(filename)

        index=(filename.split('_'))[0]
        # index=(filename.split('_'))[2] #for MT
        # print(index)
  
        lines = statsFile.readlines()    
        for line in lines:
            line = line.split(' ')
            # print(line)

            if(len(line)>1 and line[1] == "STAT"):
                key=line[2]
                value=line[4]
                data[extend(index,key)]=value

    # print("-------------------------------------------------")

# printDict(data)

graphName = (benchmark.split('/'))[0]
# print(graphName)

# ------------------------------------------------------------------------------------------------

numConfigs = 28

indexKey = dict()
numberTLBs = 1
configIndex = 0

while numberTLBs <= 64:
    numberClusters = 1

    while numberClusters <= numberTLBs:
        # print(str(configIndex) + " : " + str(numberClusters) + " : " + str(numberClusters))
        indexKey[configIndex] = [numberTLBs,numberClusters]

        numberClusters *= 2
        configIndex += 1

    numberTLBs *= 2

# printDict(indexKey)

# ------------------------------------------------------------------------------------------------

numTLBs = ['1','2','4','8','16','32','64']

def initDF():
    temp = pd.DataFrame(np.zeros((7, 7)))
    temp = temp.set_axis(numTLBs, axis='index')
    temp = temp.set_axis(numTLBs, axis='columns')
    return temp

def initDF2():
    temp = pd.DataFrame(np.zeros((7, 14)))
    temp = temp.set_axis(numTLBs, axis='index')
    temp = temp.set_axis(['1_0','1_1','2_0','2_1','4_0','4_1','8_0','8_1','16_0','16_1','32_0','32_1','64_0','64_1'], axis='columns')
    return temp

# ------------------------------------------------------------------------------------------------

l1MissRateDF     = initDF()
l2MissRateDF     = initDF()

l1MPKIDF         = initDF()
l2MPKIDF         = initDF()

l1ColdMissDF     = initDF()
l1ConflictMissDF = initDF()
l1CapacityMissDF = initDF()

l2ColdMissDF     = initDF()
l2ConflictMissDF = initDF()
l2CapacityMissDF = initDF()

l1HitDF          = initDF()
l1MissDF         = initDF()
l1MSHRHitDF      = initDF()

l2HitDF          = initDF()
l2MissDF         = initDF()
l2MSHRHitDF      = initDF()

cycleDF          = initDF2()

l1TLBMissRateDF    = initDF2()
l2TLBMissRateDF    = initDF2()

l1CacheReadMissRateDF  = initDF2()
l2CacheReadMissRateDF  = initDF2()

l1CacheWriteMissRateDF = initDF2()
l2CacheWriteMissRateDF = initDF2()

memory=parse(data[extend(0,"Total_Memory_Consumed_MB")])

# ------------------------------------------------------------------------------------------------

for i in range(numConfigs):
    row = str(indexKey[i][0])
    col = str(indexKey[i][1])
    # print(str(row) + " : " + str(col))

    l1MissRateDF.at[row,col]     = parse(data[extend(i,"L1VTLB_Miss_rate")])
    l2MissRateDF.at[row,col]     = parse(data[extend(i,"L2TLB_Miss_rate")])

    l1MPKIDF.at[row,col]         = parse(data[extend(i,"L1VTLB_MPKI")])
    l2MPKIDF.at[row,col]         = parse(data[extend(i,"L2TLB_MPKI")])

    l1ColdMissDF.at[row,col]     = parse(data[extend(i,"L1_Cold_Miss"    )])
    l1ConflictMissDF.at[row,col] = parse(data[extend(i,"L1_Conflict_Miss")])
    l1CapacityMissDF.at[row,col] = parse(data[extend(i,"L1_Capacity_Miss")])

    l2ColdMissDF.at[row,col]     = parse(data[extend(i,"L2_Cold_Miss"    )])
    l2ConflictMissDF.at[row,col] = parse(data[extend(i,"L2_Conflict_Miss")])
    l2CapacityMissDF.at[row,col] = parse(data[extend(i,"L2_Capacity_Miss")])

    l1HitDF.at[row,col]          = parse(data[extend(i,"L1_Hit"     )])
    l1MissDF.at[row,col]         = parse(data[extend(i,"L1_Miss"    )])
    l1MSHRHitDF.at[row,col]      = parse(data[extend(i,"L1_MSHR_Hit")])

    l2HitDF.at[row,col]          = parse(data[extend(i,"L2_Hit"     )])
    l2MissDF.at[row,col]         = parse(data[extend(i,"L2_Miss"    )])
    l2MSHRHitDF.at[row,col]      = parse(data[extend(i,"L2_MSHR_Hit")])

list = ['1_0','1_1','2_0','2_1']#,'4_0','4_1']#,'8_0','8_1']

baseCycle=parse(data[extend(27,"Total_Cycles")])

for i in range(56):
    if(i<28):
        row = str(indexKey[i][0])
        col = str(indexKey[i][1]) + '_0'
    else:
        row = str(indexKey[i-28][0])
        col = str(indexKey[i-28][1]) + '_1'

    # if(col in list):
    #     continue
    # print(str(row) + ": " + str(col))
    
    cycleDF.at[row,col]                = baseCycle/parse(data[extend(i,"Total_Cycles")])

    l1TLBMissRateDF.at[row,col]        = parse(data[extend(i,"L1_Top_Port_Messages")])
    l2TLBMissRateDF.at[row,col]        = parse(data[extend(i,"L1_Miss")])

    l1CacheReadMissRateDF.at[row,col]  = parse(data[extend(i,"L1VCache_Read_Miss_rate")])
    l2CacheReadMissRateDF.at[row,col]  = parse(data[extend(i,"L2Cache_Read_Miss_rate")])

    l1CacheWriteMissRateDF.at[row,col] = parse(data[extend(i,"L1VTLB_MPKI")])
    l2CacheWriteMissRateDF.at[row,col] = parse(data[extend(i,"L2TLB_MPKI")])

# --------------------------------------------------------------------------------------------------

def makeStackedDF(colNames, dfList):
    numCols = len(colNames)
    stackDF = []

    for i in range(7):
        tempDF=pd.DataFrame(np.random.rand(7, numCols), index=numTLBs, columns=colNames)
        col=numTLBs[i]   

        for j in range(7):
            row=numTLBs[j]

            for k in range(numCols):
                tempDF.at[row,colNames[k]] = (dfList[k]).at[row,col]

        stackDF.append(tempDF)

    return stackDF


l1MissStackDF = makeStackedDF(["Cold", "Conflict", "Capacity"],[l1ColdMissDF, l1ConflictMissDF, l1CapacityMissDF] )
l2MissStackDF = makeStackedDF(["Cold", "Conflict", "Capacity"],[l2ColdMissDF, l2ConflictMissDF, l2CapacityMissDF] )

l1AccessStackDF = makeStackedDF(["Hit", "Miss", "MSHR_Hit"],[l1HitDF, l1MissDF, l1MSHRHitDF] )
l2AccessStackDF = makeStackedDF(["Hit", "Miss", "MSHR_Hit"],[l2HitDF, l2MissDF, l2MSHRHitDF] )


# --------------------------------------------------------------------------------------------------

def clusturedPlot(df, axes, xlabel, ylabel, color, title):
    df.plot(ax=axes, kind="bar", linewidth=0.5, edgecolor="black", width = 1, color=color)
    axes.set_xticklabels(numTLBs, rotation = 0)
    axes.set_xlabel(xlabel)
    axes.set_ylabel(ylabel)
    axes.set_title(title, size=10)
    axes.legend(title='#Clusters')

def clusteredStackedPlot(dfList, axe, xlabel, ylabel, color, title):
    n_df = len(dfList)
    n_col = len(dfList[0].columns) 
    n_ind = len(dfList[0].index)

    for df in dfList : 
        axe = df.plot(ax=axe, kind="bar", linewidth=0.5, edgecolor="black", width=0.8, stacked=True, color=color, legend=False, grid=False)

    h,l = axe.get_legend_handles_labels() # get the handles we want to modify
    for i in range(0, n_df * n_col, n_col): # len(h) = n_col * n_df
        for j, pa in enumerate(h[i:i+n_col]):
            for rect in pa.patches: # for each index
                rect.set_x(rect.get_x() + 1 / float(n_df + 1) * i / float(n_col))
                rect.set_width(1 / float(n_df + 1))

    axe.set_xticks((np.arange(0, 2 * n_ind, 2) + 1 / float(n_df + 1)) / 2.)
    axe.set_xticklabels(df.index, rotation = 0)
    axe.legend(h[:n_col], l[:n_col])
    axe.set_xlabel(xlabel)
    axe.set_ylabel(ylabel)
    axe.set_title(title, size=10)


colorList0 = ['#F60000', '#FF8C00', '#FFEE00', '#4DE94C', '#3783FF', '#4815AA', '#000000']
colorList1 = ['#F60000', '#FFFFFF', '#1E90FF']
colorList2 = ['#4DE94C', '#F60000', '#FFEE00']
# colorList3 = sns.color_palette("rocket", 8).as_hex()  
colorList4 = ['#F60000', '#000000', '#FF8C00', '#000000', '#FFEE00', '#000000', '#4DE94C', '#000000', '#3783FF', '#000000', '#4815AA',  '#000000', '#D3D3D3', '#000000',]

import matplotlib.pylab as pylab
params = {'legend.fontsize': 'x-large',
          'figure.figsize': (15, 5),
         'axes.labelsize': 'xx-large',
         'axes.titlesize':'xx-large',
         'xtick.labelsize':'xx-large',
         'ytick.labelsize':'xx-large'}
pylab.rcParams.update(params)

# --------------------------------------------------------------------------------------------------

# fig1,axes1=plt.subplots(nrows=2, ncols=4)

# clusturedPlot(l1MissRateDF, axes1[0][0], xlabel = "#TLBs", ylabel = "L1TLB Miss Rate %", color = colorList0, title = "L1TLB Miss Rate %")     
# clusturedPlot(l2MissRateDF, axes1[1][0], xlabel = "#TLBs", ylabel = "L2TLB Miss Rate %", color = colorList0, title = "L2TLB Miss Rate %")     
# clusturedPlot(l1MPKIDF    , axes1[0][1], xlabel = "#TLBs", ylabel = "L1TLB MPKI"     , color = colorList0, title = "L1TLB MPKI"     )     
# clusturedPlot(l2MPKIDF    , axes1[1][1], xlabel = "#TLBs", ylabel = "L2TLB MPKI"     , color = colorList0, title = "L2TLB MPKI"     )     

# clusteredStackedPlot(l1MissStackDF  , axes1[0][2], xlabel = "#TLBs", ylabel = "#L1TLB Misses"  , color = colorList1, title = "L1 Miss Distribution"  )
# clusteredStackedPlot(l2MissStackDF  , axes1[1][2], xlabel = "#TLBs", ylabel = "#L2TLB Misses"  , color = colorList1, title = "L2 Miss Distribution"  )
# clusteredStackedPlot(l1AccessStackDF, axes1[0][3], xlabel = "#TLBs", ylabel = "#L1TLB Accesses", color = colorList2, title = "L1 Access Distribution")
# clusteredStackedPlot(l2AccessStackDF, axes1[1][3], xlabel = "#TLBs", ylabel = "#L2TLB Accesses", color = colorList2, title = "L2 Access Distribution")

# axes1[1,0].get_legend().remove()
# axes1[1,1].get_legend().remove()

# fig1.set_figwidth(15)
# fig1.set_figheight(10)
# fig1.suptitle(graphName+' \nMemory consumed: '+str(memory)+" MB", fontsize=12)
# fig1.tight_layout()


# plotName=statsDir + "/a.png"
# plt.savefig(plotName)

# --------------------------------------------------------------------------------------------------

def clusturedPlot2(axes, plot_df, 
    plot_color, plot_xlabel, plot_ylabel, plot_title="", 
    plot_tick_size=30, plot_label_size=60, plot_title_size=40,
    xTickLabel=[], bar_width=0.4,  
    show_xlabel=True, show_ylabel=True, show_Base=False, showLegend=True):

    plot_df.plot(ax=axes, kind="bar", linewidth=0.5, edgecolor="black", width=bar_width, color=plot_color)
    if show_Base:
        axes.axhline(y = 1.0, color = 'r', linestyle = '-', linewidth=1)
    if showLegend == False:
        axes.get_legend().remove()
    
    axes.set_xticklabels(xTickLabel, rotation=0)

    axes.tick_params(axis='x', labelsize=plot_tick_size)
    axes.tick_params(axis='y', labelsize=plot_tick_size)

    if show_xlabel:
        axes.set_xlabel(plot_xlabel, fontsize=plot_label_size)
    if show_ylabel:
        axes.set_ylabel(plot_ylabel, fontsize=plot_label_size)   

    axes.set_title(plot_title, size=plot_title_size)


fig2,axes2=plt.subplots(nrows=1, ncols=1)

# clusturedPlot2(axes=axes2, plot_df=cycleDF, 
#             plot_color = colorList4, plot_xlabel = "#TLBs", plot_ylabel = "Speedup", plot_title="",
#             plot_tick_size=10, plot_label_size=12, plot_title_size=15,
#             xTickLabel=numTLBs, bar_width=1.0,
#             show_xlabel=True, show_ylabel=True, showLegend=False)  

clusturedPlot(cycleDF, axes2, xlabel = "#TLBs", ylabel = "Speedup", color = colorList4, title="")   
axes2.tick_params(axis='x', labelsize=30)
axes2.tick_params(axis='y', labelsize=30)
axes2.set_xlabel("#TLBs", fontsize=40)
axes2.set_ylabel("Speedup", fontsize=40)
plt.axhline(y = 1.0, color = 'r', linestyle = '-', linewidth=2)
fig2.set_figwidth(13)
fig2.set_figheight(10)
# fig2.suptitle(graphName+' \nMemory consumed: '+str(memory)+" MB", fontsize=12)
fig2.tight_layout()
axes2.get_legend().remove()

plotName=statsDir + "/b.png"
plt.savefig(plotName)

# # --------------------------------------------------------------------------------------------------

# fig3,axes3=plt.subplots(nrows=2, ncols=3)

# clusturedPlot(l1TLBMissRateDF,       axes3[0][0], xlabel = "#TLBs", ylabel = "L1TLB Miss Rate %",     color = colorList4, title = "L1TLB Miss Rate %")     
# clusturedPlot(l2TLBMissRateDF,       axes3[1][0], xlabel = "#TLBs", ylabel = "L2TLB Miss Rate %",     color = colorList4, title = "L2TLB Miss Rate %")     
# clusturedPlot(l1CacheReadMissRateDF, axes3[0][1], xlabel = "#TLBs", ylabel = "L1$ Read Miss Rate %",  color = colorList4, title = "L1$ Read Miss Rate %")     
# clusturedPlot(l2CacheReadMissRateDF, axes3[1][1], xlabel = "#TLBs", ylabel = "L2$ Read Miss Rate %",  color = colorList4, title = "L2$ Read Miss Rate %")     
# clusturedPlot(l1CacheWriteMissRateDF, axes3[0][2], xlabel = "#TLBs", ylabel = "L1$ Write Miss Rate %", color = colorList4, title = "L1$ Write Miss Rate %")     
# clusturedPlot(l2CacheWriteMissRateDF, axes3[1][2], xlabel = "#TLBs", ylabel = "L2$ Write Miss Rate %", color = colorList4, title = "L2$ Write Miss Rate %")   

# axes3[0,0].get_legend().remove()
# axes3[0,1].get_legend().remove()
# axes3[0,2].get_legend().remove()
# axes3[1,0].get_legend().remove()
# axes3[1,1].get_legend().remove()
# axes3[1,2].get_legend().remove()

# fig3.set_figwidth(15)
# fig3.set_figheight(10)
# fig3.suptitle(graphName+' \nMemory consumed: '+str(memory)+" MB", fontsize=12)
# fig3.tight_layout()

# plotName=statsDir + "/c.png"
# plt.savefig(plotName)

# # --------------------------------------------------------------------------------------------------

plt.show()

