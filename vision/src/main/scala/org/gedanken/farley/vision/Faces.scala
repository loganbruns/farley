package org.gedanken.org.farley.vision

/**
  * 
  * vision/Faces.scala
  * 
  * Copyright 2015 Logan O'Sullivan Bruns
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *  http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import org.opencv.core._
import org.opencv.objdetect._

object Faces {

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  val FRONTAL_FACE = "/lbpcascade_frontalface.xml"
  val RIGHT_PROFILE_FACE = "/lbpcascade_profileface.xml"
  val DEFAULT_MIN_SIZE = new Size(30, 30)
  val DEFAULT_MAX_SIZE = new Size()

  def detect(
    image: Mat,
    classifier: String,
    scaleFactor: Double = 1.1,
    minNeighbors: Integer = 3,
    flags: Integer = 0,
    minSize: Size = DEFAULT_MIN_SIZE,
    maxSize: Size = DEFAULT_MAX_SIZE)
      : MatOfRect = {
    val detector = new CascadeClassifier(getClass().getResource(classifier).getPath())
    val detections = new MatOfRect
    detector.detectMultiScale(
      image,
      detections,
      scaleFactor,
      minNeighbors,
      flags,
      minSize,
      maxSize
    )
    return detections
  }

}
