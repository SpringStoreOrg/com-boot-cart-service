# Development

```bash
mvn package
docker build . -t fractalwoodstories/cart-service:latest
docker push fractalwoodstories/cart-service:latest
helm upgrade --install cart-service ./helm/cart-service
```