#
# Title:
# ----------------------------------------------
# Obtaining CPI Stack for Programs using Hardware Performance Counters and Linear Regression
#
# Author:
# ----------------------------------------------
# 1) Aman Choudhary ...amanc@iisc.ac.in
# 2) Shashank Singh ...shashanksing@iisc.ac.in
#


# Include Libraries ------------------------------

library(reshape2)
library(nnls)
library(data.table)
library(BBmisc)
library(ggplot2)  
library(glmnet)
library(dplyr)
library(gridExtra)



# Read file ---------------------------------------

# Set the working directly appropriately
setwd("/home/aman/Desktop/Project/Dataset")
print(getwd())


file = 
  
#File Name________________Observed Neg Coeff.
  
"i11_xz_r.csv"          #L2 Miss neg                    
# "i21_deepsjeng_r.csv"   #DTLB Miss Neg, L1 Miss Neg
# "i41_leela_r.csv"       #ALL +VE
# "f21_blender_r.csv"     #Icache, itlb, L2 neg
# "f31_cactuBSSN_r.csv"   #ITLB.Miss neg
# "f41_povray_r.csv"      #All +VE

# NOTE:
# We observed a few negative coefficients for some datasets. 
# In order to deal with this we excluded them from training & test data set.
# Moreover, there are a bunch of places in code which needs to appropriately
# commented our for those removed corresponding negative coefficients. 
# Those place have been marked with a sequence like this: #####################


#Set data ranges per file
low  <-c(501 ,1   ,601 ,1  ,1   ,1)
high <-c(1200,1450,1750,650,1400,1500)
names(low)  <- c("i11_xz_r.csv","i21_deepsjeng_r.csv","i41_leela_r.csv","f21_blender_r.csv","f31_cactuBSSN_r.csv","f41_povray_r.csv")
names(high) <- c("i11_xz_r.csv","i21_deepsjeng_r.csv","i41_leela_r.csv","f21_blender_r.csv","f31_cactuBSSN_r.csv","f41_povray_r.csv")
low[file]
high[file]


#Read file
data <- data.frame(read.csv(file,header = FALSE,skip = 1))
data <- data[,c("V1","V2","V4")]
data$V2 <- as.numeric(as.character(data$V2))
head(data)



# Pivot, Filter, Normalize Data ---------------------------------------

#Pivot and Rename cols
pivoted_data <- dcast(data, V1 ~ V4, value.var="V2", fun.aggregate=sum)
pivoted_data <- setnames(pivoted_data,
                         old = c("V1",
                                 "CPU_CLK_UNHALTED.THREAD_P",
                                 "INST_RETIRED.ANY",
                                 "icache.misses",
                                 "dtlb_load_misses.miss_causes_a_walk",
                                 "itlb_misses.miss_causes_a_walk",
                                 "br_misp_exec.all_branches",
                                 "l1d.replacement",
                                 "L2_RQSTS.MISS",
                                 "LONGEST_LAT_CACHE.MISS"),
                         new = c("Time",
                                 "Cycles",
                                 "Instruction",
                                 "Icache Miss",
                                 "DTLB Miss",
                                 "ITLB Miss",
                                 "Branch Misprediction",
                                 "L1 Miss",
                                 "L2 Miss",
                                 "L3 Miss"))
dim(pivoted_data)

#Add CPI columns, Remove unwanted columns
pivoted_data <- transform(pivoted_data, CPI=Cycles/Instruction)
pivoted_data <- subset(pivoted_data, select = -c(Time,Cycles,Instruction))

#Filter 100 rows from head and tail to account for cold misses/outliers
pivoted_data <- head(pivoted_data,-100)
pivoted_data <- tail(pivoted_data,-100)
pivoted_data <- pivoted_data %>% slice(low[file]:high[file])
head(pivoted_data)

#Normalize using Min-Max method
normalized_data <- normalize(pivoted_data,method = "range")
normalized_data$CPI <- pivoted_data$CPI
head(normalized_data)



# Regression ---------------------------------------

#Split into training and test data
rows = dim(normalized_data)[1]
sample_size = floor(0.8*rows)
set.seed(777)
rows
sample_size
picked = sample(seq_len(rows),size = sample_size)
train <- normalized_data[picked,]
test  <- normalized_data[-picked,]


