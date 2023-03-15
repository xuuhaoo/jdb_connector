import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.system.exitProcess


object Main {
  private val TIME_OUT = 4

  @JvmStatic
  fun main(args: Array<String>?) {
    if (args.isNullOrEmpty()) {
      println("the package name which need be connected")
      exitProcess(-1)
    }

    val map = mutableMapOf<String, String>()
    for (index in args.indices step 2) {
      val key = args[index].trim()
      val value = if (index + 1 < args.size && !args[index + 1].trim().startsWith("--")) {
        args[index + 1].trim()
      } else {
        ""
      }
      map[key] = value
    }
    val packageName = args.last()
    var localPort = "8700"
    map.keys.forEach { key ->
      when (key) {
        "--localPort", "--lp" -> {
          localPort = map[key]!!
        }
      }
    }
    if (packageName.isEmpty()) {
      println("packageName is empty")
      exitProcess(-1)
    }

    val result = Runtime.getRuntime().run("adb shell ps -A", true)
    val targetResultLine = result.resultList.filter { it.contains(packageName) }.getOrElse(0) { "none" }
    if (result.isSuccess() && targetResultLine.isNotBlank()) {
      val segments = targetResultLine.split(" ").map { it.trim() }.filter { it.isNotBlank() }
      if (segments.size > 1) {
        val targetPid = segments[1].toIntOrNull()
        if (targetPid == null) {
          println("ps seg has error: $segments targetPid is NAN $targetPid")
          exitProcess(-1)
        } else {
          println("targetPid: $targetPid")
          val forwardResult = Runtime.getRuntime().run("adb -d forward tcp:${localPort} jdwp:${targetPid}", true)
          if (forwardResult.isSuccess()) {
            println("forward is done listening on $localPort and call jdb now")
            val jdbResult = Runtime.getRuntime().run(
              "jdb -connect com.sun.jdi.SocketAttach:hostname=127.0.0.1,port=${localPort}", isNeedResultMsg = true, maxResultNum = 2, timeoutSecond = TIME_OUT
            )
            if (jdbResult.resultList.none { it.contains("Exception") }) {
              println("jdb connected!!")
              exitProcess(0)
            } else {
              println("jdb has fail jdbError:${jdbResult.resultMsg}")
              exitProcess(-1)
            }
          } else {
            println("forward is error, resultSuccess:${forwardResult.isSuccess()} resultMsg:${forwardResult.resultMsg?.trim()}")
            exitProcess(-1)
          }
        }
      } else {
        println("ps seg has error: $segments")
        exitProcess(-1)
      }
    } else {
      println("adb ps call has error:${result.resultMsg}")
      exitProcess(-1)
    }
  }
}