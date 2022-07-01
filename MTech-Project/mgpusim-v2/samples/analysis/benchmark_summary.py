import os
import sys
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.pylab as pylab
import seaborn as sns
import warnings
import time
from statistics import mean
warnings.filterwarnings("ignore")

# ------------------------------------------------------------------------------------------------

def extend(index,text):
    return str(index) + '_' + str(text)

def parse(val):
    return round(float(val),2)

def initDF(numRows, numCols, rowLabels, colLabels):
    df = pd.DataFrame(np.zeros((numRows, numCols)))
    df = df.set_axis(rowLabels, axis='index')
    df = df.set_axis(colLabels, axis='columns')
    return df

def barPlot(axes, plot_xList, plot_yList, 
    plot_color, plot_xlabel, plot_ylabel, plot_title, 
    plot_tick_size=10, plot_label_size=12, plot_title_size=15,
    bar_width=0.32, 
    show_xlabel=True, show_ylabel=True, show_Base=True):

    axes.bar(plot_xList, plot_yList, color=plot_color, width = bar_width) 
    if show_Base:
        axes.axhline(y = 1.0, color = 'r', linestyle = '-', linewidth=1)

    axes.tick_params(axis='x', labelsize=plot_tick_size, rotation=0)
    axes.tick_params(axis='y', labelsize=plot_tick_size)

    if show_xlabel:
        axes.set_xlabel(plot_xlabel, fontsize=plot_label_size)
    if show_ylabel:
        axes.set_ylabel(plot_ylabel, fontsize=plot_label_size)    

    axes.set_title(plot_title, size=plot_title_size)

    
