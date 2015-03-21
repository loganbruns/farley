package org.gedanken.org.farley.vision

/**
  * 
  * vision/Media.scala
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
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import scala.collection.mutable.ArrayBuffer

object Media {

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  def annotate(image: Mat, detections: MatOfRect, color: Scalar) : Mat = {
    for (rect <- detections.toArray)
      Imgproc.rectangle(
        image,
        new Point(rect.x, rect.y),
        new Point(rect.x + rect.width, rect.y + rect.height),
        color)

    return image
  }

  def load(path: String) : Mat = {
    Imgcodecs.imread(path)
  }

  def load(bytes: Array[Byte]) : Mat = {
    Imgcodecs.imdecode(
      new MatOfByte(bytes: _*),
      Imgcodecs.CV_LOAD_IMAGE_ANYDEPTH | Imgcodecs.CV_LOAD_IMAGE_COLOR
    )
  }

  def save(path: String, image: Mat) : Unit = {
    Imgcodecs.imwrite(path, image)
  }

  def collect_frames[B](path: String, process: Mat => B) : Seq[B] = {
    val collection = new ArrayBuffer[B]

    val video = new VideoCapture(path)
    val image = new Mat
    while (video.read(image))
      collection += process(image)

    return collection.result
  }

}
