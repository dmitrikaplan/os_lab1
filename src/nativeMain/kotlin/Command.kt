import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
fun StringBuilder.executeCommand(): ArrayList<String> {
    val fp: CPointer<FILE>? = popen(this.toString(), "r")
    val buffer = ByteArray(32768)
    val returnString = arrayListOf<String>()

    if (fp == NULL) {
        printf("Failed to run command\n")
        exit(1)
    }

    var scan = fgets(buffer.refTo(0), buffer.size, fp)
    if (scan != null) {
        while (scan != NULL) {
            returnString.add(scan!!.toKString())
            scan = fgets(buffer.refTo(0), buffer.size, fp)
        }
    }

    pclose(fp)
    return returnString
}


fun buildTests(
    stressor: Stressor,
    flagsAndArguments: Map<String, String?>,
    timeSeconds: ULong,
    parser: List<String>.() -> String,
    sudo: Boolean = false,
    metrics: Boolean = true,
    max: Int = 16,
    additionalCommand: AdditionalCommand? = null,
    step: Int = 2,
): List<Pair<Int, String>> {

    val commandBuilder = StringBuilder()
        .append("${if(sudo) "sudo " else ""}stress-ng ${if (metrics) "--metrics" else ""} -t $timeSeconds")
        .also { command ->
            flagsAndArguments.forEach {
                command.append(" --${it.key} ${it.value ?: ""}")
            }
        }
        .append(" --${stressor.nameOfStressor} ")


    return (2..max step step)
        .map {
            it to commandBuilder
                .append(it)
                .also { fullCommand -> println(fullCommand.toString()) }
                .executeCommand()
                .let { strings ->
                    additionalCommand?.let {
                        StringBuilder(additionalCommand.command)
                            .executeCommand()
                            .parser()
                    } ?: strings.parser()
                }
                .also {
                    commandBuilder.deleteRange(commandBuilder.lastIndexOf(" ") + 1, commandBuilder.length)
                }
        }

}



fun buildL1CacheTest(
    stressor: Stressor,
    timeSeconds: ULong,
    parser: List<String>.() -> String,
    metrics: Boolean = true,
): List<Pair<Int, String>> {
    val commandBuilder = StringBuilder()
        .append("stress-ng ${if (metrics) "--metrics" else ""} -t $timeSeconds")
        .append(" --${stressor.nameOfStressor} 1")
        .append(" --memrate-bytes ")


    return (240..272 step 4)
        .map {
            it to commandBuilder
                .append(it)
                .append("K")
                .also { fullCommand -> println(fullCommand.toString())
                }
                .executeCommand()
                .parser()
                .also {
                    commandBuilder.deleteRange(commandBuilder.lastIndexOf(" ") + 1, commandBuilder.length)
                }
        }
}
