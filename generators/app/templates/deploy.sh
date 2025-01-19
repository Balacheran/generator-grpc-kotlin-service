#!/bin/bash

# Build Docker image
docker build -t <%= projectName %>:latest .

# Push to registry
docker tag <%= projectName %>:latest registry.example.com/<%= projectName %>:latest
docker push registry.example.com/<%= projectName %>:latest

# Deploy to Kubernetes
kubectl apply -f k8s/