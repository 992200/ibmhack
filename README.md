# AppSense: Intelligent Duplicate Finder & Auto-Categorizer

A powerful Spring Boot application that intelligently detects duplicate files, categorizes them automatically, and provides an attractive user interface with authentication system.

## 🌟 Features

### Core Functionality
- **Smart Duplicate Detection**: Advanced algorithms for finding exact and similar files
- **Content-Based Video Analysis**: Detects similar videos based on content, not just names
- **Auto-Categorization**: Automatically categorizes files by type (audio, video, image, document, etc.)
- **Cross-Format Detection**: Identifies duplicates across different file formats
- **Enhanced UI**: Modern, responsive design with glassmorphism effects

### Authentication System
- **User Registration & Login**: Secure MongoDB-based authentication
- **Password Strength Validation**: Real-time password strength indicators
- **Form Validation**: Client-side and server-side validation
- **Session Management**: Secure session handling

### Gamification Elements
- **Interactive Forms**: Animated input fields with real-time feedback
- **Progress Indicators**: Visual progress bars and strength meters
- **Loading Animations**: Smooth loading states and transitions
- **Success Feedback**: Visual rewards for user interactions

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data MongoDB
- **Frontend**: Thymeleaf, Bootstrap 5, Font Awesome, Custom CSS
- **Database**: MongoDB (user authentication), H2 (file metadata)
- **Security**: BCrypt password encoding, Spring Security
- **Build Tool**: Maven
- **Java Version**: 17

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB Atlas account (for user authentication)

## 🚀 Getting Started

### Local Development

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd IBM_Hackerthon
   ```

2. **Configure MongoDB**
   - Update `application.properties` with your MongoDB URI
   - Ensure MongoDB Atlas cluster is accessible

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open browser and navigate to `http://localhost:8080`
   - You'll be redirected to the login page
   - Create a new account or use default credentials (admin/admin)

## 🚀 Deployment to Render

### Option 1: Using render.yaml (Recommended)

1. **Push your code to GitHub**
   ```bash
   git add .
   git commit -m "Initial commit for Render deployment"
   git push origin main
   ```

2. **Connect to Render**
   - Go to [Render Dashboard](https://dashboard.render.com)
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will automatically detect the `render.yaml` file

3. **Deploy**
   - Render will automatically build and deploy your application
   - Your app will be available at the provided URL

### Option 2: Manual Deployment

1. **Create a new Web Service**
   - Go to [Render Dashboard](https://dashboard.render.com)
   - Click "New +" → "Web Service"
   - Connect your GitHub repository

2. **Configure the service**
   - **Environment**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/IBM_Hackerthon-0.0.1-SNAPSHOT.jar`

3. **Set Environment Variables**
   ```
   SPRING_PROFILES_ACTIVE=production
   SPRING_DATA_MONGODB_URI=your_mongodb_uri
   SPRING_DATA_MONGODB_DATABASE=appsense_users
   SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES=true
   SERVER_PORT=10000
   ```

4. **Deploy**
   - Click "Create Web Service"
   - Render will build and deploy your application

### Environment Variables for Render

| Variable | Description | Required |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Set to `production` | Yes |
| `SPRING_DATA_MONGODB_URI` | Your MongoDB connection string | Yes |
| `SPRING_DATA_MONGODB_DATABASE` | MongoDB database name | Yes |
| `SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES` | Allow circular dependencies | Yes |
| `SERVER_PORT` | Port for the application (Render sets this) | No |

## 📁 Project Structure

```
IBM_Hackerthon/
├── src/main/java/com/example/appmanager/
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── ApplicationManagerController.java
│   ├── model/
│   │   ├── ApplicationFile.java
│   │   └── User.java
│   ├── repository/
│   │   └── UserRepository.java
│   └── service/
│       ├── DuplicateDetectorService.java
│       ├── FileScannerService.java
│       └── UserService.java
├── src/main/resources/
│   ├── templates/
│   │   ├── login.html
│   │   ├── signup.html
│   │   ├── index.html
│   │   └── duplicates.html
│   ├── application.properties
│   └── application-production.properties
├── render.yaml
├── Dockerfile
├── .dockerignore
└── pom.xml
```

## 🔧 Configuration

### MongoDB Setup
1. Create a MongoDB Atlas cluster
2. Get your connection string
3. Update the `SPRING_DATA_MONGODB_URI` environment variable

### File Upload Limits
- Maximum file size: 100MB
- Maximum request size: 100MB

### Security Features
- BCrypt password encoding
- Session timeout: 30 minutes
- CSRF protection enabled
- Secure headers configuration

## 🎯 Usage

### Authentication
1. **Registration**: Create a new account with username, email, and strong password
2. **Login**: Sign in with your credentials
3. **Logout**: Use the logout button in the navigation

### File Management
1. **Upload Files**: Use the file upload interface
2. **Scan for Duplicates**: Click "Find Duplicates" to analyze files
3. **View Results**: See categorized duplicates with similarity percentages
4. **Delete Files**: Remove unwanted duplicates

### Video Analysis
- The system uses advanced algorithms to detect similar videos
- Content-based analysis for cross-format video detection
- Configurable similarity thresholds

## 🔍 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Redirects to login page |
| `/login` | GET | Login page |
| `/signup` | GET | Registration page |
| `/register` | POST | User registration |
| `/dashboard` | GET | Main dashboard (redirects to /index) |
| `/index` | GET | Main application page |
| `/duplicates` | GET | Duplicate files page |
| `/scan` | POST | Scan for duplicates |
| `/delete` | POST | Delete selected files |

## 🎨 UI Features

### Design Elements
- **Glassmorphism**: Modern translucent glass effects
- **Gradient Backgrounds**: Beautiful color gradients
- **Animations**: Smooth transitions and hover effects
- **Responsive Design**: Works on all device sizes

### Interactive Elements
- **Real-time Validation**: Instant feedback on form inputs
- **Password Strength Meter**: Visual password strength indicator
- **Loading States**: Animated loading indicators
- **Success Animations**: Visual rewards for user actions

## 🐛 Troubleshooting

### Common Issues

1. **Build Failures**
   - Ensure Java 17 is installed
   - Check Maven version (3.6+)
   - Verify all dependencies are available

2. **MongoDB Connection Issues**
   - Verify MongoDB URI is correct
   - Check network connectivity
   - Ensure MongoDB Atlas IP whitelist includes Render IPs

3. **Authentication Problems**
   - Clear browser cache and cookies
   - Check if user exists in MongoDB
   - Verify password encoding

4. **File Upload Issues**
   - Check file size limits
   - Verify file permissions
   - Ensure sufficient disk space

### Render-Specific Issues

1. **Deployment Failures**
   - Check build logs in Render dashboard
   - Verify environment variables are set correctly
   - Ensure `render.yaml` is properly configured

2. **Runtime Errors**
   - Check application logs in Render dashboard
   - Verify MongoDB connection string
   - Ensure all required environment variables are set

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📞 Support

For support and questions:
- Check the troubleshooting section
- Review application logs
- Contact the development team

---

**AppSense** - Making file organization intelligent and beautiful! 🚀 