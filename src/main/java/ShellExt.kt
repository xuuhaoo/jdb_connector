import com.sun.source.tree.Scope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit.SECONDS
import javax.swing.text.html.HTML.Tag.I
import kotlin.Int.Companion

fun Runtime.run(command: String, isNeedResultMsg: Boolean = true, successCode: Int = 0, timeoutSecond: Int = run predict@{ return@predict if (isNeedResultMsg) 30 else 3 }, maxResultNum: Int = Int.MAX_VALUE): CommandResult =
  this.run(command.split(Regex("\\s+")), isNeedResultMsg, successCode, timeoutSecond, maxResultNum)

fun Runtime.run(commands: List<String>, isNeedResultMsg: Boolean = true, successCode: Int = 0, timeoutSecond: Int = run predict@{ return@predict if (isNeedResultMsg) 30 else 3 }, maxResultNum: Int = Int.MAX_VALUE): CommandResult {
  var result = -1
  if (commands.isEmpty()) {
    return CommandResult(result, successCode = successCode)
  } //  print("runtime run begin >>${commands}")
  val pb = ProcessBuilder(commands)
  pb.redirectErrorStream(true)
  val process = pb.start()
  var reader: BufferedReader? = null
  val resultStr: StringBuilder = StringBuilder("\r\n")
  val resultList = mutableListOf<String>()
  try {
    if (isNeedResultMsg) {
      reader = BufferedReader(InputStreamReader(process.inputStream))
      var s: String?
      if (maxResultNum > 0) {
        while (reader.readLine().also { s = it } != null) {
          resultStr.append(s)
          resultStr.append("\r\n")
          resultList.add(s.toString())
          if (resultList.size >= maxResultNum) {
            break
          }
        }
        resultStr.delete(resultStr.length - 2, resultStr.length)
      }
      val deferred = MyScope.async {
        if (process.waitFor(timeoutSecond.toLong(), SECONDS)) {
          process.exitValue()
        } else {
          resultStr.clear()
          resultStr.append("shell error timeout!!")
          -998
        }
      }
      runBlocking {
        val ret = withTimeoutOrNull((timeoutSecond) * 1000L) {
          deferred.await()
        }
        result = ret ?: -997 //unknown timeout maybe success maybe error
      }
    } else {
      val deferred = MyScope.async {
        process.waitFor(timeoutSecond.toLong(), SECONDS)
      }
      runBlocking {
        withTimeoutOrNull((timeoutSecond) * 1000L) {
          deferred.await()
        }
      }
      result = 0
    }
  } catch (e: Exception) {
    e.printStackTrace()
  } finally {
    reader?.close()
    process?.destroy()
  } //  println("\rruntime run end <<${commands} result:${resultStr.trim()}")
  return CommandResult(result, resultStr.toString(), resultList, successCode = successCode)
}

data class CommandResult(
  val resultCode: Int, val resultMsg: String? = null, val resultList: MutableList<String> = mutableListOf(), val successCode: Int = 0
) {
  fun isSuccess(): Boolean {
    return resultCode == successCode
  }

  fun hasResult(): Boolean {
    return resultList.isNotEmpty() && !resultMsg.isNullOrBlank()
  }

}


