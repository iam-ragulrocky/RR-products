# RR-products

Online e-commerce microservice application with a  React UI.

## Services
| Service | Port | Database |
|---|---|---|
| eureka-server | 8761 | none |
| api-gateway | 8080 | none (JWT check, CORS, routing) |
| auth-service | 8081 | rr_auth |
| product-service | 8082 | rr_product |
| order-service | 8083 | rr_order |
| payment-service | 8084 | rr_payment |
| frontend (React + Vite) | 5173 | none |

Databases are created automatically. Default MySQL login is root / root. Change it in each service's
`src/main/resources/application.properties` if yours is different.

## Run
1. Install JDK 17, Maven 3.8+, MySQL 8 and Node 18+. Start MySQL.
2. Start services in this order, one terminal each, from each folder: `mvn spring-boot:run`
   eureka-server, api-gateway, auth-service, product-service, order-service, payment-service.
   (Or run start-all.bat on Windows / start-all.sh on Mac and Linux.)
3. Check http://localhost:8761. All five services should be listed.
4. Frontend: `cd frontend`, `npm install`, `npm run dev`, then open http://localhost:5173

## Logins
- Admin: admin@rr.com / admin123 (created automatically)
- Buyer: use Log in, then Create an account

## Flow
Shop -> Add to cart -> Place order (unpaid, stock reserved) -> My orders -> Pay now or Cancel.
Admin: add, edit, delete products and see all orders and revenue.
Card payment succeeds with any 12+ digit number (mock payment). UPI and Net banking always succeed.
