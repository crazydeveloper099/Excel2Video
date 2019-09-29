**Excel2Video**

**About**
* Windows Application based on JAVA 8.
* Merges image and text present next to each image into a video and produces .mp4 as output for each row.
* The second column will denote the .mp4 file name for that row.
* A color is also imposed on video of each row which will be mentioned in 4th column.
* Transparency percentage of the color will be in 5th column.
* From 6th column images and text are palced in alternate manner starting from image.
* For image local image path can be used or url can be used.

**Technologies used**
* AWS Polly for converting text to mp3.
* FFMPEG to encode Image and audio file into a video file.
* JAVA AWT to set each pixel value to the specified hex color.
* JFileChooser to provide GUI to select input .xlsx(excel file).
* Apache POI to read from excel file.
