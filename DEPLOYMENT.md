# 🚀 Render Deployment Guide

This guide will help you deploy your AppSense application to Render.

## Prerequisites

1. **GitHub Account**: Your code should be in a GitHub repository
2. **Render Account**: Sign up at [render.com](https://render.com)
3. **MongoDB Atlas**: Set up a MongoDB Atlas cluster

## Step 1: Prepare Your Repository

### 1.1 Push to GitHub
```bash
git add .
git commit -m "Add Render deployment configuration"
git push origin main
```

### 1.2 Verify Files
Ensure these files are in your repository:
- ✅ `render.yaml`
- ✅ `Dockerfile`
- ✅ `.dockerignore`
- ✅ `application-production.properties`
- ✅ `pom.xml`

## Step 2: Set Up MongoDB Atlas

### 2.1 Create MongoDB Atlas Cluster
1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. Create a new cluster (free tier is fine)
3. Set up database access (username/password)
4. Set up network access (allow all IPs: `0.0.0.0/0`)

### 2.2 Get Connection String
1. Click "Connect" on your cluster
2. Choose "Connect your application"
3. Copy the connection string
4. Replace `<password>` with your actual password

## Step 3: Deploy to Render

### Option A: Using Blueprint (Recommended)

1. **Go to Render Dashboard**
   - Visit [dashboard.render.com](https://dashboard.render.com)
   - Sign in with your account

2. **Create Blueprint**
   - Click "New +" → "Blueprint"
   - Connect your GitHub account
   - Select your repository

3. **Configure Blueprint**
   - Render will automatically detect `render.yaml`
   - Review the configuration
   - Click "Apply"

4. **Set Environment Variables**
   - Go to your deployed service
   - Click "Environment" tab
   - Add these variables:
     ```
     SPRING_PROFILES_ACTIVE=production
     SPRING_DATA_MONGODB_URI=your_mongodb_connection_string
     SPRING_DATA_MONGODB_DATABASE=appsense_users
     SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES=true
     ```

5. **Deploy**
   - Click "Create Blueprint Instance"
   - Wait for deployment to complete

### Option B: Manual Deployment

1. **Create Web Service**
   - Go to [dashboard.render.com](https://dashboard.render.com)
   - Click "New +" → "Web Service"
   - Connect your GitHub repository

2. **Configure Service**
   - **Name**: `appsense-duplicate-finder`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/IBM_Hackerthon-0.0.1-SNAPSHOT.jar`

3. **Set Environment Variables**
   ```
   SPRING_PROFILES_ACTIVE=production
   SPRING_DATA_MONGODB_URI=your_mongodb_connection_string
   SPRING_DATA_MONGODB_DATABASE=appsense_users
   SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES=true
   SERVER_PORT=10000
   ```

4. **Deploy**
   - Click "Create Web Service"
   - Wait for build and deployment

## Step 4: Verify Deployment

### 4.1 Check Build Logs
- Go to your service in Render dashboard
- Click "Logs" tab
- Look for successful build messages

### 4.2 Test Your Application
- Click on your service URL
- You should be redirected to the login page
- Try creating a new account
- Test the duplicate detection features

### 4.3 Monitor Performance
- Check "Metrics" tab for performance data
- Monitor "Logs" for any errors

## Step 5: Custom Domain (Optional)

1. **Add Custom Domain**
   - Go to your service settings
   - Click "Settings" → "Custom Domains"
   - Add your domain

2. **Configure DNS**
   - Point your domain to Render's servers
   - Follow Render's DNS configuration guide

## Troubleshooting

### Build Failures
```bash
# Check if Maven wrapper exists
ls -la mvnw

# If missing, generate it
mvn wrapper:wrapper
```

### MongoDB Connection Issues
- Verify connection string format
- Check if MongoDB Atlas IP whitelist includes Render IPs
- Ensure database user has correct permissions

### Runtime Errors
- Check application logs in Render dashboard
- Verify all environment variables are set
- Ensure Java 17 is being used

### Common Error Messages

**"Build failed"**
- Check if `pom.xml` is valid
- Ensure all dependencies are available
- Verify Java version compatibility

**"Application failed to start"**
- Check environment variables
- Verify MongoDB connection
- Look for port conflicts

**"MongoDB connection refused"**
- Check MongoDB Atlas network access
- Verify connection string format
- Ensure database exists

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `production` |
| `SPRING_DATA_MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/` |
| `SPRING_DATA_MONGODB_DATABASE` | Database name | `appsense_users` |
| `SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES` | Allow circular dependencies | `true` |
| `SERVER_PORT` | Application port | `10000` |

## Performance Optimization

### 1. Enable Caching
- Thymeleaf caching is enabled in production
- Consider adding Redis for session storage

### 2. Database Optimization
- Use MongoDB Atlas M10+ for better performance
- Enable MongoDB Atlas performance advisor

### 3. Resource Allocation
- Start with free tier
- Upgrade based on usage patterns

## Security Considerations

### 1. Environment Variables
- Never commit sensitive data to Git
- Use Render's environment variable system
- Rotate passwords regularly

### 2. MongoDB Security
- Use strong passwords
- Enable MongoDB Atlas security features
- Regularly audit access logs

### 3. Application Security
- Spring Security is configured
- CSRF protection is enabled
- Session timeout is set to 30 minutes

## Monitoring and Maintenance

### 1. Health Checks
- Application has health check endpoint at `/`
- Monitor uptime in Render dashboard

### 2. Logs
- Check logs regularly for errors
- Set up log aggregation if needed

### 3. Updates
- Keep dependencies updated
- Monitor for security patches
- Test updates in staging environment

## Support

If you encounter issues:

1. **Check Render Documentation**: [docs.render.com](https://docs.render.com)
2. **Review Application Logs**: In Render dashboard
3. **Verify Configuration**: Double-check environment variables
4. **Contact Support**: Use Render's support channels

---

**Your AppSense application should now be live on Render! 🎉**

Visit your application URL and start using the intelligent duplicate finder! 