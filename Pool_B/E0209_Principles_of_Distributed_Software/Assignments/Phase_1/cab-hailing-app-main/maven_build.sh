cd cab-service

echo "-----------------------CabService : Maven Build Start-----------------------"
./mvnw -DskipTests -q clean package
echo "-----------------------CabService : Maven Build End-----------------------"


cd ../ride-service

echo "-----------------------RideService : Maven Build Start-----------------------"
./mvnw -DskipTests -q clean package
echo "-----------------------RideService : Maven Build End-----------------------"


cd ../wallet-service

echo "-----------------------WalletService : Maven Build Start-----------------------"
./mvnw -DskipTests -q clean package
echo "-----------------------WalletService : Maven Build End-----------------------"