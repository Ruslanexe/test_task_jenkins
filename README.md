# Deployment and CI/CD Pipeline Documentation

## Overview
This repository outlines the process for deploying and setting up a CI/CD pipeline using Jenkins, Docker, Prometheus, and Grafana for monitoring. The CI/CD pipeline includes steps for building a Docker image, running the container, and basic health checks.

## Prerequisites
Ensure the following are installed and configured on your system:
- Jenkins
- Docker
- Docker Compose
- Node Exporter
- Prometheus
- Grafana

## CI/CD Pipeline Configuration
The following Jenkins pipeline script is used to automate the build, deployment, and testing process:

```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Ruslanexe/test_task_jenkins', branch: 'main'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build('python-app:latest')
                }
            }
        }
        stage('Run Container') {
            steps {
                script {
                    sh 'docker run -d -p 5000:5000 --name python_app python-app:latest'
                    sh 'docker ps'
                    sh "docker logs python_app"
                    sh 'docker exec python_app ls -la /app'
                    sh "curl -f http://localhost:5000/health || exit 1"
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    sh "curl -f http://localhost:5000/health || exit 1"
                }
            }
        }
    }
    post {
        always {
            script {
                sh 'docker rm -f python_app || true'
            }
        }
    }
}
```

### Pipeline Steps Explained
1. **Checkout**: Clones the repository from the main branch.
2. **Build Docker Image**: Builds the Docker image with the tag `python-app:latest`.
3. **Run Container**: Runs the container, checks logs, lists files, and ensures the application is reachable via a health check endpoint.
4. **Test**: Performs an additional health check to verify the deployment.
5. **Cleanup**: Removes the running container after the pipeline completes.

## Monitoring and Alerting Setup
To monitor the system and set up alerts, we use Prometheus and Grafana with Node Exporter. The following `docker-compose.yaml` sets up Prometheus, Node Exporter, and Grafana:

```yaml
version: '3.7'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  node_exporter:
    image: prom/node-exporter:latest
    container_name: node_exporter
    ports:
      - "9100:9100"

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
```

### Alerting Configuration
Alerts are configured to trigger when RAM usage exceeds 70%. This configuration can be set in the `prometheus.yml` file

## Accessing Monitoring Tools
- **Prometheus**: Accessible at `http://localhost:9090`
- **Node Exporter**: Collects and exports hardware and OS metrics.
- **Grafana**: Accessible at `http://localhost:3000` (default admin login: `admin/admin`).

### Setting Up Alerts in Grafana
1. Go to `Alerting` > `Alert Rules` in Grafana.
2. Create a new alert based on the Prometheus data source.
3. Set the conditions and thresholds according to your requirements.

## Conclusion
This documentation provides an overview of deploying a Jenkins-based CI/CD pipeline, configuring Prometheus and Grafana for monitoring, and setting up alerts to monitor critical system metrics.