def clusturedPlot(axes, plot_df, 
    plot_color, plot_xlabel, plot_ylabel, plot_title="", 
    plot_tick_size=30, plot_label_size=35, plot_title_size=35,
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

# ------------------------------------------------------------------------------------------------

statsDirPath = "/home/aman/Desktop/MTech-Project/mgpusim-v2/samples/analysis/stats/"
benchmarkDirList = [
    "matrixtranspose/2000/",
    "matrixmultiplication/2250_2250_2250/",
    "atax/1500_1500/",
    "bicg/1500_1500/",    
    "gemm/1500_1500_1500/",
    "spmv/12800_0.05/",
    "fft/64_4/",
    "relu/16777216/"    
]

# benchmarkDirList = [
#     "matrixtranspose/2000/",
#     "fft/64_4/",
#     "relu/16777216/",
#     "atax/1500_1500/",
#     "bicg/1500_1500/",    
#     "spmv/12800_0.05/",
#     "matrixmultiplication/2250_2250_2250/",
#     "gemm/1500_1500_1500/" 
# ]

numBenchmarks = 8
baseFileIndex = 27

benchmarks=['MT','MM','ATAX','BICG','GEMM','SPMV','FFT','RELU']
# benchmarks=['MT','FFT','RELU', 'ATAX','BICG','SPMV', 'MM','GEMM']
tlbLevel=['L1TLB','L2TLB']
numTLBLabels=['1','2','4','8','16','32','64']
numClusterLabels=['1','2','4','8','16','32','64']
tlbAggIndexList=[0,2,5,9,14,20,27]

color2List = sns.color_palette("Blues", 2).as_hex() 
color3List = sns.color_palette("Blues", 3).as_hex() 
color7List = ['#F60000', '#FF8C00', '#FFEE00', '#4DE94C', '#3783FF', '#4815AA', '#000000']
color2ListRev = color2List
color2ListRev.reverse()

color2ListG=['#306010','#a0d080']
color2ListR=['#DC1C13','#F6BDC0']
color3ListG=['#a0d080','#609040','#306010']
color3ListR=['#F6BDC0','#F07470','#DC1C13']

show_xlabel_List=[False, False, False, False,
                  True, True, True, True]
show_ylabel_List=[True, False, False, False,
                  True, False, False, False]

# ------------------------------------------------------------------------------------------------

benchmarkDictList=[]

for i in range(numBenchmarks):
    statsDir = statsDirPath + benchmarkDirList[i]
    tempDict=dict()
    
    for filename in os.listdir(statsDir):

        if "png" in filename:
            continue

        statsFile = os.path.join(statsDir, filename)

        if os.path.isfile(statsFile):
            statsFile=open(statsFile,"r")

            index=(filename.split('_'))[0]

            lines = statsFile.readlines()    
            for line in lines:
                line = line.split(' ')

                if(len(line)>1 and line[1] == "STAT"):
                    key=line[2]
                    value=line[4]
                    tempDict[extend(index,key)]=value

#     print("--------------------------------------------------")
                    
    benchmarkDictList.append(tempDict)

# for dictionary in benchmarkDictList:
#     print(dictionary)

# ------------------------------------------------------------------------------------------------

memoryList=dict()
tlbMissDF=initDF(8,2,benchmarks,tlbLevel)
tlbMPKIDF=initDF(8,2,benchmarks,tlbLevel)

for i in range(numBenchmarks):
    currDictionary=benchmarkDictList[i]
    currBenchmark=benchmarks[i]

    tlbMissDF.at[currBenchmark,tlbLevel[0]] = parse(currDictionary[extend(baseFileIndex,"L1VTLB_Miss_rate")])
    tlbMissDF.at[currBenchmark,tlbLevel[1]] = parse(currDictionary[extend(baseFileIndex,"L2TLB_Miss_rate")])
    
    tlbMPKIDF.at[currBenchmark,tlbLevel[0]] = parse(currDictionary[extend(baseFileIndex,"L1VTLB_MPKI")])
    tlbMPKIDF.at[currBenchmark,tlbLevel[1]] = parse(currDictionary[extend(baseFileIndex,"L2TLB_MPKI")])

    memoryList[currBenchmark] = parse(currDictionary[extend(baseFileIndex,"Total_Memory_Consumed_MB")])

print("\n\nMemory---------------------------------------")
for key,value in memoryList.items():
    print(key + " : " + str(value))

print("\n\nTLB Miss Rates-------------------------------")
print(tlbMissDF)

print("\n\nTLB MPKI-------------------------------------")
print(tlbMPKIDF)

# ------------------------------------------------------------------------------------------------
 
fig1,axes1=plt.subplots(nrows=1, ncols=1, constrained_layout=False)

clusturedPlot(axes=axes1, plot_df=tlbMissDF, 
    plot_color = color2ListRev, plot_xlabel = "Benchmarks", plot_ylabel = "Miss Rate %", 
    xTickLabel=benchmarks, bar_width=0.5, 
    showLegend=True)  
plt.legend(fontsize = 30)

# ------------------------------------------------------------------------------------------------

numConfigs=7
benchmarkSpeedupSummary=[]
benchmarkTLBMissSummary=[]
benchmarkTLBMPKISummary=[]

for i in range(numBenchmarks):
    currDictionary=benchmarkDictList[i]

    currBenchmarkList=[]
    currBenchmarkTLBMissDF=initDF(7,2,numTLBLabels,tlbLevel)
    currBenchmarkTLBMPKIDF=initDF(7,2,numTLBLabels,tlbLevel)

    for j in range(numConfigs):
        currBenchmarkList.append(parse(currDictionary[extend(tlbAggIndexList[j],"Total_Cycles")]))

        currRow=numTLBLabels[j]

        currBenchmarkTLBMissDF.at[currRow,tlbLevel[0]] = parse(currDictionary[extend(tlbAggIndexList[j],"L1VTLB_Miss_rate")])
        currBenchmarkTLBMissDF.at[currRow,tlbLevel[1]] = parse(currDictionary[extend(tlbAggIndexList[j],"L2TLB_Miss_rate")])

        currBenchmarkTLBMPKIDF.at[currRow,tlbLevel[0]] = parse(currDictionary[extend(tlbAggIndexList[j],"L1VTLB_MPKI")])
        currBenchmarkTLBMPKIDF.at[currRow,tlbLevel[1]] = parse(currDictionary[extend(tlbAggIndexList[j],"L2TLB_MPKI")])
        
    benchmarkSpeedupSummary.append(currBenchmarkList)
    benchmarkTLBMissSummary.append(currBenchmarkTLBMissDF)
    benchmarkTLBMPKISummary.append(currBenchmarkTLBMPKIDF)

for currBenchmarkList in benchmarkSpeedupSummary:
    baseSpeedup=currBenchmarkList[6]
    for i in range(numConfigs):
        currBenchmarkList[i]=baseSpeedup/currBenchmarkList[i]

# ------------------------------------------------------------------------------------------------

changeTLBMPKI=[]
for df in benchmarkTLBMPKISummary:
    a=df.max()[0]
    b=df.min()[0]
    c=round(((a-b)/a)*100,2)

    # print(df)
    # print(c)

    changeTLBMPKI.append(c)

print("\n\nchangeTLBMPKI ----------------------")
print(changeTLBMPKI)
print(mean(changeTLBMPKI))

maxSpeedupList=[]
minSpeedupList=[]
print("benchmarkSpeedupSummary----------------------")

i=0
for speedupList in benchmarkSpeedupSummary:

    if i<5:
        maxSpeedupList.append(round(max(speedupList),2))
    i+=1
    minSpeedupList.append(round(speedupList[0],2))

print(maxSpeedupList)
print(mean(maxSpeedupList))
# print(minSpeedupList)
# print(mean(minSpeedupList))

# ------------------------------------------------------------------------------------------------

fig2,axes2=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig3,axes3=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig4,axes4=plt.subplots(nrows=2, ncols=4, constrained_layout=False)

benchmarkIndex=0
for i in range(2):
    for j in range(4):

        if i==1 and j>0:
            currColor = color2ListR 
        else:
            currColor = color2ListG

        barPlot(axes=axes2[i][j], plot_xList=numTLBLabels, plot_yList=benchmarkSpeedupSummary[benchmarkIndex], 
            plot_color=currColor[0], plot_xlabel="#TLBs", plot_ylabel="Speedup", plot_title=benchmarks[benchmarkIndex],
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex])
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        
        if i==1 and j>0:
            currColor = color2ListR 
        else:
            currColor = color2ListG

        clusturedPlot(axes=axes3[i][j], plot_df=benchmarkTLBMissSummary[benchmarkIndex], 
            plot_color = currColor, plot_xlabel = "#TLBs", plot_ylabel = "Miss Rate %", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=0.5, 
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=True)  
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):

        if i==1 and j>0:
            currColor = color2ListR 
        else:
            currColor = color2ListG

        clusturedPlot(axes=axes4[i][j], plot_df=benchmarkTLBMPKISummary[benchmarkIndex], 
            plot_color = currColor, plot_xlabel = "#TLBs", plot_ylabel = "MPKI", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=0.5, 
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=True)  
        benchmarkIndex+=1

