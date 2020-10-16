package cz.dobris.zkousec

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.lang.IllegalArgumentException
import java.net.URL

class Storage {
    companion object {
        val DIR_NAME = "quezzes"

        fun listQFiles(context: Context): Array<String> {
            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
            val l = dir.list()
            return if (l != null) l else Array<String>(0, { "" })
        }

        fun saveQFileFromUrl(url: String, context: Context) {
            //TODO preform download in a separate thread
//            saveQFile(BufferedInputStream(URL(url).openStream()), context)
            saveQFile("<questiions></questions>".byteInputStream(), context)
        }

        fun saveQFile(input: InputStream, context: Context) {
            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
            val name = "example.xml"  //TODO obtain the name from the XML doc
            val file = File(dir, name)
            if (file.exists()) throw IllegalArgumentException("File $name already exists.")

            val created = file.createNewFile()
            if (!created) throw IllegalArgumentException("File $name cannot be created.")

            try {
                input.use({ inStream ->
                    file.outputStream().use({ fileOutputStream ->
                        val dataBuffer = ByteArray(1024)
                        var bytesRead: Int = 0
                        while (inStream.read(dataBuffer, 0, 1024).also({ bytesRead = it }) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead)
                        }
                    })
                })
            } catch (e: IOException) {
                throw e
            }

        }

        fun deleteQFile(name: String, context: Context) {
            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
            val f = File(dir, name)
            if (!f.exists()) throw IllegalArgumentException("File $name does not exist.")
            f.delete()
        }

        fun loadQFile(name: String, context: Context) {
//            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
//            val file = File(dir, name)
//            if(!file.exists()) throw IllegalArgumentException("File $name does not exist.")
//            BufferedInputStream(file.inputStream())

            val inputStream = context.openFileInput(DIR_NAME + File.pathSeparator + name)
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            //inputStream.readFeed(parser)
            //TODO


        }
    }
}