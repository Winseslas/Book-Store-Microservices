# Book Store Microservices Architecture

## Configuration des Ports

### Services d'Infrastructure (18xxx)
- Config Server : 18888
- Eureka Discovery : 18761
- API Gateway : 18080

### Services Métier (19xxx)
- User Manager : 19090
- Book Stock Manager : 19092
- Notification Manager : 19091

### Bases de données PostgreSQL (6xxx)
- User Manager DB : 6343 (localhost:6343)
- Book Stock Manager DB : 6344 (localhost:6344)
- Notification Manager DB : 6345 (localhost:6345)

### Services de Monitoring
- Prometheus : 8090
- Grafana : 3000

### Services Auxiliaires
- PGAdmin : 15080
- MongoDB : 27019
- Mongo Express : 18081
- MailDev : 2525
- Kafka : 9092
- Zookeeper : 22181

## Notes importantes
- Les bases de données PostgreSQL sont accessibles localement via les ports 6343, 6344 et 6345
- Tous les services utilisent le Config Server pour leur configuration via `http://ms_config_server:8888`
- Les services s'enregistrent auprès d'Eureka via `http://ms_discovery_service:8761/eureka/`