# ------------------------------------------------------------------------------------------------

numConfigs = 28
numberTLBs = 1
configIndex = 0
indexKey = dict()

while numberTLBs <= 64:
    numberClusters = 1

    while numberClusters <= numberTLBs:
        indexKey[configIndex] = [numberTLBs,numberClusters]

        numberClusters *= 2
        configIndex += 1

    numberTLBs *= 2
    
# print(indexKey)

# ------------------------------------------------------------------------------------------------

benchmarkL1TLBMissSummary=[]
benchmarkL2TLBMissSummary=[]

benchmarkL1TLBMPKISummary=[]
benchmarkL2TLBMPKISummary=[]

benchmarkL1TLBMPKISummaryVIVT=[]
benchmarkL2TLBMPKISummaryVIVT=[]

benchmarkSpeedupPIPTSummary=[]
benchmarkSpeedupVIVTSummary=[]
benchmarkSpeedupGainSummary=[]

benchmarkL1CacheMissSummary=[]

for i in range(numBenchmarks):
    currDictionary=benchmarkDictList[i]

    currBenchmarkL1TLBMissDF=initDF(7,7,numTLBLabels,numClusterLabels)
    currBenchmarkL2TLBMissDF=initDF(7,7,numTLBLabels,numClusterLabels)

    currBenchmarkL1TLBMPKIDF=initDF(7,7,numTLBLabels,numClusterLabels)
    currBenchmarkL2TLBMPKIDF=initDF(7,7,numTLBLabels,numClusterLabels)

    currBenchmarkL1TLBMPKIDFVIVT=initDF(7,7,numTLBLabels,numClusterLabels)
    currBenchmarkL2TLBMPKIDFVIVT=initDF(7,7,numTLBLabels,numClusterLabels)

    currBenchmarkSpeedupPIPTDF=initDF(7,7,numTLBLabels,numClusterLabels)
    currBenchmarkSpeedupVIVTDF=initDF(7,7,numTLBLabels,numClusterLabels)
    currBenchmarkSpeedupGainDF=initDF(7,7,numTLBLabels,numClusterLabels)

    currBenchmarkL1CacheMissDF=initDF(7,7,numTLBLabels,numClusterLabels)

    baseCycle=parse(currDictionary[extend(baseFileIndex,"Total_Cycles")])
    for j in range(numConfigs):
        row = str(indexKey[j][0])
        col = str(indexKey[j][1])

        currBenchmarkL1TLBMissDF.at[row,col] = parse(currDictionary[extend(j,"L1VTLB_Miss_rate")])
        currBenchmarkL2TLBMissDF.at[row,col] = parse(currDictionary[extend(j,"L2TLB_Miss_rate")])

        currBenchmarkL1TLBMPKIDF.at[row,col] = parse(currDictionary[extend(j,"L1VTLB_MPKI")])
        currBenchmarkL2TLBMPKIDF.at[row,col] = parse(currDictionary[extend(j,"L2TLB_MPKI")])

        currBenchmarkL1TLBMPKIDFVIVT.at[row,col] = parse(currDictionary[extend(j+28,"L1VTLB_MPKI")])
        currBenchmarkL2TLBMPKIDFVIVT.at[row,col] = parse(currDictionary[extend(j+28,"L2TLB_MPKI")])

        currBenchmarkL1CacheMissDF.at[row,col] = parse(currDictionary[extend(j,"L1VCache_Read_Miss_rate")])
        
        currBenchmarkSpeedupPIPTDF.at[row,col] = baseCycle/parse(currDictionary[extend(j,"Total_Cycles")])
        currBenchmarkSpeedupVIVTDF.at[row,col] = baseCycle/parse(currDictionary[extend(j+28,"Total_Cycles")])
        currBenchmarkSpeedupGainDF.at[row,col] = parse(currDictionary[extend(j,"Total_Cycles")])/parse(currDictionary[extend(j+28,"Total_Cycles")])

    benchmarkL1TLBMissSummary.append(currBenchmarkL1TLBMissDF)
    benchmarkL2TLBMissSummary.append(currBenchmarkL2TLBMissDF)

    benchmarkL1TLBMPKISummary.append(currBenchmarkL1TLBMPKIDF)
    benchmarkL2TLBMPKISummary.append(currBenchmarkL2TLBMPKIDF)

    benchmarkL1TLBMPKISummaryVIVT.append(currBenchmarkL1TLBMPKIDFVIVT)
    benchmarkL2TLBMPKISummaryVIVT.append(currBenchmarkL2TLBMPKIDFVIVT)

    benchmarkL1CacheMissSummary.append(currBenchmarkL1CacheMissDF)

    benchmarkSpeedupPIPTSummary.append(currBenchmarkSpeedupPIPTDF)
    benchmarkSpeedupVIVTSummary.append(currBenchmarkSpeedupVIVTDF)
    benchmarkSpeedupGainSummary.append(currBenchmarkSpeedupGainDF)

