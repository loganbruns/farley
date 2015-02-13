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
import org.opencv.highgui._
import org.opencv.objdetect._

object Faces {

  nu.pattern.OpenCV.loadShared()

  val FRONTAL_FACE = "/lbpcascade_frontalface.xml"
  val RIGHT_PROFILE_FACE = "/lbpcascade_profileface.xml"

  def detect(image: Mat, classifier: String) : MatOfRect = {
    val detector = new CascadeClassifier(getClass().getResource(classifier).getPath())
    val detections = new MatOfRect
    detector.detectMultiScale(image, detections)
    return detections
  }

  def annotate(image: Mat, detections: MatOfRect, color: Scalar) : Mat = {
    for (rect <- detections.toArray)
      Core.rectangle(
        image,
        new Point(rect.x, rect.y),
        new Point(rect.x + rect.width, rect.y + rect.height),
        color)

    return image
  }

  def load(path: String) : Mat = {
    Highgui.imread(path)
  }

  def load(bytes: Array[Byte]) : Mat = {
    Highgui.imdecode(
      new MatOfByte(bytes: _*),
      Highgui.CV_LOAD_IMAGE_ANYDEPTH | Highgui.CV_LOAD_IMAGE_COLOR
    )
  }

  def save(path: String, image: Mat) : Unit = {
    Highgui.imwrite(path, image)
  }

}
