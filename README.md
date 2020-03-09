# Image secret
[![Build Status](https://travis-ci.org/izalith/imagesecret.svg?branch=master)](https://travis-ci.org/izalith/imagesecret)

 A tool that can be used to hide any secret information in images. JPEG and PNG images are supported.
 The most basic method - Least Significant Bit is used to store the data in images.
 A colour pixel is composed of red, green and blue, encoded on one byte.
 For PNG images the idea is to store information in the first and second bits of every pixel's RGB component.
 The difference between the original and modified pixels is not visible to the human eye. 
 JPEG uses a lossy form of compression and RGB representation of an image changes after a conversion into .jpeg file.
 That's why a secret data injects directly into JPEG discrete cosine transform coefficients. More precisely, into their last bits.

Features:
* Supports JPEG and PNG image formats
* Implements Least Significant Bit technique
* Volume of stored data - up to 10% of an image size
* Web application with UI to embed/extract text data of any files in/from images.

## Getting Started
### Prerequisites

* Java 8 or higher

###Running the backend
Start the backend with listening on 8080 port
```
gradlew bootRun
```

###Running the frontend
Download node.js and npm using gradle plugin, then start the frontend server with listening on 3000 port, connects to the backend's 8080 port with next commands:

```
gradlew npmInstall
gradlew startFront
```
Or, if you have Node.js and Npm installed
```
npm start
```

Access http://localhost:3000

## Deployment

Next task creates a jar with tomcat web server and compiled js in backend/build/libs
```
gradle bootJar
```
After that you can start the jar with
```
java -jar backend-<version>.jar
```
Access http://localhost:8080

## Developing

The application consists of 3 modules:

* steglib - The library module with implementations of an image steganography. Provides an API for embedding/extracting a binary data in/from images
* backend - Spring boot application provides rest services for using the library
* frontend - React UI for accessing the web application

## Built With
* [Gradle](https://www.gradle.org) - Build tool
* [Detekt](https://arturbosch.github.io/detekt/) - Static code analysis for Kotlin

Frontend:
* [Node.js](https://nodejs.org/) - JS runtime environment with NPM package manager. Downloads using "com.moowork.node" gradle plugin
* [React](https://reactjs.org/) - A JavaScript library for building UI
* [Webpack](https://webpack.js.org/) - A module bundler for JS files
* [Bootstrap](https://getbootstrap.com/) - CSS framework

Backend
* [Kotlin](https://kotlinlang.org/) 
* [Spring boot](https://spring.io/projects/spring-boot)

## Authors

* **Ilya Titovskiy** - *Initial work*

## License

This project is licensed under [Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
