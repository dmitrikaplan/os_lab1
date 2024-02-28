#include <algorithm>
#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <fcntl.h>
#include <iostream>
#include <signal.h>
#include <unistd.h>

static char buffer[1 << 20];
static volatile std::atomic<bool> terminate = false;
static uint64_t max = 0;
static void sighandler(int signum) { terminate = true; }

int main(void) {
  signal(SIGINT, sighandler);
  for (;;) {
    int fd = open("/proc/meminfo", O_RDONLY, 0);
    read(fd, buffer, sizeof(buffer));
    char *s = buffer;
    while (*s != '\n') {
      ++s;
    }
    ++s;
    while (*s != '\n') {
      ++s;
    }
    ++s;
    while (!('0' <= *s && *s <= '9')) {
      ++s;
    }
    uint64_t val = std::strtoll(s, nullptr, 10);
    max = std::max(max, val);
    close(fd);
    if (terminate == true) {
      std::cout << '\n' << max << '\n';
      return 0;
    }
  }
}

