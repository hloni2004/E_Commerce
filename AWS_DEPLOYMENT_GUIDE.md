# AWS Deployment Guide

## Environment Variables Prepared

Your backend is now ready for AWS deployment with the following files:

- **[aws.env](aws.env)** - Environment variables in .env format
- **[aws-eb.config](aws-eb.config)** - Key=Value format for Elastic Beanstalk
- **[application.properties](src/main/resources/application.properties)** - Now uses environment variables

## Deployment Options

### Option 1: AWS Elastic Beanstalk

1. **Install EB CLI**:
   ```bash
   pip install awsebcli
   ```

2. **Initialize Elastic Beanstalk**:
   ```bash
   cd E_Commerce
   eb init -p java-21 my-backend-app --region eu-west-1
   ```

3. **Create environment and deploy**:
   ```bash
   eb create production-env
   ```

4. **Set environment variables** (use values from `aws-eb.config`):
   ```bash
   eb setenv SPRING_DATASOURCE_URL="jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0"
   eb setenv SPRING_DATASOURCE_USERNAME="postgres.widewqtjdgbphbksxpco"
   eb setenv SPRING_DATASOURCE_PASSWORD="Dankoplz52123"
   # ... (continue for all variables from aws-eb.config)
   ```

   Or set them via AWS Console:
   - Go to: Elastic Beanstalk > Your Environment > Configuration > Software
   - Add all key-value pairs from `aws-eb.config`

### Option 2: AWS EC2

1. **Build JAR**:
   ```bash
   mvn clean package -DskipTests
   ```

2. **Upload to EC2**:
   ```bash
   scp -i your-key.pem target/E-Commerce-1.0-SNAPSHOT.jar ec2-user@your-ec2-ip:~/
   scp -i your-key.pem aws.env ec2-user@your-ec2-ip:~/
   ```

3. **SSH to EC2 and run**:
   ```bash
   ssh -i your-key.pem ec2-user@your-ec2-ip
   
   # Install Java 21
   sudo yum install java-21-amazon-corretto
   
   # Load environment variables and run
   export $(cat aws.env | xargs)
   java -jar E-Commerce-1.0-SNAPSHOT.jar
   ```

4. **Create systemd service** (optional for auto-start):
   ```bash
   sudo nano /etc/systemd/system/backend.service
   ```
   
   Add:
   ```ini
   [Unit]
   Description=Spring Boot Backend
   After=network.target

   [Service]
   Type=simple
   User=ec2-user
   EnvironmentFile=/home/ec2-user/aws.env
   ExecStart=/usr/bin/java -jar /home/ec2-user/E-Commerce-1.0-SNAPSHOT.jar
   Restart=always

   [Install]
   WantedBy=multi-user.target
   ```

   Enable and start:
   ```bash
   sudo systemctl enable backend
   sudo systemctl start backend
   ```

### Option 3: AWS ECS (Docker)

1. **Create Dockerfile** (if not exists):
   ```dockerfile
   FROM eclipse-temurin:21-jre
   WORKDIR /app
   COPY target/E-Commerce-1.0-SNAPSHOT.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. **Build and push to ECR**:
   ```bash
   aws ecr create-repository --repository-name my-backend
   aws ecr get-login-password --region eu-west-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.eu-west-1.amazonaws.com
   
   docker build -t my-backend .
   docker tag my-backend:latest YOUR_ACCOUNT_ID.dkr.ecr.eu-west-1.amazonaws.com/my-backend:latest
   docker push YOUR_ACCOUNT_ID.dkr.ecr.eu-west-1.amazonaws.com/my-backend:latest
   ```

3. **Create ECS Task Definition** with environment variables from `aws.env`

4. **Deploy to ECS**

### Option 4: AWS Lambda (Serverless)

1. **Add AWS Serverless dependency** to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.amazonaws.serverless</groupId>
       <artifactId>aws-serverless-java-container-springboot3</artifactId>
       <version>2.0.0-M2</version>
   </dependency>
   ```

2. **Create Lambda handler**

3. **Package and deploy via AWS SAM or Serverless Framework**

## Important Notes

1. **Security Groups**: Ensure your AWS security group allows:
   - Inbound: Port 8080 (or your chosen port)
   - Outbound: Port 6543 (for Supabase connection)

2. **Health Checks**: Configure health check endpoint if using load balancer

3. **Database Connection**: The configuration uses Supabase pooler at `aws-0-eu-west-1.pooler.supabase.com:6543`

4. **CORS**: Update `APP_CORS_ALLOWED_ORIGINS` to include your AWS domain

5. **Logs**: Check CloudWatch logs for troubleshooting

6. **Cost**: Monitor AWS costs, especially for EC2/ECS instances

## Testing Deployment

After deployment, test your endpoints:

```bash
# Health check
curl https://your-aws-domain.com/actuator/health

# Test API
curl https://your-aws-domain.com/api/products
```

## Troubleshooting

- **Connection timeout**: Check security groups and network ACLs
- **Database connection failed**: Verify Supabase credentials and pooler URL
- **Application won't start**: Check CloudWatch logs for errors
- **Port issues**: Ensure SERVER_PORT matches your AWS configuration
