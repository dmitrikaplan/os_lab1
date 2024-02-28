enum class Stressor(val nameOfStressor: String) {

    CPU("cpu"),
    L1CACHE("l1cache"),
    IO_URING("io-uring"),
    IO_MIX("iomix"),
    VM("vm"),
    MMAP("mmap"),
    NETLINK_TASK("netlink-task"),
    PIPE_HERD("pipeherd"),
    PIPE("pipe"),
    SCHED_POLICY("schedpolicy"),
    MEM_RATE("memrate"),
    FORK("fork"),
    MMAP_HUGE("mmaphuge"),
}