package org.gedanken.org.farley.vision

/**
  * 
  * vision/FacesTest.scala
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
import org.scalatest._

class FacesTest extends FlatSpec with Matchers {

  behavior of "Face recognition"

  var image : Mat = null

  it should "load an image" in {
    image = Faces.load(getClass().getResource("/lena.png").getPath())

    (image) should not be (null)
  }

  it should "recognize a face" in {
    val detections = Faces.detect(image, Faces.FRONTAL_FACE)
    (detections.toArray.length) should be (1)
  }

  it should "not recognize a face if face must be too large" in {
    val detections = Faces.detect(image, Faces.FRONTAL_FACE, 1.1, 3, 0, new Size(400, 400))
    (detections.toArray.length) should be (0)
  }
}
