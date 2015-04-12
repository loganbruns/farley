### Third-party repository

Migrating over to third party repository instead of copy into cache
approach.

#### Installed jar files

mvn deploy:deploy-file -Durl=file://repo -Dfile=/opt/OpenCV/share/OpenCV/java/opencv-300.jar -DgroupId=org.opencv -DartifactId=opencv -Dpackaging=jar -Dversion=3.0.0-725
