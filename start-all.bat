@echo off
echo Starting RR-products services. Keep MySQL running.
start "eureka-server" cmd /k "cd eureka-server && mvn spring-boot:run"
timeout /t 25
start "api-gateway" cmd /k "cd api-gateway && mvn spring-boot:run"
start "auth-service" cmd /k "cd auth-service && mvn spring-boot:run"
start "product-service" cmd /k "cd product-service && mvn spring-boot:run"
start "order-service" cmd /k "cd order-service && mvn spring-boot:run"
start "payment-service" cmd /k "cd payment-service && mvn spring-boot:run"
start "frontend" cmd /k "cd frontend && npm install && npm run dev"
