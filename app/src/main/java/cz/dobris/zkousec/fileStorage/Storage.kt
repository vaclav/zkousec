package cz.dobris.zkousec.fileStorage

import android.content.Context
import android.util.Log
import android.util.Xml
import cz.dobris.zkousec.QuestionPackParser
import layout.QuestionPack
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.net.URL
import kotlin.random.Random

class Storage {
    companion object {
        val DIR_NAME = "quezzes"

        fun listQFiles(context: Context): Array<String> {
            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
            val l = dir.list()
            return if (l != null) l else Array<String>(0, { "" })
        }

        fun saveQFileFromUrl(url: String, testName: String, context: Context) {
            saveQFile(
                BufferedInputStream(URL(url).openStream()),
                testName,
                context
            )
//            saveQFile(
//                """
//                <testing xmlns="http://www.w3schools.com/Testovac"
//         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//         xsi:schemaLocation="http://www.w3schools.com/Testovac TestingQuestionsFormat.xsd">
//    <id>test_otazek</id>
//    <description>tests for anything and everything</description>
//    <version>1</version>
//    <questions></questions>
//</testing>
//            """.trimIndent().byteInputStream(), context
//            )
        }

        fun saveQFile(input: InputStream, testName: String, context: Context) {
            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
            val r = Random(System.currentTimeMillis()).nextInt()
            val name = "temp" + r + ".xml"
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

                val qp =
                    loadQFile(
                        name,
                        context
                    )
                Log.d("Zkousec", "Question pack " + qp.id + ":" + qp.description)
                val realName = (if (testName.length == 0) qp.id + r else testName) + ".xml"
                file.renameTo(File(dir, realName))
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

        fun loadQFile(name: String, context: Context): QuestionPack {
            val dir = context.getDir(DIR_NAME, Context.MODE_PRIVATE)
            val file = File(dir, name)
            if (!file.exists()) throw IllegalArgumentException("File $name does not exist.")
            val inputStream = file.inputStream() //.openFileInput(DIR_NAME + File.pathSeparator + name)
            return loadQFile(name, inputStream, context)
        }

        fun loadQFile(fileName : String, inputStream: FileInputStream, context: Context): QuestionPack {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return QuestionPackParser().readQuestionPack(fileName, parser)
        }

    }
}