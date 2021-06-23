library(BBmisc)
options(scipen=999)

setwd("/home/aman/Desktop/LA3")
print(getwd())

DATA = data.frame(read.csv("trdata.csv",header = FALSE))
head(DATA)
dim(DATA)

X = DATA[c(2:14)]
Y = DATA[15]
head(X)
head(Y)
dim(X)

data = cbind(X,Y)
dim(data)



# -----------------------------------------------------
#Split Data
set.seed(777)
dim(data)
rows = dim(data)[1]
train_size = floor(0.8*rows)
val_size = floor(0.1*rows)
test_size = floor(0.1*rows)

picked = sample(seq_len(rows),size = train_size)
train = data[picked,]
rem = data[-picked,]
dim(train)
dim(rem)

picked = sample(seq_len(dim(rem)[1]),size = val_size)
val = rem[picked,]
test = rem[-picked,]
dim(val)
dim(test)



# -----------------------------------------------------
d = dim(train)[2]-1
d

x = as.matrix(normalize(train[c(1:d)],method = "range"))
y = as.matrix(train[d+1])
dim(x)
dim(y)

valx = as.matrix(normalize(val[c(1:d)],method = "range"))
valy = as.matrix(val[d+1])
dim(valx)
dim(valy)

testx = as.matrix(normalize(test[c(1:d)],method = "range"))
testy = as.matrix(test[d+1])
dim(testx)
dim(testy)



# -----------------------------------------------------
rate = 0.0001
iter = 5000

lambda = 0.1
trial = 10
E = rep(0,trial)
L = rep(0,trial)

for(a in 1:trial){
  w = as.matrix(runif(d))
  
  for(i in 1:iter){
    pred = x%*%w
    g = t(x)%*%(pred-y) + (lambda*w)
    w = w - rate*g
  }
  
  valpred = valx%*%w
  E[a] = sqrt(sum((valpred-valy)^2)/dim(val)[1])
  L[a] = lambda
  lambda = lambda/5
}

plot(c(1:trial),E,type='line',xlab = 'lambda opt')



# -----------------------------------------------------
minE = E[1]
minI = 1
for(i in 1:trial){
  if(E[i] < minE){
    minI = i
    minE = E[i]
  }
}
print(paste(minI,":",E[minI],":",L[minI]))



# -----------------------------------------------------
lambda =  L[minI]
rate = 0.0001
iter = 5000
w = as.matrix(runif(d))
e = rep(0,iter)

for(i in 1:iter){
  pred = x%*%w
  g = t(x)%*%(pred-y) + (lambda*w)
  w = w - rate*g
  e[i] = sum((pred-y)^2) + (lambda*sum(w^2))
}

plot(c(1:iter),e,type='line',xlab = paste(lambda))
e[iter]



# -----------------------------------------------------
pred = x%*%w
# plotx = c(1:dim(train)[1])
# plot(plotx,pred,type='line')
# lines(plotx,y,col='red')

valpred = valx%*%w
plotx = c(1:dim(val)[1])
plot(plotx,valpred,type='line')
lines(plotx,valy,col='blue')

testpred = testx%*%w
plotx = c(1:dim(test)[1])
plot(plotx,testpred,type='line')
lines(plotx,testy,col='green')



# -----------------------------------------------------
sqrt(sum((pred-y)^2)/dim(train)[1])
sqrt(sum((valpred-valy)^2)/dim(val)[1])
sqrt(sum((testpred-testy)^2)/dim(test)[1])



# -----------------------------------------------------
# DATA = data.frame(read.csv("trdata.csv",header = FALSE))
# head(DATA)
# dim(DATA)
# 
# X = DATA[1:100,2:14]
# head(X)
# dim(X)
# 
# write.csv(X,"tsdata.csv")


TDATA = data.frame(read.csv("tsdata.csv",skip=1,header = FALSE))
head(TDATA)
dim(TDATA)

TX = TDATA[,2:14]
head(TX)
dim(TX)

finx = as.matrix(normalize(TX,method = "range"))
finpred = finx%*%w
head(finpred)

