fun main() {
    forkVm
//    buildL1CacheTest(stressor = Stressor.MEM_RATE, 10UL).also(::println) // write256, read256

}


val int32longDouble: Unit by lazy {
    buildTests(
        stressor = Stressor.CPU,
        flagsAndArguments = mapOf("cpu-method" to "int32longdouble"),
        timeSeconds = 10UL,
        parser = List<String>::bogoOpsParser
    ).forEach(::println)
}

val ipv4checksum: Unit by lazy {
    buildTests(
        stressor = Stressor.CPU,
        flagsAndArguments = mapOf("cpu-method" to "ipv4checksum"),
        timeSeconds = 10UL,
        parser = List<String>::bogoOpsParser
    ).forEach(::println)
}

val l1cache: Unit by lazy {
    buildL1CacheTest(
        stressor = Stressor.MEM_RATE,
        timeSeconds = 10UL,
        parser = List<String>::memrateParser
    ).forEach(::println)
}


val ioUring: Unit by lazy {
    buildTests(
        stressor = Stressor.IO_URING,
        flagsAndArguments = mapOf(), //iostat добавить
        timeSeconds = 10UL,
        parser = List<String>::iostatParser,
        additionalCommand = AdditionalCommand.IOSTAT
    ).forEach(::println)
}

val ioMix: Unit by lazy {
    buildTests(
        stressor = Stressor.IO_MIX,
        flagsAndArguments = mapOf(), //iostat добавить
        timeSeconds = 10UL,
        parser = List<String>::iostatParser,
        additionalCommand = AdditionalCommand.IOSTAT
    ).forEach(::println)
}

val forkVm: Unit by lazy {
    buildTests(
        stressor = Stressor.FORK,
        flagsAndArguments = mapOf("fork-vm" to null), // bogo ops позволяет постоянно форкать в ядре(создавать детей)-**
        timeSeconds = 10UL,
        parser = List<String>::bogoOpsParser
    ).forEach(::println)
}

val mmapHugeMmaps: Unit by lazy {
    buildTests(
        stressor = Stressor.MMAP_HUGE,
        flagsAndArguments = mapOf("mmaphuge-mmaps" to "65536"), //пытается использовать страницы в 2мб bogo ops
        timeSeconds = 1UL,
        parser = List<String>::bogoOpsParser
    ).forEach(::println)
}


//только с sudo
val netlinkTask: Unit by lazy {
    buildTests(
        stressor = Stressor.NETLINK_TASK,
        flagsAndArguments = mapOf(), // bogo ops - плодит детей, общается через внутренний сетевой интерфейс
        timeSeconds = 10UL,
        sudo = true,
        parser = List<String>::bogoOpsParser
    ).forEach(::println)
}

val pipeHerdYield: Unit by lazy {
    buildTests(
        stressor = Stressor.PIPE_HERD, //построить с yield и без
        flagsAndArguments = mapOf("pipeherd-yield" to null), //количество context switches per sec, после чтения-записи отдает время другому процессу
        timeSeconds = 10UL,
        parser = List<String>::contextSwitchesParser
    ).forEach(::println)
}

val pipeDataSize: Unit by lazy {
    for(i in listOf(8, 1024, 2048, 4096)) {
        buildTests(
            stressor = Stressor.PIPE, //8 максимум(создает по одному воркеру на чтение и запись)
            flagsAndArguments = mapOf("pipe-data-size" to "$i"), // MB per sec pipe write rate, сколько за раз будешь писать и читать
            timeSeconds = 10UL,
            parser = List<String>::pipeWriteRate,
            max = 8,
            step = 1
        ).forEach(::println)
    }
}

val schedPolicy: Unit by lazy {
    buildTests(
        stressor = Stressor.SCHED_POLICY, // меняет приоритеты процессоры туда-сюда bogo ops
        flagsAndArguments = mapOf(),
        timeSeconds = 10UL,
        parser = List<String>::bogoOpsParser
    ).forEach(::println)
}