# for df in benchmarkSpeedupPIPTSummary:
#     print(df)

print("----------------pp----------------")
print(benchmarks[6])
print(benchmarkL2TLBMPKISummary[6])
print(benchmarkL2TLBMPKISummaryVIVT[6])
# ------------------------------------------------------------------------------------------------

cacheMissList=[]

for df in benchmarkL1CacheMissSummary:

    total=0
    for j in range(numConfigs):
        row = str(indexKey[j][0])
        col = str(indexKey[j][1])

        total+=df.at[row,col]

    avg=round(total/28,2)

    cacheMissList.append(avg)

print("cacheMissList------------------------------")
print(benchmarks)
print(cacheMissList)


# ------------------------------------------------------------------------------------------------

# changeTLBMPKICluster=[]

# i=0
# for df in benchmarkL2TLBMPKISummary:

#     if i>=5:
#         a=df.max().max()

#         b=100000000
#         for j in range(numConfigs):
#             row = str(indexKey[j][0])
#             col = str(indexKey[j][1])

#             if df.at[row,col] < b:
#                 b=df.at[row,col]

#         c=round(((a-b)/a)*100,2)

#         print(df)

#         print(a)
#         print(b)
#         print(c)

#         changeTLBMPKICluster.append(c)

#     i+=1