# LM ###############################################################
model <- lm(CPI ~                
            Icache.Miss          +    #1
            Branch.Misprediction +    #2
            DTLB.Miss            +    #3
            ITLB.Miss            +    #4
            L1.Miss              +    #5
            L2.Miss              +    #6
            L3.Miss,                  #7
            data = train
)
summary(model)


# Make prediction and calculate RMSE ###########################
pred =    model$coefficients["(Intercept)"]          +
  test[1]*model$coefficients["Branch.Misprediction"] +
  test[2]*model$coefficients["DTLB.Miss"]            +
  test[3]*model$coefficients["Icache.Miss"]          +
  test[4]*model$coefficients["ITLB.Miss"]            +
  test[5]*model$coefficients["L1.Miss"]              +
  test[6]*model$coefficients["L2.Miss"]              +
  test[7]*model$coefficients["L3.Miss"]
head(pred)
pred=as.vector(pred$Branch)
error = pred-test$CPI
RMSE = sqrt(mean(error^2))
RMSE


# CPI Stack Calculation ---------------------------------------------


coeff = as.vector(model$coefficients)
model$coefficients
###################################################################
temp_Mean = colMeans(subset(normalized_data, select = c(
  Icache.Miss
  ,Branch.Misprediction
  ,DTLB.Miss
  ,ITLB.Miss
  ,L1.Miss
  ,L2.Miss
  ,L3.Miss
  #,CPI
)))
means = c(1,as.vector(temp_Mean))
temp_Mean
stack = coeff*means
stack

a=mean(normalized_data$CPI)
b=sum(stack)
stack_err = (abs(a-b)/a)*100
mean(normalized_data$CPI)
sum(stack)
stack_err



# Plot ---------------------------------------

col = rainbow(8)
xaxis=c(1:dim(pivoted_data)[1])

p1 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = Icache.Miss),    color = col[2])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p2 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = Branch.Misprediction), color = col[1]) +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p3 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = DTLB.Miss), color = col[3])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p4 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = ITLB.Miss), color = col[4])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p5 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = L1.Miss),   color = col[5])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p6 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = L2.Miss),   color = col[6])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p7 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = L3.Miss),   color = col[7])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

p8 = ggplot(pivoted_data, aes(x=xaxis)) + labs(x="Time") +
  geom_line(aes(y = CPI),       color = col[8])  +
  theme(axis.title.x = element_blank()) #,axis.title.y = element_blank())

grid.arrange(p1, p2, p3, p4, p5, p6, p7, p8, nrow = 2)


#Plot CPI Stack ################################################

xlab = c(rep(" " , length(stack)))
ylab = as.vector(c(
paste("Base CPI"             , "(" , round( model$coefficients["(Intercept)"] , 3 ), ")" ),
paste("Icache Miss"          , "(" , round( model$coefficients["Icache.Miss"] * temp_Mean["Icache.Miss"] , 3 ) ,")" ) ,
paste("Branch Misprediction" , "(" , round( model$coefficients["Branch.Misprediction"] * temp_Mean["Branch.Misprediction"] , 3 ) ,")" ),
paste("DTLB Miss"            , "(" , round( model$coefficients["DTLB.Miss"] * temp_Mean["DTLB.Miss"] , 3 ) ,")" ),
paste("ITLB.Miss"            , "(" , round( model$coefficients["ITLB.Miss"] * temp_Mean["ITLB.Miss"] , 3 ), ")" ),
paste("L1 Miss"              , "(" , round( model$coefficients["L1.Miss"] * temp_Mean["L1.Miss"] , 3 ) ,")" ),
paste("L2 Miss"              , "(" , round( model$coefficients["L2.Miss"] * temp_Mean["L2.Miss"] , 3 ) ,")" ),
paste("L3 Miss"              , "(" , round( model$coefficients["L3.Miss"] * temp_Mean["L3.Miss"] , 3 ) ,")" ) 
))
data = data.frame(xlab,ylab,stack)
data

str= paste(file,
           "          Mean CPI = ",round(mean(normalized_data$CPI),5),
           "          CPI derived from model = ",round(sum(stack),5),
           "          Error = ",round(stack_err,3), "%")

ggplot(data, aes(fill=ylab, y=stack, x=xlab)) + 
  geom_bar(position="stack", stat="identity") +
  labs(title = str ) + 
  theme(plot.title = element_text(hjust = 0.5)) +
  # theme(legend.position="bottom") +
  scale_fill_manual(values = col)+coord_flip() +
  theme(axis.title.x = element_blank(),axis.title.y = element_blank())


# -----------------------------------------------------------------


