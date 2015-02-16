package org.gedanken.org.farley.net

import java.io.ByteArrayOutputStream
import org.apache.commons.net.ftp.FTPClient

/**
  * 
  * net/FTP.scala
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

class FTP(hostname: String) {

  val client = new FTPClient()

  client.connect(hostname)

  client.enterLocalPassiveMode

  client.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE)

  def login(username: String, password: String) = 
    client.login(username, password)

  def close() = {
    if (client.isConnected)
      client.disconnect
  }

  def listFiles = 
    client.listFiles

  def listFiles(directory: String) = 
    client.listFiles(directory)

  def retrieve(path: String) : Array[Byte] = {
    val baos = new ByteArrayOutputStream
    synchronized {
      client.retrieveFile(path, baos)
    }
    return baos.toByteArray
  }
}