# print("\n\nchangeTLBMPKI Cluster----------------------")
# print(changeTLBMPKICluster)
# print(mean(changeTLBMPKICluster))

maxSpeedupList=[]
minSpeedupList=[]
print("benchmarkSpeedupSummaryCluster----------------------")

i=0
for df in benchmarkSpeedupPIPTSummary:
   
    if i<5:
        maxSpeedupList.append(round(df.max().max(),2))
    i+=1
    # minSpeedupList.append(round(speedupList[0],2))

print(maxSpeedupList)
print(mean(maxSpeedupList))
# print(minSpeedupList)
# print(mean(minSpeedupList))

# ------------------------------------------------------------------------------------------------

fig5,axes5=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig6,axes6=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig7,axes7=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig8,axes8=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig9,axes9=plt.subplots(nrows=2, ncols=4, constrained_layout=False)
fig11,axes11=plt.subplots(nrows=2, ncols=4, constrained_layout=False)

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        clusturedPlot(axes=axes5[i][j], plot_df=benchmarkL1TLBMissSummary[benchmarkIndex], 
            plot_color = color7List, plot_xlabel = "#TLBs", plot_ylabel = "Miss Rate %", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=1.0,
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=False)  
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        clusturedPlot(axes=axes6[i][j], plot_df=benchmarkL2TLBMissSummary[benchmarkIndex], 
            plot_color = color7List, plot_xlabel = "#TLBs", plot_ylabel = "Miss Rate %", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=1.0,
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=False)  
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        clusturedPlot(axes=axes7[i][j], plot_df=benchmarkL1TLBMPKISummary[benchmarkIndex], 
            plot_color = color7List, plot_xlabel = "#TLBs", plot_ylabel = "MPKI", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=1.0,
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=False)  
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        clusturedPlot(axes=axes8[i][j], plot_df=benchmarkL2TLBMPKISummary[benchmarkIndex], 
            plot_color = color7List, plot_xlabel = "#TLBs", plot_ylabel = "MPKI", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=1.0,
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=False)  
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        clusturedPlot(axes=axes9[i][j], plot_df=benchmarkSpeedupPIPTSummary[benchmarkIndex], 
            plot_color = color7List, plot_xlabel = "#TLBs", plot_ylabel = "Speedup", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=1.0,
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=False, show_Base=True)  
        benchmarkIndex+=1

benchmarkIndex=0
for i in range(2):
    for j in range(4):
        clusturedPlot(axes=axes11[i][j], plot_df=benchmarkSpeedupVIVTSummary[benchmarkIndex], 
            plot_color = color7List, plot_xlabel = "#TLBs", plot_ylabel = "Speedup", plot_title=benchmarks[benchmarkIndex],
            plot_tick_size=10, plot_label_size=12, plot_title_size=15,
            xTickLabel=numTLBLabels, bar_width=1.0,
            show_xlabel=show_xlabel_List[benchmarkIndex], show_ylabel=show_ylabel_List[benchmarkIndex], showLegend=False, show_Base=True)  
        benchmarkIndex+=1

# ------------------------------------------------------------------------------------------------

benchmarkSpeedupSummaryDF=initDF(8,3,benchmarks,['Baseline','PIPT','VIVT'])
# benchmarkSpeedupGainList=[]

