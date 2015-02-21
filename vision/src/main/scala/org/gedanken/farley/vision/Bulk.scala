package org.gedanken.org.farley.vision

/**
  * 
  * vision/Bulk.scala
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

import org.apache.commons.net.ftp.FTPFile
import org.gedanken.org.farley.net.FTP
import org.opencv.core._

object Bulk {

  /** Filter images in bulk from an FTP site */
  def filter(
    ftp: FTP,
    directory: String,
    images: Seq[FTPFile]
  ) : Seq[FTPFile] = {
    images.par.filter((file : FTPFile) => {
      val image = Media.load(ftp.retrieve(directory + "/" + file.getName))
      val flippedImage = new Mat

      Core.flip(image, flippedImage, 0)
      
      (Faces.detect(image, Faces.FRONTAL_FACE).toArray.length > 0) ||
      (Faces.detect(image, Faces.RIGHT_PROFILE_FACE).toArray.length > 0) ||
      (Faces.detect(flippedImage, Faces.FRONTAL_FACE).toArray.length > 0) ||
      (Faces.detect(flippedImage, Faces.RIGHT_PROFILE_FACE).toArray.length > 0)
    }).seq
  }
}
