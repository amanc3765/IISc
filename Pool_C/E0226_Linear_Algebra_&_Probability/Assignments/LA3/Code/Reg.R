library(BBmisc)
options(scipen=999)
set.seed(777)

setwd("/home/aman/Desktop/LA3")
print(getwd())


explode <- function(X){
  for(i in 1:13){
    for(j in 1:13){
      X = cbind(X,X[i]*X[j])
    } 
  }
  
  return(X)
}



# -----------------------------------------------------
# Read data
DATA = data.frame(read.csv("trdata.csv",header = FALSE))
head(DATA)
dim(DATA)

X = DATA[c(2:14)]
Y = DATA[15]
head(X)
head(Y)

data = cbind(X,Y)
dim(data)



# -----------------------------------------------------
#Split Data
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


# temp = c(0.006,18,2.3,0,0.5,6.5,65.2,4.09,1,296,15.3,296.9,4.9,0)
# length(temp)
# test = rbind(test,temp)
# write.csv(test,"tsdata.csv")

test = data.frame(read.csv("tsdata.csv",header = FALSE))
test = test[c(2:14)]
tail(test)
dim(test)



# -----------------------------------------------------
d = 13

x = explode(train[c(1:d)])
x = as.matrix(normalize(x,method = "range"))
y = as.matrix(train[d+1])
dim(x)
dim(y)

valx = explode(val[c(1:d)])
valx = as.matrix(normalize(valx,method = "range"))
valy = as.matrix(val[d+1])
dim(valx)
dim(valy)

testx = explode(test[c(1:d)])
testx = as.matrix(normalize(testx,method = "range"))
# testy = as.matrix(test[d+1])
dim(testx)
# dim(testy)



# -----------------------------------------------------
rate = 0.0001
iter = 1000

lambda = 1
trial = 20
E = rep(0,trial)
L = rep(0,trial)
d = dim(x)[2]

for(a in 1:trial){
  w = rep(0,d)
  
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

# [1] 4.759006
# > sqrt(sum((valpred-valy)^2)/dim(val)[1])
# [1] 5.147646
# > sqrt(sum((testpred-testy)^2)/dim(test)[1])
# [1] 6.569363
# > 


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
w = rep(0,d)
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
plot(plotx,valy,type = 'line')
lines(plotx,valpred,col='blue')


testpred = testx%*%w
plotx = c(1:dim(test)[1])
plot(plotx,testy,type='line')
lines(plotx,testpred,col='green')




# -----------------------------------------------------
sqrt(sum((pred-y)^2)/dim(train)[1])
sqrt(sum((valpred-valy)^2)/dim(val)[1])
sqrt(sum((testpred-testy)^2)/dim(test)[1])

tail(testpred,1)
lambda

write.csv(testpred,"answer.csv")


