#!/usr/bin/env python3
"""
MongoDB Connection Test Script
This script tests the MongoDB Atlas connection before deployment
"""

import pymongo
from pymongo import MongoClient
import sys

def test_mongodb_connection():
    """Test MongoDB Atlas connection"""
    
    # Your MongoDB connection string
    connection_string = "mongodb+srv://sethu:1234@cluster0.dbntwx8.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
    
    try:
        print("🔍 Testing MongoDB Atlas connection...")
        print(f"Connection string: {connection_string}")
        
        # Create MongoDB client
        client = MongoClient(connection_string)
        
        # Test connection by listing databases
        print("📋 Listing databases...")
        databases = client.list_database_names()
        print(f"Available databases: {databases}")
        
        # Test specific database
        db_name = "appsense_users"
        db = client[db_name]
        
        # Test collection access
        collection_name = "users"
        collection = db[collection_name]
        
        # Test write operation
        test_doc = {"test": "connection", "timestamp": "2024-01-01"}
        result = collection.insert_one(test_doc)
        print(f"✅ Write test successful: {result.inserted_id}")
        
        # Test read operation
        doc = collection.find_one({"test": "connection"})
        print(f"✅ Read test successful: {doc}")
        
        # Clean up test document
        collection.delete_one({"test": "connection"})
        print("✅ Cleanup successful")
        
        print("🎉 MongoDB connection test PASSED!")
        return True
        
    except Exception as e:
        print(f"❌ MongoDB connection test FAILED: {e}")
        print("\n🔧 Troubleshooting steps:")
        print("1. Check if MongoDB Atlas cluster is running")
        print("2. Verify network access allows all IPs (0.0.0.0/0)")
        print("3. Ensure database user 'sethu' exists with correct permissions")
        print("4. Verify connection string format")
        return False

if __name__ == "__main__":
    success = test_mongodb_connection()
    sys.exit(0 if success else 1) 