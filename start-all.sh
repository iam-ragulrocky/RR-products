#!/bin/bash
echo "Starting RR-products services. Keep MySQL running."
(cd eureka-server && mvn spring-boot:run > ../logs-eureka.txt 2>&1 &)
sleep 25
for s in api-gateway auth-service product-service order-service payment-service; do
  (cd $s && mvn spring-boot:run > ../logs-$s.txt 2>&1 &)
done
cd frontend && npm install && npm run dev
