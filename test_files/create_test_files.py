#!/usr/bin/env python3
import os
import zipfile
import random
import string

def create_text_files():
    """Create text files with similar content in different order"""
    # Test case 1: Same words, different order
    with open('test1.txt', 'w') as f:
        f.write("Hello world this is a test file")
    
    with open('test2.txt', 'w') as f:
        f.write("world Hello test file this is a")
    
    # Test case 2: Similar content with minor differences
    with open('test3.txt', 'w') as f:
        f.write("Hello world this is a test file with some content")
    
    with open('test4.txt', 'w') as f:
        f.write("Hello world this is a test file with some content and more text")

def create_jar_files():
    """Create JAR files with similar content"""
    # Create a simple JAR file
    with zipfile.ZipFile('test1.jar', 'w') as jar:
        jar.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nMain-Class: Test\n')
        jar.writestr('Test.class', b'\xca\xfe\xba\xbe' + b'\x00' * 100)  # Fake class file
    
    # Create a similar JAR file with slight differences
    with zipfile.ZipFile('test2.jar', 'w') as jar:
        jar.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nMain-Class: Test\n')
        jar.writestr('Test.class', b'\xca\xfe\xba\xbe' + b'\x00' * 100 + b'\x01')  # Slightly different
    
    # Create an identical JAR file (should be detected as exact duplicate)
    with zipfile.ZipFile('test3.jar', 'w') as jar:
        jar.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nMain-Class: Test\n')
        jar.writestr('Test.class', b'\xca\xfe\xba\xbe' + b'\x00' * 100)  # Same as test1.jar

def create_apk_files():
    """Create APK-like files (ZIP files with Android structure)"""
    # Create a simple APK structure
    with zipfile.ZipFile('test1.apk', 'w') as apk:
        apk.writestr('AndroidManifest.xml', '<?xml version="1.0"?><manifest package="com.test"/>')
        apk.writestr('classes.dex', b'\x64\x65\x78' + b'\x00' * 200)  # Fake DEX file
        apk.writestr('resources.arsc', b'\x02\x00' + b'\x00' * 150)  # Fake resources
    
    # Create a similar APK with slight differences
    with zipfile.ZipFile('test2.apk', 'w') as apk:
        apk.writestr('AndroidManifest.xml', '<?xml version="1.0"?><manifest package="com.test"/>')
        apk.writestr('classes.dex', b'\x64\x65\x78' + b'\x00' * 200 + b'\x01')  # Slightly different
        apk.writestr('resources.arsc', b'\x02\x00' + b'\x00' * 150)

def create_exe_files():
    """Create EXE-like files"""
    # Create a simple executable-like file
    with open('test1.exe', 'wb') as f:
        f.write(b'MZ' + b'\x00' * 1000)  # Fake PE header
    
    # Create a similar executable with slight differences
    with open('test2.exe', 'wb') as f:
        f.write(b'MZ' + b'\x00' * 1000 + b'\x01')  # Slightly different

def create_zip_files():
    """Create ZIP files with similar content"""
    # Create a ZIP file
    with zipfile.ZipFile('test1.zip', 'w') as zip_file:
        zip_file.writestr('file1.txt', 'This is a test file')
        zip_file.writestr('file2.txt', 'Another test file')
    
    # Create a similar ZIP file
    with zipfile.ZipFile('test2.zip', 'w') as zip_file:
        zip_file.writestr('file1.txt', 'This is a test file')
        zip_file.writestr('file2.txt', 'Another test file with more content')

if __name__ == "__main__":
    print("Creating test files for duplicate detection...")
    
    # Create test directory
    os.makedirs('test_files', exist_ok=True)
    os.chdir('test_files')
    
    create_text_files()
    create_jar_files()
    create_apk_files()
    create_exe_files()
    create_zip_files()
    
    print("Test files created successfully!")
    print("\nFiles created:")
    print("- test1.txt, test2.txt (same words, different order)")
    print("- test3.txt, test4.txt (similar content)")
    print("- test1.jar, test2.jar, test3.jar (JAR files - test3.jar is identical to test1.jar)")
    print("- test1.apk, test2.apk (APK-like files)")
    print("- test1.exe, test2.exe (EXE-like files)")
    print("- test1.zip, test2.zip (ZIP files)")
    print("\nExpected results:")
    print("- test1.txt and test2.txt should be detected as duplicates (normalized text)")
    print("- test1.jar and test3.jar should be detected as exact duplicates")
    print("- test1.jar and test2.jar should be detected as near-duplicates (fuzzy hashing)")
    print("- Similar pattern for APK, EXE, and ZIP files") 