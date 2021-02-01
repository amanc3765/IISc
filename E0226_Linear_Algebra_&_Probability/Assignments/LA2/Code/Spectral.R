

library(dplyr)
options(scipen=999)


# Read Data ----------------------------------------

data = read.csv("/home/aman/Desktop/Assignments/LA2/aman3.csv")
data$x = data$x
data$y = 500-data$y
x = data$x
y = data$y

# head(data)
plot(x,y,main='Data set',sub='Size of dataset: 918 points')
n = dim(data)[1]
k = 6


# Calculate W ----------------------------------------
w = matrix(rep(0,n*n),n,n)
for(i in 1:n){
  for(j in 1:n){
    if(i == j){
      w[i,j] = 0
    }else{
      w[i,j] = 1/(sqrt( ((x[i]-x[j])^2) + ((y[i]-y[j])^2) ))
    }
  }
}
w
dim(w)



# Calculate D ----------------------------------------
d = matrix(rep(0,n*n),n,n)
deg = rowSums(w)
for(i in 1:n){
  d[i,i] = deg[i]
}
d
dim(d)


# Calculate L ----------------------------------------
l = d-w
l

# Calculate Q ----------------------------------------
e = eigen(l)
round(e$values,3)
q =e$vectors
round(q,6)


t1 = data.frame(c(1:n),e$values)
head(t1)
plot(t1$c.1.n.,t1$e.values)

t2 = t1[with(t1, order(e.values)),]
t2$c.1.n. = t1$c.1.n.

head(t2)
plot(t1$c.1.n.,t2$e.values,main= 'Eigen Values (in Increasing order)',
     sub='NOTE: Eigen vectors corresponding to first k (= 6) non-zero eigen values were used for Spectral Clustering',
     xlab = '',ylab = '',col = 'orange')
points(t3$c.1.n.,t3$e.values,col='blue')
# text(t3$e.values ~ t3$c.1.n.,labels=round(t3$e.values,2),cex=1,font=0.2)

t3 = t2 %>% slice(2:7)  
t3
q = q[,t3$c.1.n.]
round(head(q),6)
dim(q)



# K-Means mine --------------------------------------------

euclid <- function(x,y){
  return(sqrt(sum((x-y)^2)))
}


# seedMean = sample(n)[1:k]
seedMean = c(234,433,18,178,563,801)

cluster = rep(0,n)
for(i in 1:n){
  min = euclid( q[i,] , q[seedMean[1],] )
  cluster[i] = 1
  for(j in 2:k){
    curr = euclid( q[i,] , q[seedMean[j],] )
    if(curr < min){
      min=curr
      cluster[i] = j
    }
  }
}
cluster


iter=10
while(iter>0){
  
  pointSum =  matrix(rep(0,k*k),k,k)
  count = rep(0,k)
  for(i in 1:n){
    index = cluster[i]
    pointSum[index,] = pointSum[index,] + q[i,]
    count[index] = count[index] + 1
  }
  for(i in 1:k){
    pointSum[i,] = pointSum[i,]/count[i]
  }
  print(count)
  
  
  for(i in 1:n){
    min = euclid( q[i,] , pointSum[1,] )
    cluster[i] = 1
    for(j in 2:k){
      curr = euclid( q[i,] , pointSum[j,] )
      if(curr < min){
        min=curr
        cluster[i] = j
      }
    }
  }
  
  iter = iter-1
}


# K-Means library --------------------------------------------
# c = kmeans(q,k,nstart = 50)
# clusColor = rainbow(k)[c$cluster]

clusColor = rainbow(k)[cluster]
plot(x,y,col=clusColor,main='Spectral Clustering on Data Set')