val perfSchedDeadline: Unit by lazy {
    (2..16 step 2).forEach { stressor ->
            StringBuilder("sudo perf stat -e context-switches stress-ng -t 10 --cpu $stressor --sched-deadline 1000000000000 2>&1")
            .executeCommand()
            .first { it.contains("context-switches") }
            .split(" ")
            .filter { it.trim() != "" }[0]
            .also {
                println("stressor = $stressor, context-switches = $it")
            }
    }
}

val perfL1CacheLineSize: Unit by lazy {
    (2..16 step 2).forEach { stressor ->
        (4..64 step 8).forEach { lineSize ->
            StringBuilder("sudo perf stat -e cache-misses -- stress-ng -t 10 --l1cache $stressor --l1cache-line-size $lineSize 2>&1")
                .executeCommand()
                .first { it.contains("cache-misses") }
                .split(" ")
                .filter { it.trim() != "" }[0]
                .also {
                    println("stressor = $stressor, cache-line-size = $lineSize, cache-misses = $it")
                }
        }

    }

}


val perfScheduler: Unit by lazy {
    (2..16 step 2).forEach { stressor ->
        StringBuilder("sudo -i perf stat -e context-switches stress-ng -t 10 --schedpolicy $stressor 2>&1")
            .executeCommand()
            .first { it.contains("context-switches") }
            .split(" ")
            .filter { it.trim() != "" }[0]
            .also {
                println("stressor = $stressor, context-switches = $it")
            }
    }
}

val perfPipeDataSize: Unit by lazy {
    (2..8 step 2).forEach { stressor ->
        StringBuilder("sudo -i perf stat -e context-switches stress-ng -t 10 --schedpolicy $stressor 2>&1")
            .executeCommand()
            .first { it.contains("context-switches") }
            .split(" ")
            .filter { it.trim() != "" }[0]
            .also {
                println("stressor = $stressor, context-switches = $it")
            }
    }
        buildTests(
            stressor = Stressor.PIPE, //8 максимум(создает по одному воркеру на чтение и запись)
            flagsAndArguments = mapOf("pipe-data-size" to "4096"), // MB per sec pipe write rate, сколько за раз будешь писать и читать
            timeSeconds = 10UL,
            parser = List<String>::pipeWriteRate,
            max = 8,
            step = 1
        ).forEach(::println)
}

fun List<String>.bogoOpsParser(): String {
    return this.filter { string -> string.contains("metrc") }[2]
        .split(" ")
        .filter { words -> words.trim() != "" }[9].replace(".", ",")
}

fun List<String>.todo(): String {
    return this.joinToString()
}

fun List<String>.contextSwitchesParser(): String {
    return this.filter { it.contains("context switches per sec") }[0]
        .split(" ").filter { it.trim() != "" }[4].replace(".", ",") //context switches per second
}

fun List<String>.pipeWriteRate(): String {
    return this.filter { it.contains("MB per sec pipe write rate") }[0]
        .split(" ").filter { it.trim() != "" }[4].replace(".", ",")
}


fun List<String>.iostatParser(): String {
    val results = ArrayList<Pair<String, String>>()
    this.filter { it.contains("nvme0n1") || it.contains("zram0") }.forEach {
        val string = it.split(" ").filter { word -> word.trim() != "" }
        results.add(string[2].replace(".", ",") to string[3].replace(".", ","))
    }
    return "nvme(kb_read/s: ${results[0].first} kb_write/s: ${results[0].second})\n" +
            "ram(kb_read/s: ${results[1].first} kb_write/s: ${results[1].second})"
}

fun List<String>.memrateParser(): String {
    val results = ArrayList<String>()
    this.filter { it.contains("write256") || it.contains("read256") }.forEach {
        val string = it.split(" ").filter { word -> word.trim() != "" }
        results.add(string[4].replace(".", ","))
    }
    return "write256: ${results[0]}, read256: ${results[1]}"
}