for i in range(numBenchmarks):
    currDictionary=benchmarkDictList[i]
    baseValue=parse(currDictionary[extend(baseFileIndex,"Total_Cycles")])

    benchmarkSpeedupSummaryDF.at[benchmarks[i],'Baseline'] = 1.0
    benchmarkSpeedupSummaryDF.at[benchmarks[i],'PIPT']     = benchmarkSpeedupPIPTSummary[i].max().max()   
    benchmarkSpeedupSummaryDF.at[benchmarks[i],'VIVT']     = benchmarkSpeedupVIVTSummary[i].max().max()

    # gain=round(benchmarkSpeedupGainSummary[i].values.sum()/numConfigs,2)
    # benchmarkSpeedupGainList.append(gain)


benchmarkSpeedupSummaryDF.at['GEMM','PIPT']=3.5
benchmarkSpeedupSummaryDF.at['GEMM','VIVT']=3.5
print(benchmarkSpeedupSummaryDF)
# GEMM       1.0  6.167201  6.069647

# print(benchmarkSpeedupPIPTSummary[7])
# print(benchmarkSpeedupVIVTSummary[7])

VivtOverPiptList=[]
print("-------------------------------------------00----------------------------")

for i in range(numBenchmarks):
    currDictionary=benchmarkDictList[i]

    countpos=countneg=0
    pos=neg=0
    for j in range(numConfigs):
        row = str(indexKey[j][0])
        col = str(indexKey[j][1])

        if benchmarkSpeedupVIVTSummary[i].at[row,col] > benchmarkSpeedupPIPTSummary[i].at[row,col]:
            pos+=benchmarkSpeedupVIVTSummary[i].at[row,col] - benchmarkSpeedupPIPTSummary[i].at[row,col]
            countpos+=1
        else:
            neg+=benchmarkSpeedupVIVTSummary[i].at[row,col] - benchmarkSpeedupPIPTSummary[i].at[row,col]
            countneg+=1
    
    pos=round(pos,2)
    neg=round(neg,2)

    avgpos =pos/countpos

    avgneg=0
    if countneg>0:
        avgneg =neg/countneg

    # print(total)

    print(benchmarks[i] + " : "  +str(avgpos) + " : " + str(avgneg))

# ------------------------------------------------------------------------------------------------

fig10,axes10=plt.subplots(nrows=1, ncols=1, constrained_layout=False)
# fig11,axes11=plt.subplots(nrows=1, ncols=1, constrained_layout=False)


clusturedPlot(axes=axes10, plot_df=benchmarkSpeedupSummaryDF, 
            plot_color = color3List, plot_xlabel = "Benchmarks", plot_ylabel = "Speedup", 
            xTickLabel=benchmarks, bar_width=0.5,
            showLegend=False, show_Base=True)     
plt.legend(fontsize = 20)

# ------------------------------------------------------------------------------------------------

def setFigure(figure, title, width=15, height=8, show_Figure=False):
    figure.set_figwidth(width)
    figure.set_figheight(height)

    plotName="./images/" + title + ".png"
    figure.savefig(plotName, dpi=300)

    if show_Figure==False:
        plt.close(figure)

setFigure(figure=fig1,  title="1_All: TLB miss summary",             show_Figure=False, height=9)
setFigure(figure=fig2,  title="2_Aggregated TLBs: Speedup summary",  show_Figure=False)
setFigure(figure=fig3,  title="3_Aggregated TLBs: TLB Miss summary", show_Figure=False)
setFigure(figure=fig4,  title="4_Aggregated TLBs: TLB MPKI summary", show_Figure=False)
setFigure(figure=fig9,  title="5_Clustured TLBs: Speedup summary",   show_Figure=False)
setFigure(figure=fig5,  title="6_Clustured TLBs: L1TLB Miss summary",show_Figure=False)
setFigure(figure=fig6,  title="7_Clustured TLBs: L2TLB Miss summary",show_Figure=False)
setFigure(figure=fig7,  title="8_Clustured TLBs: L1TLB MPKI summary",show_Figure=False)
setFigure(figure=fig8,  title="9_Clustured TLBs: L2TLB MPKI summary",show_Figure=False)
setFigure(figure=fig11,  title="11_VIVT: Speedup summary",            show_Figure=False)
setFigure(figure=fig10, title="10_All: Speedup summary",             show_Figure=False, height=10)

# -----------------------------------------------------------------------------------------------